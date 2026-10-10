package com.serfagab.app.model

data class Material(
    val idMaterial: Int = 0,
    val tipoMaterial: TipoMaterial,
    val nombre: String,
    val unidadMedida: String,
    val stockActual: Double,
    val precioReferencial: Double,
    val descripcion: String?,
    val version: Int = 0,
    val activo: Boolean
)
