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

    private val noiseService = NoiseService()

    private val requestAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                iniciarMedicion()
            } else {
                mostrarPermisoDenegado()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultadoTextView = view.findViewById(R.id.textResultadoRuido)
        botonMedir = view.findViewById(R.id.botonMedir)

        botonMedir.setOnClickListener {
            comprobarPermisoYMedir()
        }
    }

    private fun comprobarPermisoYMedir() {
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

                resultadoTextView.text = if (db > 0.0) {
                    getString(R.string.resultado_ruido, db)
                } else {
                    getString(R.string.error_medicion)
                }

                botonMedir.isEnabled = true
            }
        }.start()
    }

    private fun mostrarPermisoDenegado() {
        resultadoTextView.text = getString(R.string.permiso_microfono_denegado)
    }
}