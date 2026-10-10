package com.serfagab.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.serfagab.app.databinding.ActivityMainBinding
import com.serfagab.app.ui.inicio.InicioFragment
import com.serfagab.app.ui.material.MaterialFragment
import com.serfagab.app.ui.orden.OrdenFragment
import com.serfagab.app.ui.proveedor.ProveedorFragment
import com.serfagab.app.ui.tipo.TipoMaterialFragment
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnInicio.setOnClickListener { irA(INICIO) }
        binding.btnMateriales.setOnClickListener { irA(MATERIALES) }
        binding.btnTipos.setOnClickListener { irA(TIPOS) }
        binding.btnProveedores.setOnClickListener { irA(PROVEEDORES) }
        binding.btnOrdenes.setOnClickListener { irA(ORDENES) }

        if (savedInstanceState == null){
            irA(INICIO)
        }


    }

    private fun irA(destino: Int){
        val fragment: Fragment = when(destino){
            MATERIALES -> MaterialFragment()
            TIPOS -> TipoMaterialFragment()
            PROVEEDORES -> ProveedorFragment()
            ORDENES -> OrdenFragment()
            else -> InicioFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        resaltarBoton(destino)
        binding.toolbar.title = tituloDe(destino)
    }

    private fun resaltarBoton(destino: Int){
        val botones = mapOf(
            INICIO to binding.btnInicio,
            MATERIALES to binding.btnMateriales,
            TIPOS to binding.btnTipos,
            PROVEEDORES to binding.btnProveedores,
            ORDENES to binding.btnOrdenes
        )
        botones.forEach { (id, boton) ->
            val activo = id == destino
            boton.setBackgroundColor(getColor(if (activo) R.color.boton_activo_fondo else R.color.boton_destino_fondo))
            boton.setTextColor(getColor(if (activo) R.color.boton_activo_texto else R.color.boton_destino_texto))
            boton.setTypeface(null, if (activo) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        }
    }

    private fun tituloDe(destino: Int): String = when(destino) {
        MATERIALES -> getString(R.string.boton_materiales)
        TIPOS -> getString(R.string.boton_tipos)
        PROVEEDORES -> getString(R.string.boton_proveedores)
        ORDENES -> getString(R.string.boton_ordenes)
        else -> getString(R.string.boton_inicio)
    }

    companion object {
        const val INICIO = 0
        const val MATERIALES = 1
        const val TIPOS = 2
        const val PROVEEDORES = 3
        const val ORDENES = 4
    }
}