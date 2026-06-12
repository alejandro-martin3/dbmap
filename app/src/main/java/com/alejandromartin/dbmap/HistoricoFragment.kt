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

/**
 * Fragment que representa la pantalla de histórico.
 * Consulta los últimos agregados de ruido almacenados en Firebase Firestore
 * y los muestra como tarjetas ordenadas cronológicamente.
 */
class HistoricoFragment : Fragment(R.layout.fragment_historico) {

    private lateinit var contenedorHistorico: LinearLayout
    private val firestoreManager = FirestoreManager()

    /**
     * Inicializa el contenedor de tarjetas, el botón de actualización
     * y lanza la primera carga del histórico al abrir la pantalla.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contenedorHistorico = view.findViewById(R.id.layoutHistoricoContenido)
        val botonActualizar = view.findViewById<Button>(R.id.botonActualizarHistorico)

        botonActualizar.setOnClickListener {
            cargarHistorico()
        }

        cargarHistorico()
    }

    /**
     * Consulta los últimos agregados en Firestore y actualiza la vista.
     * Muestra un aviso si no hay conexión a internet.
     */
    private fun cargarHistorico() {
        mostrarMensaje(getString(R.string.historico_cargando))

        if (!AppUtils.hayConexion(requireContext())) {
            mostrarMensaje(getString(R.string.sin_conexion_historico))
            return
        }

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

    /**
     * Limpia el contenedor y muestra un mensaje de texto informativo.
     *
     * @param mensaje Texto a mostrar en el contenedor.
     */
    private fun mostrarMensaje(mensaje: String) {
        contenedorHistorico.removeAllViews()

        val textView = TextView(requireContext()).apply {
            text = mensaje
            textSize = 16f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.dbmap_text_secondary))
        }

        contenedorHistorico.addView(textView)
    }

    /**
     * Limpia el contenedor y genera una tarjeta por cada agregado recibido.
     *
     * @param agregados Lista de agregados de ruido a mostrar.
     */
    private fun mostrarAgregados(agregados: List<AgregadoZona>) {
        contenedorHistorico.removeAllViews()

        agregados.forEach { agregado ->
            contenedorHistorico.addView(crearTarjetaAgregado(agregado))
        }
    }

    /**
     * Crea y devuelve una tarjeta visual con los datos de un agregado de ruido.
     * Muestra zona, nivel medio de ruido, periodo, fecha de inicio y número de muestras.
     *
     * @param agregado Agregado de ruido a representar.
     * @return MaterialCardView con el contenido del agregado.
     */
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

        // El color del nivel de ruido varía según el rango de decibelios
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

    /**
     * Crea un TextView con estilo secundario para mostrar datos de detalle en las tarjetas.
     *
     * @param texto Texto a mostrar.
     * @return TextView con el estilo aplicado.
     */
    private fun crearTextoSecundario(texto: String): TextView {
        return TextView(requireContext()).apply {
            text = texto
            textSize = 14f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.dbmap_text_secondary))
            setPadding(0, dp(4), 0, 0)
        }
    }

    /**
     * Devuelve el color correspondiente al nivel de ruido según la escala de dBMap.
     *
     * @param nivelRuido Nivel de ruido en decibelios.
     * @return Color como entero resuelto desde los recursos.
     */
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

    /**
     * Convierte un timestamp en una cadena de fecha legible con formato dd/MM/yyyy HH:mm.
     *
     * @param timestamp Timestamp en milisegundos.
     * @return Fecha formateada como cadena de texto.
     */
    private fun formatearFecha(timestamp: Long): String {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return formato.format(Date(timestamp))
    }

    /**
     * Convierte un valor en dp a píxeles según la densidad de pantalla del dispositivo.
     *
     * @param value Valor en dp.
     * @return Valor equivalente en píxeles.
     */
    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}