package com.serfagab.app.ui.inicio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.serfagab.app.R
import com.serfagab.app.databinding.FragmentInicioBinding
import com.serfagab.app.local.MaterialStore
import com.serfagab.app.local.OrdenStore
import com.serfagab.app.local.ProveedorStore
import com.serfagab.app.local.TipoMaterialStore
import com.serfagab.app.util.SessionStore

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mostrarBienvenida()
        cargarConteos()
    }

    private fun mostrarBienvenida() {
        val id = SessionStore.idUsuario(requireContext().applicationContext)
        binding.textViewBienvidaInicio.text = getString(R.string.bienvenida_inicio_formato, id)
    }

    private fun cargarConteos() {
        val contexto = requireContext().applicationContext
        val idUsuario = SessionStore.idUsuario(contexto)

        Thread {
            val totalMateriales = MaterialStore(contexto).obtenerTodos().size
            val totalProveedores = ProveedorStore(contexto).obtenerTodos().size
            val totalTipos = TipoMaterialStore(contexto).obtenerTodos().size
            val totalPendientes = OrdenStore(contexto).listarPendientes().size
            // TODO E4: cuando Valerie agregue OrdenStore.listarDe(idUsuario), usarlo aquí
            val totalOrdenes = 0

            activity?.runOnUiThread {
                val bindingActual = _binding ?: return@runOnUiThread
                bindingActual.textViewCountMateriales.text = totalMateriales.toString()
                bindingActual.textViewCountProveedores.text = totalProveedores.toString()
                bindingActual.textViewCountTipoMaterial.text = totalTipos.toString()
                bindingActual.textViewCountPendientes.text = totalPendientes.toString()
                bindingActual.textViewCountOrdenes.text = totalOrdenes.toString()
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}