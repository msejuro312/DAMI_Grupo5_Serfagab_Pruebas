package com.serfagab.app.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.serfagab.app.model.Usuario
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

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
    private const val CLAVE_CREDENCIAL_CIFRADA = "credencialCifrada"
    private const val CLAVE_CREDENCIAL_IV = "credencialIv"

    private const val ALIAS_CLAVE = "serfagab_sesion_key"
    private const val PROVEEDOR_KEYSTORE = "AndroidKeyStore"
    private const val ALGORITMO = "AES"
    private const val TRANSFORMACION = "AES/GCM/NoPadding"
    private const val TAMANO_TAG = 128
    private const val TAMANO_CLAVE = 256
    private const val ROL_ADMINISTRADOR = "administrador"

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
        persistirCredenciales(context, login, clave)
    }

    fun restaurarSesion(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
        if (!prefs.contains(CLAVE_ID_USUARIO) || prefs.getInt(CLAVE_ID_USUARIO, 0) <= 0) {
            cerrar(context)
            return false
        }
        if (!prefs.getBoolean(CLAVE_ACTIVO, false)) {
            cerrar(context)
            return false
        }
        val credenciales = leerCredenciales(context)
        if (credenciales == null) {
            cerrar(context)
            return false
        }
        credencialLogin = credenciales.first
        credencialClave = credenciales.second
        return true
    }

    fun haySesion(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
        return prefs.contains(CLAVE_ID_USUARIO) &&
            prefs.getInt(CLAVE_ID_USUARIO, 0) > 0 &&
            prefs.getBoolean(CLAVE_ACTIVO, false)
    }

    fun idUsuario(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
        return prefs.getInt(CLAVE_ID_USUARIO, 0)
    }

    fun esAdmin(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
        val descripcion = prefs.getString(CLAVE_DESCRIPCION_TIPO, null) ?: return false
        return descripcion.equals(ROL_ADMINISTRADOR, ignoreCase = true)
    }

    fun cerrar(context: Context) {
        val prefs = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        limpiarCredencialesMemoria()
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

    private fun persistirCredenciales(context: Context, login: String, clave: String) {
        try {
            val cipher = Cipher.getInstance(TRANSFORMACION)
            cipher.init(Cipher.ENCRYPT_MODE, obtenerClave())
            val iv = cipher.iv
            val cifrado = cipher.doFinal("$login:$clave".toByteArray(Charsets.UTF_8))
            val prefs = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(CLAVE_CREDENCIAL_CIFRADA, Base64.getEncoder().encodeToString(cifrado))
                .putString(CLAVE_CREDENCIAL_IV, Base64.getEncoder().encodeToString(iv))
                .apply()
        } catch (e: Exception) {
            val prefs = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
            prefs.edit()
                .remove(CLAVE_CREDENCIAL_CIFRADA)
                .remove(CLAVE_CREDENCIAL_IV)
                .apply()
        }
    }

    private fun leerCredenciales(context: Context): Pair<String, String>? {
        val prefs = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
        val textoCifrado = prefs.getString(CLAVE_CREDENCIAL_CIFRADA, null) ?: return null
        val textoIv = prefs.getString(CLAVE_CREDENCIAL_IV, null) ?: return null
        return try {
            val cifrado = Base64.getDecoder().decode(textoCifrado)
            val iv = Base64.getDecoder().decode(textoIv)
            val cipher = Cipher.getInstance(TRANSFORMACION)
            cipher.init(Cipher.DECRYPT_MODE, obtenerClave(), GCMParameterSpec(TAMANO_TAG, iv))
            val plano = String(cipher.doFinal(cifrado), Charsets.UTF_8)
            val separador = plano.indexOf(':')
            if (separador <= 0) {
                null
            } else {
                Pair(plano.substring(0, separador), plano.substring(separador + 1))
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun obtenerClave(): SecretKey {
        val keyStore = KeyStore.getInstance(PROVEEDOR_KEYSTORE)
        keyStore.load(null)
        val existente = keyStore.getEntry(ALIAS_CLAVE, null) as? KeyStore.SecretKeyEntry
        if (existente != null) {
            return existente.secretKey
        }
        val generador = KeyGenerator.getInstance(ALGORITMO, PROVEEDOR_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            ALIAS_CLAVE,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(TAMANO_CLAVE)
            .setRandomizedEncryptionRequired(true)
            .build()
        generador.init(spec)
        return generador.generateKey()
    }
}
