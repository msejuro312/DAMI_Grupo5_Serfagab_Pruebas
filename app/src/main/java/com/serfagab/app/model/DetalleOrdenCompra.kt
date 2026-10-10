package com.serfagab.app.model

data class DetalleOrdenCompra(
    val idDetalle: Int?,
    val material: Material?,
    val cantidad: Double?,
    val precioUnitario: Double?,
    val subtotal: Double?
)
