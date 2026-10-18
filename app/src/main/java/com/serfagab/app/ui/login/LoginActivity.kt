package com.serfagab.app.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.serfagab.app.MainActivity
import com.serfagab.app.R
import com.serfagab.app.databinding.ActivityLoginBinding
import com.serfagab.app.model.Tipo
import com.serfagab.app.model.Usuario
import com.serfagab.app.remote.ApiClient
import com.serfagab.app.sync.SincronizaScheduler
import com.serfagab.app.sync.SyncManager
import com.serfagab.app.util.SessionStore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private var autenticando = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (SessionStore.haySesion(this)) {
            val usuario = SessionStore.datosUsuario(this)
            if (usuario != null && SessionStore.restaurarSesion(this)) {
                abrirPrincipal(usuario)
                return
            }
        }

        configurarGoogleSignIn()

        binding.buttonIngresar.setOnClickListener {
            inciarSesion()
        }

        binding.buttonGoogle.setOnClickListener {
            iniciarSesionGoogle()
        }

    }

    private fun configurarGoogleSignIn() {
        val opciones = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, opciones)
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { resultado ->
            procesarResultadoGoogle(resultado)
        }
    }

    private fun inciarSesion() {
        if (autenticando) {
            return
        }
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
        if (!formulario) {
            return
        }

        bloquearBotones(true)

        Thread {
            try {
                val respuesta = ApiClient.login(login, clave)
                val usuario = respuesta.datos

                if (respuesta.exito && usuario != null) {
                    SessionStore.guardarSesion(applicationContext, usuario, login, clave)
                    SincronizaScheduler.programarPeriodico(applicationContext)
                    try {
                        val sincronizado = SyncManager.sincronizarCatalogos(applicationContext)
                        if (!sincronizado) {
                            SincronizaScheduler.programar(applicationContext)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("Login", "Error al sincronizar", e)
                        SincronizaScheduler.programar(applicationContext)
                    }
                }
                runOnUiThread {
                    bloquearBotones(false)

                    if (!respuesta.exito || usuario == null) {
                        Toast.makeText(this, respuesta.mensaje, Toast.LENGTH_SHORT).show()
                    } else {
                        abrirPrincipal(usuario)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("Login", "Error en login", e)
                runOnUiThread {
                    bloquearBotones(false)
                    Toast.makeText(this, getString(R.string.error_formato, e.message.orEmpty()), Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun iniciarSesionGoogle() {
        if (autenticando) {
            return
        }
        bloquearBotones(true)
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private fun procesarResultadoGoogle(resultado: ActivityResult) {
        if (resultado.resultCode != Activity.RESULT_OK) {
            finalizarGoogleConMensaje(R.string.google_login_cancelado)
            return
        }
        try {
            val cuenta = GoogleSignIn.getSignedInAccountFromIntent(resultado.data)
                .getResult(ApiException::class.java)
            val idToken = cuenta.idToken
            if (idToken.isNullOrBlank()) {
                finalizarGoogleConMensaje(R.string.google_login_sin_token)
                return
            }
            autenticarConFirebase(cuenta, idToken)
        } catch (e: ApiException) {
            if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                finalizarGoogleConMensaje(R.string.google_login_cancelado)
            } else {
                finalizarGoogleConMensaje(R.string.google_login_error)
            }
        } catch (e: Exception) {
            finalizarGoogleConMensaje(R.string.google_login_error)
        }
    }

    private fun autenticarConFirebase(cuenta: GoogleSignInAccount, idToken: String) {
        val credencial = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credencial)
            .addOnSuccessListener { resultado ->
                val usuarioFirebase = resultado.user
                if (usuarioFirebase == null) {
                    finalizarGoogleConMensaje(R.string.google_login_error)
                } else {
                    guardarSesionGoogle(usuarioFirebase, cuenta)
                }
            }
            .addOnFailureListener {
                finalizarGoogleConMensaje(R.string.google_login_error)
            }
    }

    private fun guardarSesionGoogle(usuarioFirebase: FirebaseUser, cuenta: GoogleSignInAccount) {
        try {
            val email = usuarioFirebase.email ?: cuenta.email ?: ""
            val nombreCompleto = (usuarioFirebase.displayName ?: cuenta.displayName ?: "").trim()
            val partes = nombreCompleto.split(" ").filter { it.isNotBlank() }
            val nombres = partes.firstOrNull() ?: email.substringBefore('@')
            val apellidos = if (partes.size > 1) partes.drop(1).joinToString(" ") else ""

            val usuario = Usuario(
                idUsuario = SessionStore.ID_USUARIO_GOOGLE,
                tipo = Tipo(idTipo = 0, descripcion = TIPO_USUARIO_GOOGLE),
                nombres = nombres,
                apellidos = apellidos,
                email = email,
                login = email.ifBlank { usuarioFirebase.uid },
                clave = "",
                activo = true
            )
            SincronizaScheduler.cancelarTodo(applicationContext)
            SessionStore.guardarSesion(applicationContext, usuario, usuario.login, "")
            abrirPrincipal(usuario)
        } catch (e: Exception) {
            android.util.Log.e("Login", "Error al guardar la sesion de Google", e)
            finalizarGoogleConMensaje(R.string.google_login_error_guardar)
        }
    }

    private fun abrirPrincipal(usuario: Usuario) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_ID_USUARIO, usuario.idUsuario)
            putExtra(MainActivity.EXTRA_ID_TIPO, usuario.tipo.idTipo)
            putExtra(MainActivity.EXTRA_NOMBRES, usuario.nombres)
            putExtra(MainActivity.EXTRA_APELLIDOS, usuario.apellidos)
        }
        startActivity(intent)
        finish()
    }

    private fun finalizarGoogleConMensaje(@StringRes mensaje: Int) {
        bloquearBotones(false)
        Toast.makeText(this, getString(mensaje), Toast.LENGTH_SHORT).show()
    }

    private fun bloquearBotones(bloquear: Boolean) {
        autenticando = bloquear
        binding.buttonGoogle.isEnabled = !bloquear
        binding.buttonIngresar.isEnabled = !bloquear
    }

    companion object {
        private const val TIPO_USUARIO_GOOGLE = "usuario"
    }
}
