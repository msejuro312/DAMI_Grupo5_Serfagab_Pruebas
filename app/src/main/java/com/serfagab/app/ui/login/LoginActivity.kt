package com.serfagab.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.serfagab.app.MainActivity
import com.serfagab.app.R
import com.serfagab.app.databinding.ActivityLoginBinding
import com.serfagab.app.remote.ApiClient
import com.serfagab.app.sync.Sincronizador
import com.serfagab.app.util.SessionStore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonIngresar.setOnClickListener {
            inciarSesion()
        }

    }

    private fun inciarSesion() {
        val login = binding.editTextUsuario.text.toString().trim()
        val clave = binding.editTextPassword.text.toString().trim()
        var formulario = true

        binding.inputLayoutUsuario.error = null
        binding.inputLayoutPassword.error = null

        if (login.isBlank()) {
            binding.inputLayoutUsuario.error = getString(R.string.error_usuario_vacio)
            formulario = false
        }
        if (clave.isBlank()) {
            binding.inputLayoutPassword.error = getString(R.string.error_password_vacio)
            formulario = false
        }
        if(!formulario) {
            return
        }

        binding.buttonIngresar.isEnabled = false

        Thread {
            val respuesta = ApiClient.login(login, clave)
            val usuario = respuesta.datos

            if (respuesta.exito && usuario != null) {
                SessionStore.guardarSesion(applicationContext, usuario, login, clave)
                Sincronizador.sincronizarCatalogos(applicationContext)
            }

            runOnUiThread {
                binding.buttonIngresar.isEnabled = true

                if (!respuesta.exito || usuario == null) {
                    Toast.makeText(this, R.string.error_credenciales, Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra(MainActivity.EXTRA_ID_USUARIO, usuario.idUsuario)
                        putExtra(MainActivity.EXTRA_ID_TIPO, usuario.tipo.idTipo)
                        putExtra(MainActivity.EXTRA_NOMBRES, usuario.nombres)
                        putExtra(MainActivity.EXTRA_APELLIDOS, usuario.apellidos)
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }.start()
    }
}
