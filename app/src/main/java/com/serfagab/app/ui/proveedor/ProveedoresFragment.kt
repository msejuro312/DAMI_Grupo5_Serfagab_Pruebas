package com.serfagab.app.ui.proveedor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.serfagab.app.R
import com.serfagab.app.adapter.ProveedorAdapter
import com.serfagab.app.databinding.DialogProveedorDetalleBinding
import com.serfagab.app.databinding.FragmentProveedoresBinding
import com.serfagab.app.local.ProveedorStore
import com.serfagab.app.model.Proveedor

class ProveedoresFragment : Fragment() {

    private var _binding: FragmentProveedoresBinding? = null
    private val binding get() = _binding!!

    private lateinit var proveedorAdapter: ProveedorAdapter
    private lateinit var proveedorStore: ProveedorStore

    private var listaCompleta: List<Proveedor> = emptyList()
    private var filtroEstado = FILTRO_TODOS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        proveedorStore = ProveedorStore(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProveedoresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        proveedorAdapter = ProveedorAdapter { proveedor ->
            mostrarDetalle(proveedor)
        }
        configurarRecyclerView()
        configurarBuscador()
        configurarFiltro()
        cargarProveedores()
    }

    private fun configurarRecyclerView() {
        binding.recyclerViewProveedores.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewProveedores.adapter = proveedorAdapter
    }

    private fun configurarBuscador() {
        binding.editTextBuscar.doAfterTextChanged {
            aplicarFiltros()
        }
    }

    private fun configurarFiltro() {
        binding.btnFiltro.setOnClickListener {
            val menu = PopupMenu(requireContext(), binding.btnFiltro)
            menu.menu.add(0, FILTRO_TODOS, 0, R.string.filtro_todos)
            menu.menu.add(0, FILTRO_ACTIVOS, 1, R.string.filtro_activos)
            menu.menu.add(0, FILTRO_INACTIVOS, 2, R.string.filtro_inactivos)

            menu.setOnMenuItemClickListener { item ->
                filtroEstado = item.itemId
                aplicarFiltros()
                true
            }
            menu.show()
        }
    }

    private fun cargarProveedores() {
        Thread {
            val proveedores = proveedorStore.obtenerTodos()

            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                listaCompleta = proveedores
                aplicarFiltros()
            }
        }.start()
    }

    private fun aplicarFiltros() {
        val bindingActual = _binding ?: return
        val texto = bindingActual.editTextBuscar.text.toString().trim().lowercase()

        val listaFiltrada = listaCompleta.filter { proveedor ->
            val coincideTexto = proveedor.razonSocial.lowercase().contains(texto)
            val coincideEstado = when (filtroEstado) {
                FILTRO_ACTIVOS -> proveedor.activo
                FILTRO_INACTIVOS -> !proveedor.activo
                else -> true
            }
            coincideTexto && coincideEstado
        }

        proveedorAdapter.actualizar(listaFiltrada)
        bindingActual.recyclerViewProveedores.visibility =
            if (listaFiltrada.isEmpty()) View.GONE else View.VISIBLE
        bindingActual.tvVacio.visibility =
            if (listaFiltrada.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun mostrarDetalle(proveedor: Proveedor) {
        val dialogBinding = DialogProveedorDetalleBinding.inflate(layoutInflater)
        dialogBinding.textViewDetalleRazonSocial.text = proveedor.razonSocial
        dialogBinding.textViewDetalleRuc.text = proveedor.ruc
        dialogBinding.textViewDetalleCelular.text = proveedor.celular
        dialogBinding.textViewDetalleEmail.text = proveedor.email
        dialogBinding.textViewDetalleDescripcion.text =
            if (proveedor.descripcion.isNullOrBlank()) {
                getString(R.string.sin_descripcion)
            } else {
                proveedor.descripcion
            }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.codigo_formato_proveedor, proveedor.idProveedor))
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.dialogo_boton_aceptar, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val FILTRO_TODOS = 0
        private const val FILTRO_ACTIVOS = 1
        private const val FILTRO_INACTIVOS = 2
    }
}