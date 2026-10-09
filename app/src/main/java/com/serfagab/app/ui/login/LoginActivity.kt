package com.serfagab.app.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.serfagab.app.MainActivity
import com.serfagab.app.R
import com.serfagab.app.databinding.ActivityLoginBinding
import com.serfagab.app.model.Usuario
import com.serfagab.app.remote.LoginRequest
import com.serfagab.app.remote.RetrofitClient
import com.serfagab.app.util.SessionStore
import java.io.IOException
import retrofit2.Response

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
    }

    private fun iniciarSesion() {
        if (peticionEnCurso) {
            return
        }

        binding.tvMensaje.visibility = android.view.View.INVISIBLE
        val login = binding.etUsuario.text?.toString()?.trim().orEmpty()
        val clave = binding.etClave.text?.toString().orEmpty()

        if (!validarCampos(login, clave)) {
            return
        }

        peticionEnCurso = true
        mostrarEstadoCarga(true)
        SessionStore.limpiarCredencialesMemoria()

        Thread {
            try {
                val respuesta = RetrofitClient.apiService.login(LoginRequest(login, clave)).execute()
                runOnUiThread {
                    procesarRespuesta(respuesta, login, clave)
                }
            } catch (e: IOException) {
                runOnUiThread {
                    mostrarEstadoCarga(false)
                    mostrarMensaje(getString(R.string.error_conexion))
                }
            } catch (e: Exception) {
                runOnUiThread {
                    mostrarEstadoCarga(false)
                    mostrarMensaje(getString(R.string.error_respuesta_invalida))
                }
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

    private fun procesarRespuesta(respuesta: Response<Usuario>, login: String, clave: String) {
        try {
            if (respuesta.isSuccessful) {
                val usuario = respuesta.body()
                if (usuario == null) {
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
            } else if (respuesta.code() == 401) {
                mostrarMensaje(getString(R.string.error_credenciales))
            } else {
                mostrarMensaje(getString(R.string.error_generico))
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
        binding.tvMensaje.visibility = android.view.View.VISIBLE
    }

    private fun mostrarEstadoCarga(cargando: Boolean) {
        peticionEnCurso = cargando
        binding.btnIngresar.isEnabled = !cargando
        binding.progressCargando.visibility =
            if (cargando) android.view.View.VISIBLE else android.view.View.GONE
    }
}