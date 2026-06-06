package com.alejandromartin.dbmap

import android.Manifest
import android.content.pm.PackageManager
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
    private lateinit var botonMedir: Button
    private lateinit var locationHelper: LocationHelper

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
                mostrarResultadoConInfo(getString(R.string.permiso_ubicacion_denegado))
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultadoTextView = view.findViewById(R.id.textResultadoRuido)
        botonMedir = view.findViewById(R.id.botonMedir)
        locationHelper = LocationHelper(requireContext().applicationContext)

        botonMedir.setOnClickListener {
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
        resultadoTextView.text = getString(R.string.midiendo_ruido)
        botonMedir.isEnabled = false

        Thread {
            val db = noiseService.medirRuidoDurante()

            activity?.runOnUiThread {
                if (!isAdded) return@runOnUiThread

                when {
                    db <= 0.0 -> {
                        resultadoTextView.text = getString(R.string.error_medicion)
                    }

                    db < MIN_RELIABLE_DB -> {
                        resultadoTextView.text = getString(R.string.senal_demasiado_baja)
                    }

                    else -> {
                        ultimoDbValido = db
                        resultadoTextView.text = getString(R.string.resultado_ruido, db)
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
                    mostrarResultadoConInfo(getString(R.string.ubicacion_no_disponible))
                    return@getCurrentLocation
                }

                val zona = ZoneManager.toReducedGeohash(
                    latitude = location.latitude,
                    longitude = location.longitude
                )

                resultadoTextView.text = getString(
                    R.string.resultado_ruido_zona_estado,
                    db,
                    zona,
                    getString(R.string.guardando_medicion)
                )

                firestoreManager.guardarMedicion(
                    zonaId = zona,
                    nivelRuido = db,
                    onSuccess = {
                        if (!isAdded) return@guardarMedicion

                        resultadoTextView.text = getString(
                            R.string.resultado_ruido_zona_estado,
                            db,
                            zona,
                            getString(R.string.medicion_guardada)
                        )
                    },
                    onError = {
                        if (!isAdded) return@guardarMedicion

                        resultadoTextView.text = getString(
                            R.string.resultado_ruido_zona_estado,
                            db,
                            zona,
                            getString(R.string.error_guardar_medicion)
                        )
                    }
                )
            },
            onError = {
                if (!isAdded) return@getCurrentLocation
                mostrarResultadoConInfo(getString(R.string.ubicacion_no_disponible))
            }
        )
    }

    private fun mostrarResultadoConInfo(info: String) {
        val db = ultimoDbValido ?: return
        resultadoTextView.text = getString(R.string.resultado_ruido_con_info, db, info)
    }

    private fun mostrarPermisoMicrofonoDenegado() {
        resultadoTextView.text = getString(R.string.permiso_microfono_denegado)
    }
}