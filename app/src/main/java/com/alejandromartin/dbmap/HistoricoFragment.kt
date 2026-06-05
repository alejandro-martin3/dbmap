package com.alejandromartin.dbmap

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.alejandromartin.dbmap.model.AgregadoZona
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoricoFragment : Fragment(R.layout.fragment_historico) {

    private lateinit var resultadoTextView: TextView
    private val firestoreManager = FirestoreManager()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resultadoTextView = view.findViewById(R.id.textHistoricoResultado)
        val botonActualizar = view.findViewById<Button>(R.id.botonActualizarHistorico)

        botonActualizar.setOnClickListener {
            cargarHistorico()
        }

        cargarHistorico()
    }

    private fun cargarHistorico() {
        resultadoTextView.text = getString(R.string.historico_cargando)

        firestoreManager.obtenerUltimosAgregados(
            onSuccess = { agregados ->
                resultadoTextView.text = if (agregados.isEmpty()) {
                    getString(R.string.historico_sin_datos)
                } else {
                    formatearAgregados(agregados)
                }
            },
            onError = {
                resultadoTextView.text = getString(R.string.historico_error)
            }
        )
    }

    private fun formatearAgregados(agregados: List<AgregadoZona>): String {
        return agregados.joinToString(separator = "\n\n") { agregado ->
            val fecha = formatearFecha(agregado.fechaInicio)

            "Zona: ${agregado.zonaId}\n" +
                    "Periodo: ${agregado.periodo}\n" +
                    "Inicio: $fecha\n" +
                    "Ruido medio: ${"%.1f".format(agregado.nivelRuidoPromedio)} dB\n" +
                    "Muestras: ${agregado.numeroMuestras}"
        }
    }

    private fun formatearFecha(timestamp: Long): String {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return formato.format(Date(timestamp))
    }
}