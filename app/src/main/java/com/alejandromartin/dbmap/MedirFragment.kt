package com.alejandromartin.dbmap

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class MedirFragment : Fragment(R.layout.fragment_medir) {

    private lateinit var resultadoTextView: TextView
    private lateinit var zonaResultadoTextView: TextView
    private lateinit var estadoResultadoTextView: TextView
    private lateinit var botonMedir: Button
    private lateinit var locationHelper: LocationHelper
    private lateinit var configManager: ConfigManager

    private val noiseService = NoiseService()
    private val firestoreManager = FirestoreManager()
    private var ultimoDbValido: Double? = null

    companion object {
        private const val MIN_RELIABLE_DB = 20.0
    }

    private val requestAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                iniciarMedicion()
            } else {
                mostrarPermisoMicrofonoDenegado()
            }
        }

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                mostrarResultadoConZona()
            } else {
                mostrarSoloMensaje(getString(R.string.permiso_ubicacion_denegado))
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultadoTextView = view.findViewById(R.id.textResultadoRuido)
        zonaResultadoTextView = view.findViewById(R.id.textZonaResultado)
        estadoResultadoTextView = view.findViewById(R.id.textEstadoResultado)
        botonMedir = view.findViewById(R.id.botonMedir)

        locationHelper = LocationHelper(requireContext().applicationContext)
        configManager = ConfigManager(requireContext().applicationContext)

        val avisoPrivacidadTextView = view.findViewById<TextView>(R.id.textAvisoPrivacidad)
        avisoPrivacidadTextView.visibility =
            if (configManager.mostrarAvisoPrivacidad()) View.VISIBLE else View.GONE

        botonMedir.setOnClickListener {
            if (!AppUtils.hayConexion(requireContext())) {
                mostrarSoloMensaje(getString(R.string.sin_conexion_medicion))
                return@setOnClickListener
            }

            comprobarPermisoMicrofonoYMedir()
        }
    }

    private fun comprobarPermisoMicrofonoYMedir() {
        val permisoConcedido = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (permisoConcedido) {
            iniciarMedicion()
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permiso_microfono_explicacion),
                    Toast.LENGTH_LONG
                ).show()
            }

            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun iniciarMedicion() {
        mostrarSoloMensaje(getString(R.string.midiendo_ruido))
        botonMedir.isEnabled = false

        Thread {
            val db = noiseService.medirRuidoDurante()

            activity?.runOnUiThread {
                if (!isAdded) return@runOnUiThread

                when {
                    db <= 0.0 -> {
                        mostrarSoloMensaje(getString(R.string.error_medicion))
                    }

                    db < MIN_RELIABLE_DB -> {
                        mostrarSoloMensaje(getString(R.string.senal_demasiado_baja))
                    }

                    else -> {
                        ultimoDbValido = db
                        mostrarSoloMensaje(getString(R.string.resultado_ruido, db))
                        comprobarPermisoUbicacionYMostrarZona()
                    }
                }

                botonMedir.isEnabled = true
            }
        }.start()
    }

    private fun comprobarPermisoUbicacionYMostrarZona() {
        val permisoConcedido = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (permisoConcedido) {
            mostrarResultadoConZona()
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permiso_ubicacion_explicacion),
                    Toast.LENGTH_LONG
                ).show()
            }

            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    private fun mostrarResultadoConZona() {
        val db = ultimoDbValido ?: return

        locationHelper.getCurrentLocation(
            onSuccess = { location ->
                if (!isAdded) return@getCurrentLocation

                if (location == null) {
                    mostrarSoloMensaje(getString(R.string.ubicacion_no_disponible))
                    return@getCurrentLocation
                }

                val zona = ZoneManager.toReducedGeohash(
                    latitude = location.latitude,
                    longitude = location.longitude
                )

                mostrarResultadoCompleto(
                    nivelRuido = db,
                    zona = zona,
                    estado = getString(R.string.guardando_medicion)
                )

                firestoreManager.guardarMedicion(
                    zonaId = zona,
                    nivelRuido = db,
                    onSuccess = {
                        if (!isAdded) return@guardarMedicion

                        mostrarResultadoCompleto(
                            nivelRuido = db,
                            zona = zona,
                            estado = getString(R.string.medicion_guardada)
                        )
                    },
                    onError = {
                        if (!isAdded) return@guardarMedicion

                        mostrarResultadoCompleto(
                            nivelRuido = db,
                            zona = zona,
                            estado = getString(R.string.error_guardar_medicion),
                            estadoEsError = true
                        )
                    }
                )
            },
            onError = {
                if (!isAdded) return@getCurrentLocation
                mostrarSoloMensaje(getString(R.string.ubicacion_no_disponible))
            }
        )
    }

    private fun mostrarSoloMensaje(mensaje: String) {
        resultadoTextView.text = mensaje
        zonaResultadoTextView.visibility = View.GONE
        estadoResultadoTextView.visibility = View.GONE
    }

    private fun mostrarResultadoCompleto(
        nivelRuido: Double,
        zona: String,
        estado: String,
        estadoEsError: Boolean = false
    ) {
        resultadoTextView.text = getString(R.string.resultado_ruido, nivelRuido)

        zonaResultadoTextView.text = getString(R.string.resultado_zona, zona)
        zonaResultadoTextView.visibility = View.VISIBLE

        estadoResultadoTextView.text = estado
        estadoResultadoTextView.visibility = View.VISIBLE

        val colorEstado = if (estadoEsError) {
            Color.rgb(198, 40, 40)
        } else {
            ContextCompat.getColor(requireContext(), R.color.dbmap_success)
        }

        estadoResultadoTextView.setTextColor(colorEstado)
    }

    private fun mostrarPermisoMicrofonoDenegado() {
        mostrarSoloMensaje(getString(R.string.permiso_microfono_denegado))
    }
}