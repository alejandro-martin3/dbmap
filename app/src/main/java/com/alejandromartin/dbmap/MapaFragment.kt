package com.alejandromartin.dbmap

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.alejandromartin.dbmap.model.AgregadoZona
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapaFragment : Fragment(R.layout.fragment_mapa) {

    private var mapView: MapView? = null
    private val firestoreManager = FirestoreManager()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                // En esta primera versión dejamos el mapa cargado aunque no haya datos.
            }
        )
    }

    private fun mostrarAgregadosEnMapa(agregados: List<AgregadoZona>) {
        val mapa = mapView ?: return

        mapa.overlays.clear()

        val ultimosPorZona = agregados.distinctBy { it.zonaId }

        ultimosPorZona.forEach { agregado ->
            val centroZona = ZoneManager.getApproximateCenter(agregado.zonaId)
            val punto = GeoPoint(centroZona.first, centroZona.second)

            val marcador = Marker(mapa).apply {
                position = punto
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Zona ${agregado.zonaId}"
                snippet =
                    "Ruido medio: ${"%.1f".format(agregado.nivelRuidoPromedio)} dB\n" +
                            "Muestras: ${agregado.numeroMuestras}"
            }

            mapa.overlays.add(marcador)
        }

        if (ultimosPorZona.isNotEmpty()) {
            val primeraZona = ZoneManager.getApproximateCenter(ultimosPorZona.first().zonaId)
            mapa.controller.setCenter(GeoPoint(primeraZona.first, primeraZona.second))
            mapa.controller.setZoom(14.0)
        }

        mapa.invalidate()
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