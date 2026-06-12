package com.alejandromartin.dbmap.model

/**
 * Representa una medición de ruido individual realizada por el usuario.
 * Se almacena en la colección "mediciones" de Firebase Firestore.
 * No contiene audio, datos personales ni coordenadas exactas.
 *
 * @property medicionId Identificador único de la medición.
 * @property zonaId Geohash reducido de la zona aproximada donde se realizó la medición.
 * @property agregadoId Identificador del agregado temporal al que pertenece, si existe.
 * @property nivelRuido Nivel de ruido estimado en decibelios (dB).
 * @property fechaHora Timestamp del momento en que se realizó la medición.
 */
data class Medicion(
    val medicionId: String = "",
    val zonaId: String = "",
    val agregadoId: String? = null,
    val nivelRuido: Double = 0.0,
    val fechaHora: Long = System.currentTimeMillis()
)