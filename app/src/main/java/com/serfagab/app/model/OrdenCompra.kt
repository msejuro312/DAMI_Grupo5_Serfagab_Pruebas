package com.serfagab.app.model

data class OrdenCompra(
    val idOrdenCompra: Int?,
    val proveedor: Proveedor?,
    val usuario: Usuario?,
    val fecha: String?,
    val estado: String?,
    val total: Double?,
    val observaciones: String?,
    val detalles: List<DetalleOrdenCompra>?
)
