package com.serfagab.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.serfagab.app.MainActivity
import com.serfagab.app.R
import com.serfagab.app.databinding.ActivityLoginBinding
import com.serfagab.app.model.Usuario
import com.serfagab.app.remote.ApiClient
import com.serfagab.app.remote.Respuesta
import com.serfagab.app.util.SessionStore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var peticionEnCurso = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLogin) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.btnIngresar.setOnClickListener { iniciarSesion() }
        restaurarSesionSiExiste()
    }

    private fun restaurarSesionSiExiste() {
        if (SessionStore.restaurarSesion(this)) {
            abrirMainActivity()
        }
    }

    private fun iniciarSesion() {
        if (peticionEnCurso) {
            return
        }

        binding.tvMensaje.visibility = View.INVISIBLE
        val login = binding.etUsuario.text?.toString()?.trim().orEmpty()
        val clave = binding.etClave.text?.toString().orEmpty()

        if (!validarCampos(login, clave)) {
            return
        }

        peticionEnCurso = true
        mostrarEstadoCarga(true)
        SessionStore.limpiarCredencialesMemoria()

        Thread {
            val respuesta = try {
                ApiClient.login(login, clave)
            } catch (e: Exception) {
                Respuesta<Usuario>(
                    false,
                    null,
                    ApiClient.CODIGO_ERROR_RESPUESTA,
                    getString(R.string.error_respuesta_invalida)
                )
            }
            runOnUiThread {
                procesarRespuesta(respuesta, login, clave)
            }
        }.start()
    }

    private fun validarCampos(login: String, clave: String): Boolean {
        var valido = true
        if (login.isEmpty()) {
            binding.tilUsuario.error = getString(R.string.error_campo_usuario)
            valido = false
        } else {
            binding.tilUsuario.error = null
        }
        if (clave.isEmpty()) {
            binding.tilClave.error = getString(R.string.error_campo_clave)
            valido = false
        } else {
            binding.tilClave.error = null
        }
        return valido
    }

    private fun procesarRespuesta(respuesta: Respuesta<Usuario>, login: String, clave: String) {
        try {
            if (respuesta.exito) {
                val usuario = respuesta.datos
                if (usuario == null || usuario.idUsuario == null || usuario.idUsuario <= 0) {
                    mostrarMensaje(getString(R.string.error_respuesta_invalida))
                } else if (usuario.activo != true) {
                    val mensaje = if (usuario.activo == false) {
                        getString(R.string.error_usuario_inactivo)
                    } else {
                        getString(R.string.error_respuesta_invalida)
                    }
                    mostrarMensaje(mensaje)
                } else {
                    SessionStore.guardarSesion(this, usuario, login, clave)
                    abrirMainActivity()
                }
            } else {
                when (respuesta.codigo) {
                    401 -> mostrarMensaje(getString(R.string.error_credenciales))
                    ApiClient.CODIGO_ERROR_CONEXION -> mostrarMensaje(getString(R.string.error_conexion))
                    ApiClient.CODIGO_ERROR_RESPUESTA -> mostrarMensaje(getString(R.string.error_respuesta_invalida))
                    else -> mostrarMensaje(getString(R.string.error_generico))
                }
            }
        } catch (e: Exception) {
            mostrarMensaje(getString(R.string.error_respuesta_invalida))
        } finally {
            mostrarEstadoCarga(false)
        }
    }

    private fun abrirMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun mostrarMensaje(mensaje: String) {
        binding.tvMensaje.text = mensaje
        binding.tvMensaje.visibility = View.VISIBLE
    }

    private fun mostrarEstadoCarga(cargando: Boolean) {
        peticionEnCurso = cargando
        binding.btnIngresar.isEnabled = !cargando
        binding.progressCargando.visibility =
            if (cargando) View.VISIBLE else View.GONE
    }
}
