package com.alejandromartin.dbmap

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.alejandromartin.dbmap.model.AgregadoZona
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon

/**
 * Fragment que representa la pantalla de mapa.
 * Muestra un mapa interactivo de OpenStreetMap con zonas geohash coloreadas
 * según el nivel medio de ruido obtenido de Firebase Firestore.
 */
class MapaFragment : Fragment(R.layout.fragment_mapa) {

    private var mapView: MapView? = null
    private val firestoreManager = FirestoreManager()
    private lateinit var configManager: ConfigManager

    /**
     * Inicializa el mapa con OpenStreetMap, configura los controles táctiles
     * y lanza la carga de datos de ruido desde Firestore.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configManager = ConfigManager(requireContext().applicationContext)

        // Necesario para osmdroid: identifica la app ante el servidor de tiles
        Configuration.getInstance().userAgentValue = requireContext().packageName

        mapView = view.findViewById(R.id.mapaRuido)

        mapView?.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)

            // Posición inicial centrada en Madrid hasta que se carguen datos reales
            controller.setZoom(12.0)
            controller.setCenter(GeoPoint(40.4168, -3.7038))
        }

        cargarDatosMapa()
    }

    /**
     * Consulta los últimos agregados de ruido en Firestore y los representa en el mapa.
     * Si la carga falla, el mapa permanece visible sin zonas coloreadas.
     */
    private fun cargarDatosMapa() {
        firestoreManager.obtenerUltimosAgregados(
            onSuccess = { agregados ->
                if (!isAdded) return@obtenerUltimosAgregados
                mostrarAgregadosEnMapa(agregados)
            },
            onError = {
                // Si falla la carga, el mapa permanece visible sin zonas coloreadas
            }
        )
    }

    /**
     * Limpia las capas del mapa y dibuja un polígono coloreado por cada zona con datos.
     * Si el centrado automático está activo, centra el mapa en la primera zona disponible.
     *
     * @param agregados Lista de agregados de ruido a representar.
     */
    private fun mostrarAgregadosEnMapa(agregados: List<AgregadoZona>) {
        val mapa = mapView ?: return

        mapa.overlays.clear()

        // Muestra solo el agregado más reciente por zona para evitar solapamientos
        val ultimosPorZona = agregados.distinctBy { it.zonaId }

        ultimosPorZona.forEach { agregado ->
            val rectangulo = crearRectanguloRuido(mapa, agregado)
            mapa.overlays.add(rectangulo)
        }

        if (ultimosPorZona.isNotEmpty() && configManager.centrarMapaAutomaticamente()) {
            val primeraZona = ZoneManager.getApproximateCenter(ultimosPorZona.first().zonaId)
            mapa.controller.setCenter(GeoPoint(primeraZona.first, primeraZona.second))
            mapa.controller.setZoom(14.0)
        }

        mapa.invalidate()
    }

    /**
     * Crea un polígono rectangular que representa una zona geohash en el mapa,
     * coloreado según el nivel medio de ruido del agregado.
     *
     * @param mapa Instancia del MapView donde se añadirá el polígono.
     * @param agregado Agregado de ruido con los datos de zona y nivel.
     * @return Polygon listo para añadir al mapa.
     */
    private fun crearRectanguloRuido(
        mapa: MapView,
        agregado: AgregadoZona
    ): Polygon {
        val bounds = ZoneManager.getBounds(agregado.zonaId)
        val color = obtenerColorZona(agregado.nivelRuidoPromedio)

        val puntos = listOf(
            GeoPoint(bounds.latMin, bounds.lonMin),
            GeoPoint(bounds.latMin, bounds.lonMax),
            GeoPoint(bounds.latMax, bounds.lonMax),
            GeoPoint(bounds.latMax, bounds.lonMin)
        )

        return Polygon(mapa).apply {
            setPoints(puntos)
            fillPaint.color = color
            fillPaint.alpha = 90
            outlinePaint.color = color
            outlinePaint.alpha = 190
            outlinePaint.strokeWidth = 2f
            title = getString(R.string.mapa_zona_titulo, agregado.zonaId)
            snippet = getString(
                R.string.mapa_zona_snippet,
                agregado.nivelRuidoPromedio,
                agregado.numeroMuestras
            )
        }
    }

    /**
     * Devuelve el color correspondiente al nivel de ruido según la escala de dBMap.
     * Verde oscuro: < 40 dB, verde claro: < 50 dB, amarillo: < 60 dB,
     * naranja: < 70 dB, rojo: >= 70 dB.
     *
     * @param nivelRuido Nivel de ruido en decibelios.
     * @return Color como entero RGB.
     */
    private fun obtenerColorZona(nivelRuido: Double): Int {
        return when {
            nivelRuido < 40.0 -> Color.rgb(46, 125, 50)
            nivelRuido < 50.0 -> Color.rgb(139, 195, 74)
            nivelRuido < 60.0 -> Color.rgb(255, 235, 59)
            nivelRuido < 70.0 -> Color.rgb(255, 152, 0)
            else -> Color.rgb(244, 67, 54)
        }
    }

    /** Reanuda el mapa al volver a la pantalla. */
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    /** Pausa el mapa al salir de la pantalla para liberar recursos. */
    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    /** Libera la referencia al mapa al destruir la vista para evitar memory leaks. */
    override fun onDestroyView() {
        super.onDestroyView()
        mapView = null
    }
}