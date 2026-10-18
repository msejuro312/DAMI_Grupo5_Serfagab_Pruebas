package com.serfagab.app.ui.material

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.serfagab.app.R
import com.serfagab.app.adapter.MaterialesAdapter
import com.serfagab.app.databinding.DialogMaterialDetalleBinding
import com.serfagab.app.databinding.DialogMaterialFormBinding
import com.serfagab.app.databinding.FragmentMaterialesBinding
import com.serfagab.app.local.MaterialStore
import com.serfagab.app.local.TipoMaterialStore
import com.serfagab.app.model.Material
import com.serfagab.app.model.TipoMaterial
import com.serfagab.app.sync.SincronizaScheduler
import com.serfagab.app.util.SessionStore

class MaterialesFragment : Fragment() {

    private var _binding: FragmentMaterialesBinding? = null

    private val binding get() = _binding!!

    private lateinit var materialesAdapter: MaterialesAdapter
    private lateinit var materialStore: MaterialStore
    private lateinit var tipoMaterialStore: TipoMaterialStore

    private var listaCompleta: List<Material> = emptyList()
    private var tiposMaterial: List<TipoMaterial> = emptyList()
    private var filtroEstado = FILTRO_TODOS
    private var idTipoSeleccionado = TIPO_TODOS
    private var abrirFormularioPendiente = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        materialStore = MaterialStore(requireContext().applicationContext)
        tipoMaterialStore = TipoMaterialStore(requireContext().applicationContext)

        if (savedInstanceState == null) {
            abrirFormularioPendiente = arguments?.getBoolean(ARG_ABRIR_FORMULARIO, false) ?: false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMaterialesBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarRecyclerView()
        configurarBuscador()
        configurarFiltro()
        configurarBotonNuevo()
    }

    override fun onResume() {
        super.onResume()
        cargarMateriales()
    }

    private fun puedeGestionar(): Boolean = SessionStore.esAdmin(requireContext())

    private fun configurarRecyclerView() {
        materialesAdapter = MaterialesAdapter(
            puedeGestionar = puedeGestionar(),
            onMaterialClick = { material -> mostrarDetalle(material) },
            onEditarClick = { material -> mostrarFormulario(material) },
            onEstadoClick = { material -> confirmarCambioEstado(material) }
        )
        binding.recyclerViewMateriales.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMateriales.adapter = materialesAdapter
    }

    private fun configurarBotonNuevo() {
        binding.fabNuevoMaterial.visibility = if (puedeGestionar()) View.VISIBLE else View.GONE
        binding.fabNuevoMaterial.setOnClickListener { mostrarFormulario(null) }
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

    private fun cargarMateriales() {
        Thread {
            val tipos = tipoMaterialStore.obtenerTodos()

            val materiales = materialStore.obtenerTodos().sortedWith(
                compareByDescending<Material> { it.pendienteSync }
                    .thenByDescending { it.idMaterial }
            )

            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                tiposMaterial = tipos
                listaCompleta = materiales
                armarChips()
                aplicarFiltros()
                if (abrirFormularioPendiente) {
                    abrirFormularioPendiente = false
                    mostrarFormulario(null)
                }
            }
        }.start()
    }

    private fun armarChips() {
        val chipGroup = binding.chipGroupTipos
        val tiposActivos = tiposMaterial.filter { it.activo }

        if (idTipoSeleccionado != TIPO_TODOS &&
            tiposActivos.none { it.idTipoMaterial == idTipoSeleccionado }
        ) {
            idTipoSeleccionado = TIPO_TODOS
        }

        chipGroup.setOnCheckedStateChangeListener(null)
        chipGroup.removeAllViews()
        chipGroup.isSelectionRequired = true

        agregarChip(getString(R.string.chip_todos_tipos), TIPO_TODOS)
        tiposActivos.forEach { tipo -> agregarChip(tipo.nombre, tipo.idTipoMaterial) }

        chipGroup.setOnCheckedStateChangeListener { grupo, idsMarcados ->
            val chip = idsMarcados.firstOrNull()?.let { grupo.findViewById<Chip>(it) }
            idTipoSeleccionado = (chip?.tag as? Int) ?: TIPO_TODOS
            aplicarFiltros()
        }
    }

    private fun agregarChip(texto: String, idTipo: Int) {
        val chip = Chip(requireContext()).apply {
            id = View.generateViewId()
            text = texto
            tag = idTipo
            isCheckable = true
            isChecked = idTipo == idTipoSeleccionado
        }
        binding.chipGroupTipos.addView(chip)
    }

