package com.serfagab.app.model

data class Usuario(
    val idUsuario: Int?,
    val nombres: String?,
    val apellidos: String?,
    val login: String?,
    val email: String?,
    val activo: Boolean?,
    val tipo: Tipo?
)