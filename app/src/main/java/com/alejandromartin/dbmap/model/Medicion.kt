package com.alejandromartin.dbmap.model

data class Medicion(
    val medicionId: String = "",
    val zonaId: String = "",
    val agregadoId: String? = null,
    val nivelRuido: Double = 0.0,
    val fechaHora: Long = System.currentTimeMillis()
)