    private fun aplicarFiltros() {
        val bindingActual = _binding ?: return
        val texto = bindingActual.editTextBuscar.text.toString().trim().lowercase()

        val listaFiltrada = listaCompleta.filter { material ->
            val coincideTexto = texto.isEmpty() ||
                    material.nombre.lowercase().contains(texto) ||
                    material.idMaterial.toString() == texto ||
                    material.descripcion?.lowercase()?.contains(texto) == true
            val coincideTipo = idTipoSeleccionado == TIPO_TODOS ||
                    material.tipoMaterial.idTipoMaterial == idTipoSeleccionado
            val coincideEstado = when (filtroEstado) {
                FILTRO_ACTIVOS -> material.activo
                FILTRO_INACTIVOS -> !material.activo
                else -> true
            }
            coincideTexto && coincideTipo && coincideEstado
        }

        materialesAdapter.actualizar(listaFiltrada)
        bindingActual.recyclerViewMateriales.visibility =
            if (listaFiltrada.isEmpty()) View.GONE else View.VISIBLE
        bindingActual.tvVacio.visibility =
            if (listaFiltrada.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun mostrarDetalle(material: Material) {
        val dialogBinding = DialogMaterialDetalleBinding.inflate(layoutInflater)
        dialogBinding.textViewDetalleNombre.text = material.nombre
        dialogBinding.textViewDetalleTipo.text = material.tipoMaterial.nombre
        dialogBinding.textViewDetalleUnidad.text = material.unidadMedida
        dialogBinding.textViewDetalleStock.text =
            getString(R.string.stock_formato, material.stockActual)
        dialogBinding.textViewDetallePrecio.text =
            getString(R.string.precio_formato, material.precioReferencial)
        dialogBinding.textViewDetalleDescripcion.text =
            if (material.descripcion.isNullOrBlank()) {
                getString(R.string.sin_descripcion)
            } else {
                material.descripcion
            }
        val titulo = if (material.idMaterial < 0) {
            getString(R.string.material_nuevo_pendiente)
        } else {
            getString(R.string.codigo_formato_material, material.idMaterial)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(titulo)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.dialogo_boton_aceptar, null)
            .show()
    }

    private fun mostrarFormulario(materialEditar: Material?) {
        val idTipoActual = materialEditar?.tipoMaterial?.idTipoMaterial
        val opciones = tiposMaterial.filter { it.activo || it.idTipoMaterial == idTipoActual}
        if (opciones.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_sin_tipos_material, Toast.LENGTH_LONG).show()
            return
        }

        val dialogBinding = DialogMaterialFormBinding.inflate(layoutInflater)
        var tipoSeleccionado: TipoMaterial? =
            opciones.firstOrNull { it.idTipoMaterial == idTipoActual }

        dialogBinding.autoCompleteTipoMaterial.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                opciones.map { it.nombre }
            )
        )
        dialogBinding.autoCompleteTipoMaterial.setOnItemClickListener { _, _, posicion, _ ->
            tipoSeleccionado = opciones[posicion]
        }

        materialEditar?.let { material ->
            dialogBinding.editTextNombre.setText(material.nombre)
            dialogBinding.editTextUnidad.setText(material.unidadMedida)
            dialogBinding.editTextStock.setText(material.stockActual.toString())
            dialogBinding.editTextPrecio.setText(material.precioReferencial.toString())
            dialogBinding.editTextDescripcion.setText(material.descripcion ?: "")
            tipoSeleccionado?.let { dialogBinding.autoCompleteTipoMaterial.setText(it.nombre, false) }
        }

