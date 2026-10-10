package com.serfagab.app.model

data class OrdenCompra(
    val idOrdenCompra: Int = 0,
    val proveedor: Proveedor,
    val usuario: Usuario,
    val observaciones: String?,
    val fecha: String,
    val total: Double,
    val estado: String,
    val detalles: List<DetalleOrdenCompra>
)
