package com.alejandromartin.dbmap.model

data class Zona(
    val zonaId: String = "",
    val geohashReducido: String = "",
    val nombreZona: String? = null,
    val centroLatitud: Double = 0.0,
    val centroLongitud: Double = 0.0
)