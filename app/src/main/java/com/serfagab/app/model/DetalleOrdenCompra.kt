package com.serfagab.app.model

data class DetalleOrdenCompra(
    val idDetalle: Int = 0,
    val material: Material,
    val ordenCompra: OrdenCompra,
    val cantidad: Double,
    val precioUnitario: Double,
    val subtotal: Double
)
