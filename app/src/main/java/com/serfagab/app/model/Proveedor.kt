package com.serfagab.app.model

data class Proveedor(
    val idProveedor: Int?,
    val razonSocial: String?,
    val ruc: String?,
    val celular: String?,
    val email: String?,
    val descripcion: String?,
    val activo: Boolean?
)
