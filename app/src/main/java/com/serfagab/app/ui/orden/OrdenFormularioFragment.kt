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
import com.serfagab.app.local.OrdenStore
import com.serfagab.app.local.ProveedorStore
import com.serfagab.app.model.DetalleOrdenCompra
import com.serfagab.app.model.Material
import com.serfagab.app.model.OrdenCompra
import com.serfagab.app.model.Proveedor
import com.serfagab.app.util.SessionStore
import java.time.LocalDate
import java.util.Locale

class OrdenFormularioFragment : Fragment() {

    companion object {
        const val ARG_ID_ORDEN = "id_orden_editar"
    }

    private var _binding: FragmentOrdenFormularioBinding? = null
    private val binding get() = _binding!!

    private lateinit var proveedores: List<Proveedor>
    private lateinit var materiales: List<Material>

    private val materialesSeleccionados = mutableListOf<Material>()
    private val cantidades = mutableListOf<Double>()

    private var ordenEditando: OrdenCompra? = null

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

        val idOrden = arguments?.getInt(ARG_ID_ORDEN, -1) ?: -1
        if (idOrden != -1) {
            cargarOrdenParaEditar(idOrden)
        }

        binding.buttonAgregarLinea.setOnClickListener {
            agregarLinea()
        }

        binding.buttonCancelarOrden.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.buttonGuardarOrden.setOnClickListener {
            guardar()
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

    private fun cargarOrdenParaEditar(idOrden: Int) {
        val contexto = requireContext().applicationContext

        Thread {
            val orden = OrdenStore(contexto).obtenerPorId(idOrden)

            if (_binding == null) return@Thread

            activity?.runOnUiThread {
                if (orden == null) {
                    Toast.makeText(
                        requireContext(),
                        "No se pudo cargar la orden",
                        Toast.LENGTH_SHORT
                    ).show()
                    parentFragmentManager.popBackStack()
                    return@runOnUiThread
                }

                ordenEditando = orden
                binding.textViewTituloFormulario.text =
                    getString(R.string.orden_editar)

                val posicion = proveedores.indexOfFirst {
                    it.idProveedor == orden.proveedor.idProveedor
                }
                if (posicion >= 0) {
                    binding.spinnerProveedor.setSelection(posicion)
                }

                binding.editTextObservaciones.setText(orden.observaciones ?: "")

                orden.detalles.forEach { detalle ->
                    materialesSeleccionados.add(detalle.material)
                    cantidades.add(detalle.cantidad)
                    mostrarLineaEnContenedor(detalle.material, detalle.cantidad)
                }

                actualizarTotal()
            }
        }.start()
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

        mostrarLineaEnContenedor(material, cantidad)

        actualizarTotal()
        binding.editTextCantidad.text?.clear()
    }

    private fun mostrarLineaEnContenedor(material: Material, cantidad: Double) {
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
    }

    private fun guardar() {
        if (materialesSeleccionados.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Agrega al menos un material a la orden",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val posicionProveedor = binding.spinnerProveedor.selectedItemPosition
        if (posicionProveedor !in proveedores.indices) return

        val proveedor = proveedores[posicionProveedor]
        val observacionesTexto =
            binding.editTextObservaciones.text.toString().trim()
        val observaciones = observacionesTexto.ifEmpty { null }
        val contexto = requireContext().applicationContext
        val ordenAnterior = ordenEditando

        binding.buttonGuardarOrden.isEnabled = false

        Thread {
            val usuario = ordenAnterior?.usuario
                ?: OrdenStore(contexto)
                    .obtenerUsuario(SessionStore.idUsuario(contexto))

            if (usuario == null) {
                if (_binding == null) return@Thread
                activity?.runOnUiThread {
                    binding.buttonGuardarOrden.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        "No se encontró el usuario de sesión",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@Thread
            }

            val total = materialesSeleccionados.indices.sumOf { indice ->
                materialesSeleccionados[indice].precioReferencial *
                        cantidades[indice]
            }

            val ordenBase = OrdenCompra(
                idOrdenCompra = ordenAnterior?.idOrdenCompra ?: 0,
                proveedor = proveedor,
                usuario = usuario,
                observaciones = observaciones,
                fecha = ordenAnterior?.fecha ?: LocalDate.now().toString(),
                total = total,
                estado = ordenAnterior?.estado ?: "PENDIENTE",
                detalles = emptyList()
            )

            val detalles = materialesSeleccionados.indices.map { indice ->
                val material = materialesSeleccionados[indice]
                val cantidad = cantidades[indice]
                DetalleOrdenCompra(
                    material = material,
                    ordenCompra = ordenBase,
                    cantidad = cantidad,
                    precioUnitario = material.precioReferencial,
                    subtotal = material.precioReferencial * cantidad
                )
            }

            val ordenFinal = ordenBase.copy(detalles = detalles)
            val store = OrdenStore(contexto)

            if (ordenAnterior != null) {
                store.actualizar(ordenFinal)
            } else {
                store.insertarOrden(ordenFinal)
            }

            if (_binding == null) return@Thread
            activity?.runOnUiThread {
                Toast.makeText(
                    requireContext(),
                    if (ordenAnterior != null) "Orden actualizada"
                    else "Orden creada",
                    Toast.LENGTH_SHORT
                ).show()
                parentFragmentManager.popBackStack()
            }
        }.start()
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