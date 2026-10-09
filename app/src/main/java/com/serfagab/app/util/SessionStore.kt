package com.serfagab.app.util

import android.content.Context
import com.serfagab.app.model.Usuario
import java.util.Base64

object SessionStore {

    private const val PREFERENCIAS = "serfagab_sesion"
    private const val CLAVE_ID_USUARIO = "idUsuario"
    private const val CLAVE_NOMBRES = "nombres"
    private const val CLAVE_APELLIDOS = "apellidos"
    private const val CLAVE_LOGIN = "login"
    private const val CLAVE_EMAIL = "email"
    private const val CLAVE_ACTIVO = "activo"
    private const val CLAVE_ID_TIPO = "idTipo"
    private const val CLAVE_DESCRIPCION_TIPO = "descripcionTipo"

    private var credencialLogin: String? = null
    private var credencialClave: String? = null

    fun guardarSesion(context: Context, usuario: Usuario, login: String, clave: String) {
        val prefs = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(CLAVE_ID_USUARIO, usuario.idUsuario ?: 0)
            .putString(CLAVE_NOMBRES, usuario.nombres)
            .putString(CLAVE_APELLIDOS, usuario.apellidos)
            .putString(CLAVE_LOGIN, usuario.login ?: login)
            .putString(CLAVE_EMAIL, usuario.email)
            .putBoolean(CLAVE_ACTIVO, usuario.activo == true)
            .putInt(CLAVE_ID_TIPO, usuario.tipo?.idTipo ?: 0)
            .putString(CLAVE_DESCRIPCION_TIPO, usuario.tipo?.descripcion)
            .apply()
        credencialLogin = login
        credencialClave = clave
    }

    fun limpiarCredencialesMemoria() {
        credencialLogin = null
        credencialClave = null
    }

    fun credencialesDisponibles(): Boolean {
        return credencialLogin != null && credencialClave != null
    }

    fun obtenerCabeceraBasicAuth(): String? {
        val login = credencialLogin ?: return null
        val clave = credencialClave ?: return null
        val texto = "$login:$clave"
        val codificado = Base64.getEncoder().encodeToString(texto.toByteArray(Charsets.UTF_8))
        return "Basic $codificado"
    }
}