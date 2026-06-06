package com.alejandromartin.dbmap

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.alejandromartin.dbmap.model.AgregadoZona
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoricoFragment : Fragment(R.layout.fragment_historico) {

    private lateinit var contenedorHistorico: LinearLayout
    private val firestoreManager = FirestoreManager()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contenedorHistorico = view.findViewById(R.id.layoutHistoricoContenido)
        val botonActualizar = view.findViewById<Button>(R.id.botonActualizarHistorico)

        botonActualizar.setOnClickListener {
            cargarHistorico()
        }

        cargarHistorico()
    }

    private fun cargarHistorico() {
        mostrarMensaje(getString(R.string.historico_cargando))

        firestoreManager.obtenerUltimosAgregados(
            onSuccess = { agregados ->
                if (!isAdded) return@obtenerUltimosAgregados

                if (agregados.isEmpty()) {
                    mostrarMensaje(getString(R.string.historico_sin_datos))
                } else {
                    mostrarAgregados(agregados)
                }
            },
            onError = {
                if (!isAdded) return@obtenerUltimosAgregados
                mostrarMensaje(getString(R.string.historico_error))
            }
        )
    }

    private fun mostrarMensaje(mensaje: String) {
        contenedorHistorico.removeAllViews()

        val textView = TextView(requireContext()).apply {
            text = mensaje
            textSize = 16f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.dbmap_text_secondary))
        }

        contenedorHistorico.addView(textView)
    }

    private fun mostrarAgregados(agregados: List<AgregadoZona>) {
        contenedorHistorico.removeAllViews()

        agregados.forEach { agregado ->
            contenedorHistorico.addView(crearTarjetaAgregado(agregado))
        }
    }

    private fun crearTarjetaAgregado(agregado: AgregadoZona): MaterialCardView {
        val context = requireContext()

        val card = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(16)
            }

            radius = dp(18).toFloat()
            cardElevation = dp(2).toFloat()
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.dbmap_card))
            setContentPadding(dp(18), dp(18), dp(18), dp(18))
        }

        val contenido = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val zonaTextView = TextView(context).apply {
            text = getString(R.string.historico_card_zona, agregado.zonaId)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ContextCompat.getColor(context, R.color.dbmap_text_primary))
        }

        val ruidoTextView = TextView(context).apply {
            text = getString(R.string.historico_card_ruido, agregado.nivelRuidoPromedio)
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(obtenerColorRuido(agregado.nivelRuidoPromedio))
        }

        val periodoTextView = crearTextoSecundario(
            getString(R.string.historico_card_periodo, agregado.periodo)
        )

        val inicioTextView = crearTextoSecundario(
            getString(R.string.historico_card_inicio, formatearFecha(agregado.fechaInicio))
        )

        val muestrasTextView = crearTextoSecundario(
            getString(R.string.historico_card_muestras, agregado.numeroMuestras)
        )

        contenido.addView(zonaTextView)
        contenido.addView(ruidoTextView)
        contenido.addView(periodoTextView)
        contenido.addView(inicioTextView)
        contenido.addView(muestrasTextView)

        card.addView(contenido)

        return card
    }

    private fun crearTextoSecundario(texto: String): TextView {
        return TextView(requireContext()).apply {
            text = texto
            textSize = 14f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.dbmap_text_secondary))
            setPadding(0, dp(4), 0, 0)
        }
    }

    private fun obtenerColorRuido(nivelRuido: Double): Int {
        val context = requireContext()

        return when {
            nivelRuido < 40.0 -> ContextCompat.getColor(context, R.color.ruido_muy_bajo)
            nivelRuido < 50.0 -> ContextCompat.getColor(context, R.color.ruido_bajo)
            nivelRuido < 60.0 -> ContextCompat.getColor(context, R.color.ruido_medio)
            nivelRuido < 70.0 -> ContextCompat.getColor(context, R.color.ruido_alto)
            else -> ContextCompat.getColor(context, R.color.ruido_muy_alto)
        }
    }

    private fun formatearFecha(timestamp: Long): String {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return formato.format(Date(timestamp))
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}