package com.serfagab.app.ui.tipo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.serfagab.app.R
import com.serfagab.app.adapter.TipoMaterialAdapter
import com.serfagab.app.databinding.FragmentTipoMaterialBinding
import com.serfagab.app.local.TipoMaterialStore
import com.serfagab.app.model.TipoMaterial

class TipoMaterialFragment : Fragment() {

    private var _binding: FragmentTipoMaterialBinding? = null
    private val binding get() = _binding!!

    private lateinit var tipoMaterialAdapter: TipoMaterialAdapter
    private lateinit var tipoMaterialStore: TipoMaterialStore

    private var listaCompleta: List<TipoMaterial> = emptyList()
    private var filtroEstado = FILTRO_TODOS


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tipoMaterialStore = TipoMaterialStore(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTipoMaterialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tipoMaterialAdapter = TipoMaterialAdapter { tipoMaterial ->
        }

        configurarRecyclerView()
        configurarBuscador()
        configurarFiltro()
        cargarTiposMaterial()
    }

    private fun configurarRecyclerView() {
        binding.recyclerViewTipoMaterial.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewTipoMaterial.adapter = tipoMaterialAdapter
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

    private fun cargarTiposMaterial() {
        Thread {
            val tiposMaterial = tipoMaterialStore.obtenerTodos()

            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                listaCompleta = tiposMaterial
                aplicarFiltros()
            }
        }.start()
    }

    private fun aplicarFiltros() {
        val bindingActual = _binding ?: return
        val texto = bindingActual.editTextBuscar.text.toString().trim().lowercase()

        val listaFiltrada = listaCompleta.filter { tipoMaterial ->
            val coincideTexto = tipoMaterial.nombre.lowercase().contains(texto)
            val coincideEstado = when (filtroEstado) {
                FILTRO_ACTIVOS -> tipoMaterial.activo
                FILTRO_INACTIVOS -> !tipoMaterial.activo
                else -> true
            }
            coincideTexto && coincideEstado
        }

        tipoMaterialAdapter.actualizar(listaFiltrada)
        bindingActual.recyclerViewTipoMaterial.visibility =
            if (listaFiltrada.isEmpty()) View.GONE else View.VISIBLE
        bindingActual.tvVacio.visibility =
            if (listaFiltrada.isEmpty()) View.VISIBLE else View.GONE

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