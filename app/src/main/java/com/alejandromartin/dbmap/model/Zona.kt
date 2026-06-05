package com.alejandromartin.dbmap.model

data class Zona(
    val geohashReducido: String = "",
    val ultimaActualizacion: Long = System.currentTimeMillis()
)