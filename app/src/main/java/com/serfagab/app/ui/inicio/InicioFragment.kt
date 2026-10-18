package com.serfagab.app.ui.inicio

import android.app.job.JobScheduler
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.serfagab.app.R
import com.serfagab.app.databinding.FragmentInicioBinding
import com.serfagab.app.local.MaterialStore
import com.serfagab.app.local.OrdenStore
import com.serfagab.app.local.ProveedorStore
import com.serfagab.app.local.TipoMaterialStore
import com.serfagab.app.sync.SincronizaScheduler
import com.serfagab.app.ui.material.MaterialesFragment
import com.serfagab.app.ui.orden.OrdenFormularioFragment
import com.serfagab.app.util.SessionStore
import java.text.DateFormat
import java.util.Date

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mostrarBienvenida()
        cargarConteos()
        configurarAccesosRapidos()

        mostrarEstadoSincronizacion()

        binding.buttonSincronizar.setOnClickListener {
            programarSincronizacion()
        }
    }

    private fun configurarAccesosRapidos() {
        binding.accesoNuevaOrden.setOnClickListener {
            if (!SessionStore.esAdmin(requireContext())) {
                Toast.makeText(requireContext(), R.string.solo_administrador, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.ContenedorFragmento, OrdenFormularioFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.accesoNuevaMaterial.setOnClickListener {
            if (!SessionStore.esAdmin(requireContext())) {
                Toast.makeText(requireContext(), R.string.solo_administrador, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val fragment = MaterialesFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(MaterialesFragment.ARG_ABRIR_FORMULARIO, true)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.ContenedorFragmento, fragment)
                .addToBackStack(null)
                .commit()
        }
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
            val totalOrdenes = OrdenStore(contexto).listarDe(idUsuario).size

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

    private fun programarSincronizacion() {
        val resultado = SincronizaScheduler.programar(requireContext())
        val mensaje = if (resultado == JobScheduler.RESULT_SUCCESS) {
            R.string.sincronizacion_programada
        } else {
            R.string.sincronizacion_no_programada
        }
        binding.textViewEstadoSincronizacion.setText(mensaje)
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

        binding.root.postDelayed({ mostrarEstadoSincronizacion() }, COMPROBACION_SINCRONIZACION_MS)
    }

    private fun mostrarEstadoSincronizacion() {
        val bindingActual = _binding ?: return
        val preferencias = requireContext().getSharedPreferences(
            SincronizaScheduler.PREFERENCIAS_SINCRONIZACION,
            Context.MODE_PRIVATE
        )
        val ultimaSincronizacion = preferencias.getLong(
            SincronizaScheduler.CLAVE_ULTIMA_SINCRONIZACION,
            SIN_SINCRONIZACION
        )
        if (ultimaSincronizacion != SIN_SINCRONIZACION) {
            val fecha = DateFormat.getDateTimeInstance().format(Date(ultimaSincronizacion))
            bindingActual.textViewEstadoSincronizacion.text =
                getString(R.string.sincronizacion_ultima_formato, fecha)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val COMPROBACION_SINCRONIZACION_MS = 8_000L
        private const val SIN_SINCRONIZACION = 0L
    }
}