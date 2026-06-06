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

class MapaFragment : Fragment(R.layout.fragment_mapa) {

    private var mapView: MapView? = null
    private val firestoreManager = FirestoreManager()
    private lateinit var configManager: ConfigManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configManager = ConfigManager(requireContext().applicationContext)
        Configuration.getInstance().userAgentValue = requireContext().packageName

        mapView = view.findViewById(R.id.mapaRuido)

        mapView?.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)

            controller.setZoom(12.0)
            controller.setCenter(GeoPoint(40.4168, -3.7038))
        }

        cargarDatosMapa()
    }

    private fun cargarDatosMapa() {
        firestoreManager.obtenerUltimosAgregados(
            onSuccess = { agregados ->
                if (!isAdded) return@obtenerUltimosAgregados
                mostrarAgregadosEnMapa(agregados)
            },
            onError = {
                // Si falla la carga, dejamos el mapa visible igualmente.
            }
        )
    }

    private fun mostrarAgregadosEnMapa(agregados: List<AgregadoZona>) {
        val mapa = mapView ?: return

        mapa.overlays.clear()

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

    private fun obtenerColorZona(nivelRuido: Double): Int {
        return when {
            nivelRuido < 50.0 -> Color.rgb(76, 175, 80)
            nivelRuido < 65.0 -> Color.rgb(255, 193, 7)
            else -> Color.rgb(244, 67, 54)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView = null
    }
}