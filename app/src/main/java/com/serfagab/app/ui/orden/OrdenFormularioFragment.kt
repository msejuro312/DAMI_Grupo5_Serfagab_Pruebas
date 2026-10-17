package com.serfagab.app.ui.orden

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.serfagab.app.R
import com.serfagab.app.databinding.FragmentOrdenFormularioBinding
import com.serfagab.app.local.MaterialStore
import com.serfagab.app.local.ProveedorStore
import com.serfagab.app.model.Material
import com.serfagab.app.model.Proveedor
import java.util.Locale

class OrdenFormularioFragment : Fragment() {

    private var _binding: FragmentOrdenFormularioBinding? = null
    private val binding get() = _binding!!

    private lateinit var proveedores: List<Proveedor>
    private lateinit var materiales: List<Material>

    private val materialesSeleccionados = mutableListOf<Material>()
    private val cantidades = mutableListOf<Double>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdenFormularioBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarProveedores()
        cargarMateriales()

        binding.buttonAgregarLinea.setOnClickListener {
            agregarLinea()
        }

        binding.buttonCancelarOrden.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.buttonGuardarOrden.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "El guardado se conectará durante la integración.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun cargarProveedores() {
        proveedores = ProveedorStore(requireContext())
            .obtenerTodos()
            .filter { it.activo }

        val nombres = proveedores.map { it.razonSocial }

        binding.spinnerProveedor.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            nombres
        ).also {
            it.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )
        }
    }

    private fun cargarMateriales() {
        materiales = MaterialStore(requireContext())
            .obtenerTodos()
            .filter { it.activo }

        val nombres = materiales.map { it.nombre }

        binding.spinnerMaterial.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            nombres
        ).also {
            it.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )
        }
    }

    private fun agregarLinea() {
        if (materiales.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "No hay materiales activos disponibles.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val cantidadTexto =
            binding.editTextCantidad.text.toString().trim()

        val cantidad = cantidadTexto.toDoubleOrNull()

        if (cantidad == null || cantidad <= 0) {
            binding.editTextCantidad.error =
                "Ingresa una cantidad mayor que cero"
            return
        }

        binding.editTextCantidad.error = null

        val posicion = binding.spinnerMaterial.selectedItemPosition

        if (posicion !in materiales.indices) return

        val material = materiales[posicion]

        materialesSeleccionados.add(material)
        cantidades.add(cantidad)

        val linea = LayoutInflater.from(requireContext()).inflate(
            R.layout.item_linea_orden,
            binding.contenedorLineasOrden,
            false
        )

        linea.findViewById<android.widget.TextView>(
            R.id.textViewMaterialLinea
        ).text = material.nombre

        linea.findViewById<android.widget.TextView>(
            R.id.textViewCantidadLinea
        ).text = "Cantidad: ${formatear(cantidad)} ${material.unidadMedida}"

        linea.findViewById<android.widget.TextView>(
            R.id.textViewSubtotalLinea
        ).text = "Subtotal: ${formatear(cantidad * material.precioReferencial)}"

        binding.contenedorLineasOrden.addView(linea)

        actualizarTotal()
        binding.editTextCantidad.text?.clear()
    }

    private fun actualizarTotal() {
        val total = materialesSeleccionados.indices.sumOf { indice ->
            materialesSeleccionados[indice].precioReferencial *
                    cantidades[indice]
        }

        binding.textViewTotalOrden.text =
            "Total referencial: S/ ${formatear(total)}"
    }

    private fun formatear(valor: Double): String =
        String.format(Locale.getDefault(), "%.2f", valor)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}