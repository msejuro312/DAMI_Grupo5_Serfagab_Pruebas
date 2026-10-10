package com.serfagab.app.model

data class Usuario(
    val idUsuario: Int = 0,
    val tipo: Tipo,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val login: String,
    val clave : String,
    val activo: Boolean = true
)