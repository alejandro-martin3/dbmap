package com.alejandromartin.dbmap.model

data class AgregadoZona(
    val agregadoId: String = "",
    val zonaId: String = "",
    val periodo: String = "",
    val fechaInicio: Long = 0L,
    val fechaFin: Long = 0L,
    val nivelRuidoPromedio: Double = 0.0,
    val numeroMuestras: Int = 0
)