        val dialogo = MaterialAlertDialogBuilder(requireContext())
            .setTitle(
                if (materialEditar == null) R.string.dialogo_material_nuevo
                else R.string.dialogo_material_editar
            )
            .setView(dialogBinding.root)
            .setPositiveButton(
                if (materialEditar == null) R.string.boton_crear else R.string.boton_actualizar,
                null
            )
            .setNegativeButton(R.string.boton_cancelar, null)
            .create()

        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                guardarMaterial(dialogo, dialogBinding, materialEditar, tipoSeleccionado)
            }
        }
        dialogo.show()
    }

    private fun guardarMaterial(
        dialogo: AlertDialog,
        dialogBinding: DialogMaterialFormBinding,
        materialOriginal: Material?,
        tipo: TipoMaterial?
    ) {
        val nombre = dialogBinding.editTextNombre.text.toString().trim()
        val unidad = dialogBinding.editTextUnidad.text.toString().trim()
        val stock = dialogBinding.editTextStock.text.toString().trim().replace(',', '.').toDoubleOrNull()
        val precio = dialogBinding.editTextPrecio.text.toString().trim().replace(',', '.').toDoubleOrNull()
        val descripcion = dialogBinding.editTextDescripcion.text.toString().trim()

        dialogBinding.inputLayoutNombre.error = null
        dialogBinding.inputLayoutTipoMaterial.error = null
        dialogBinding.inputLayoutUnidad.error = null
        dialogBinding.inputLayoutStock.error = null
        dialogBinding.inputLayoutPrecio.error = null

        var formularioValido = true

        if (nombre.isBlank()) {
            dialogBinding.inputLayoutNombre.error = getString(R.string.error_material_nombre)
            formularioValido = false
        }
        if (tipo == null) {
            dialogBinding.inputLayoutTipoMaterial.error = getString(R.string.error_material_tipo)
            formularioValido = false
        }
        if (unidad.isBlank()) {
            dialogBinding.inputLayoutUnidad.error = getString(R.string.error_material_unidad)
            formularioValido = false
        }
        if (stock == null || stock <= 0) {
            dialogBinding.inputLayoutStock.error = getString(R.string.error_material_stock)
            formularioValido = false
        }
        if (precio == null || precio <=0) {
            dialogBinding.inputLayoutPrecio.error = getString(R.string.error_material_precio)
            formularioValido = false
        }
        if (!formularioValido || tipo == null || stock == null || precio == null)
            return

        if (materialOriginal != null) {
            val sinCambios = nombre == materialOriginal.nombre &&
                    unidad == materialOriginal.unidadMedida &&
                    stock == materialOriginal.stockActual &&
                    precio == materialOriginal.precioReferencial &&
                    descripcion == materialOriginal.descripcion.orEmpty() &&
                    tipo.idTipoMaterial == materialOriginal.tipoMaterial.idTipoMaterial
            if (sinCambios) {
                dialogo.dismiss()
                return
            }
        }

        val botonGuardar = dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
        botonGuardar.isEnabled = false

        Thread {
            val guardado = if (materialOriginal == null) {
                val nuevo = Material(
                    tipoMaterial = tipo,
                    nombre = nombre,
                    unidadMedida = unidad,
                    stockActual = stock,
                    precioReferencial = precio,
                    descripcion = descripcion.ifBlank { null },
                    activo = true
                )
                materialStore.crearLocal(nuevo) != -1L
            } else {
                val editado = materialOriginal.copy(
                    tipoMaterial = tipo,
                    nombre = nombre,
                    unidadMedida = unidad,
                    stockActual = stock,
                    precioReferencial = precio,
                    descripcion = descripcion.ifBlank { null },
                    pendienteSync = true
                )
                materialStore.actualizar(editado) == 1
            }

            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                if (guardado) {
                    dialogo.dismiss()
                    Toast.makeText(requireContext(), R.string.material_guardado_local, Toast.LENGTH_SHORT).show()
                    cargarMateriales()
                    programarSincronizacion()
                } else {
                    botonGuardar.isEnabled = true
                    Toast.makeText(requireContext(), R.string.error_material_guardar, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun confirmarCambioEstado(material: Material) {
        val mensaje = if (material.activo) {
            getString(R.string.confirmar_inactivar_material, material.nombre)
        } else {
            getString(R.string.confirmar_activar_material, material.nombre)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setMessage(mensaje)
            .setPositiveButton(R.string.boton_confirmar) { _, _ -> cambiarEstado(material) }
            .setNegativeButton(R.string.boton_cancelar, null)
            .show()
    }

    private fun cambiarEstado(material: Material) {
        Thread {
            val filas = if (material.activo) {
                materialStore.desactivar(material.idMaterial)
            } else {
                materialStore.activar(material.idMaterial)
            }

            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                if (filas == 1) {
                    cargarMateriales()
                    programarSincronizacion()
                } else {
                    Toast.makeText(requireContext(), R.string.error_material_guardar, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun programarSincronizacion() {
        SincronizaScheduler.programar(requireContext().applicationContext)

        binding.root.postDelayed({
            if (_binding == null) return@postDelayed
            cargarMateriales()
        }, RECARGA_TRAS_SYNC_MS)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val FILTRO_TODOS = 0
        private const val FILTRO_ACTIVOS = 1
        private const val FILTRO_INACTIVOS = 2
        private const val TIPO_TODOS = 0
        private const val RECARGA_TRAS_SYNC_MS = 7_000L
        const val ARG_ABRIR_FORMULARIO = "abrir_formulario"
    }
}