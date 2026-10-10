package com.serfagab.app.model

data class Material(
    val idMaterial: Int?,
    val tipoMaterial: TipoMaterial?,
    val nombre: String?,
    val unidadMedida: String?,
    val stockActual: Double?,
    val precioReferencial: Double?,
    val descripcion: String?,
    val activo: Boolean?,
    val version: Int?
)
