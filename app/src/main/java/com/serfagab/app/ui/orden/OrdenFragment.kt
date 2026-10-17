package com.serfagab.app.ui.orden

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.serfagab.app.R
import com.serfagab.app.adapter.OnOrdenAccion
import com.serfagab.app.adapter.OrdenCompraAdapter
import com.serfagab.app.databinding.FragmentOrdenBinding
import com.serfagab.app.model.OrdenCompra
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.serfagab.app.util.SessionStore


class OrdenFragment : Fragment(), OnOrdenAccion {

    private var _binding: FragmentOrdenBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OrdenCompraAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdenBinding.inflate(inflater, container,false)
        return binding.root
}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

        configurarLista()
        mostrarListaVacia()

        binding.fabNuevaOrden.visibility =
            if (puedeGestionar()) View.VISIBLE else View.GONE

        binding.fabNuevaOrden.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.ContenedorFragmento, OrdenFormularioFragment())
                .addToBackStack(null)
                .commit()
        }



    }
    private fun configurarLista(){
        adapter = OrdenCompraAdapter(
            this,
            puedeGestionar = puedeGestionar())

        binding.recyclerOrdenes.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerOrdenes.adapter = adapter
    }

    private fun puedeGestionar(): Boolean {
        return SessionStore.esAdmin(requireContext())
    }




    private fun mostrarListaVacia() {
        binding.progressIndicator.visibility = View.GONE
        binding.textViewListaVacia.visibility = View.VISIBLE
        binding.recyclerOrdenes.visibility = View.GONE
    }

    override fun onEditar(orden: OrdenCompra) {
        //Manuel : abrir el formulario para editar la orden
    }

    override fun onBorrar(orden: OrdenCompra) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_confirmar_borrar,null,false)

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setNegativeButton(R.string.boton_cancelar, null)
            .setPositiveButton(R.string.boton_confirmar) {  _, _ ->
                //MANUEL conectar OrdenStore.borrar(id).
            }
            .show()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}