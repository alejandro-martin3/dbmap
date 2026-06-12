package com.alejandromartin.dbmap.model

/**
 * Representa una zona aproximada del mapa identificada por su geohash reducido.
 * Se almacena en la colección "zonas" de Firebase Firestore.
 * No contiene coordenadas exactas ni datos personales.
 *
 * @property geohashReducido Código geohash de precisión 6 que identifica la zona aproximada.
 * @property ultimaActualizacion Timestamp de la última medición registrada en esta zona.
 */
data class Zona(
    val geohashReducido: String = "",
    val ultimaActualizacion: Long = System.currentTimeMillis()
)