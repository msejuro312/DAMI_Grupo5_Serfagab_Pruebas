package com.serfagab.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.serfagab.app.databinding.ActivityMainBinding
import com.serfagab.app.ui.inicio.InicioFragment
import com.serfagab.app.ui.login.LoginActivity
import com.serfagab.app.ui.material.MaterialesFragment
import com.serfagab.app.ui.proveedor.ProveedoresFragment
import com.serfagab.app.ui.tipo.TipoMaterialFragment
import com.serfagab.app.util.SessionStore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        mostrarBienvenida()
        configurarMenu()

        if (savedInstanceState == null) {
            mostrarFragmento(InicioFragment())
        }

    }

    private fun mostrarBienvenida() {
        val nombres = intent.getStringExtra(EXTRA_NOMBRES)
            ?:getString(R.string.valor_no_disponible)
        val apellidos = intent.getStringExtra(EXTRA_APELLIDOS)
            ?:getString(R.string.valor_no_disponible)

        if (nombres.isNotBlank()) {
            binding.textViewUsuario.text =
                getString(R.string.bienvenida_formato, nombres, apellidos)
        }
    }

    private fun configurarMenu() {
        binding.btnMenu.setOnClickListener {
            val visible = binding.sidebarContenedor.visibility == View.VISIBLE
            binding.sidebarContenedor.visibility = if(visible) View.GONE else View.VISIBLE
        }

        binding.itemInicio.root.setOnClickListener {
            mostrarFragmento(InicioFragment())
        }
        binding.itemMateriales.root.setOnClickListener {
            mostrarFragmento(MaterialesFragment())
        }
        binding.itemTipoMaterial.root.setOnClickListener {
            mostrarFragmento(TipoMaterialFragment())
        }
        binding.itemProveedores.root.setOnClickListener {
            mostrarFragmento(ProveedoresFragment())
        }
        binding.itemOrdenCompra.root.setOnClickListener {
            Toast.makeText(this, R.string.modulo_no_disponible, Toast.LENGTH_SHORT).show()
        }

        binding.itemCerrarSesion.setOnClickListener {
            SessionStore.cerrar(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun mostrarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.ContenedorFragmento, fragment)
            .commit()
    }


    companion object {
        const val EXTRA_ID_USUARIO = "extra_id_usuario"
        const val EXTRA_ID_TIPO = "extra_id_tipo"
        const val EXTRA_NOMBRES = "extra_nombres"
        const val EXTRA_APELLIDOS = "extra_apellidos"
    }


}