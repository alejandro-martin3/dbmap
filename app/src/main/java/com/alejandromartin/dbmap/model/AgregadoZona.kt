package com.alejandromartin.dbmap.model

/**
 * Representa un resumen de ruido agrupado por zona aproximada y franja temporal.
 * Se almacena en la colección "agregados_zona" de Firebase Firestore.
 *
 * @property agregadoId Identificador único del agregado.
 * @property zonaId Geohash reducido de la zona a la que pertenece el agregado.
 * @property periodo Etiqueta de la franja temporal usada para agrupar (ej. "15_minutos").
 * @property fechaInicio Timestamp de inicio del periodo agrupado.
 * @property fechaFin Timestamp de fin del periodo agrupado.
 * @property nivelRuidoPromedio Media del nivel de ruido en dB de las mediciones del periodo.
 * @property numeroMuestras Número de mediciones individuales incluidas en el agregado.
 */
data class AgregadoZona(
    val agregadoId: String = "",
    val zonaId: String = "",
    val periodo: String = "",
    val fechaInicio: Long = 0L,
    val fechaFin: Long = 0L,
    val nivelRuidoPromedio: Double = 0.0,
    val numeroMuestras: Int = 0
)