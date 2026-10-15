package com.serfagab.app.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.serfagab.app.R
import com.serfagab.app.databinding.ItemMaterialBinding
import com.serfagab.app.model.Material

class MaterialesAdapter(
    private val puedeGestionar: Boolean,
    private val onMaterialClick: (Material) -> Unit,
    private val onEditarClick: (Material) -> Unit,
    private val onEstadoClick: (Material) -> Unit
) : RecyclerView.Adapter<MaterialesAdapter.MaterialViewHolder>() {

    private val materiales = mutableListOf<Material>()

    fun actualizar(nuevosMateriales: List<Material>) {
        val cantidadAnterior = materiales.size
        materiales.clear()
        if (cantidadAnterior > 0) {
            notifyItemRangeRemoved(0, cantidadAnterior)
        }

        materiales.addAll(nuevosMateriales)
        if (materiales.isNotEmpty()) {
            notifyItemRangeInserted(0, materiales.size)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MaterialesAdapter.MaterialViewHolder {
        val binding = ItemMaterialBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MaterialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MaterialesAdapter.MaterialViewHolder, position: Int) {
        holder.vincular(materiales[position])
    }

    override fun getItemCount(): Int {
        return materiales.size
    }

    inner class MaterialViewHolder(
        private val binding: ItemMaterialBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun vincular(material: Material) {
            val context = binding.root.context

            binding.textViewId.text = when {
                material.idMaterial < 0 -> context.getString(R.string.material_nuevo_pendiente)
                material.pendienteSync ->
                    context.getString(R.string.material_pendiente_formato, material.idMaterial)
                else -> context.getString(R.string.codigo_formato_material, material.idMaterial)
            }
            binding.textViewId.setTextColor(
                context.getColor(if (material.pendienteSync) R.color.naranja else R.color.color_header_2)
            )

            binding.textViewNombre.text = material.nombre
            binding.textViewUnidad.text = material.unidadMedida
            binding.textViewStock.text =
                context.getString(R.string.stock_formato, material.stockActual)
            binding.textViewPrecio.text =
                context.getString(R.string.precio_formato, material.precioReferencial)
            binding.textViewTipo.text = material.tipoMaterial.nombre

            if (material.activo) {
                binding.textViewEstado.text = context.getString(R.string.estado_activo)
                binding.textViewEstado.setBackgroundResource(R.drawable.bg_badge_activo)
                binding.textViewEstado.setTextColor(context.getColor(R.color.color_verde))
                binding.btnEliminarActivar.setImageResource(R.drawable.ic_delete)
                binding.btnEliminarActivar.imageTintList =
                    ColorStateList.valueOf(context.getColor(R.color.color_rojo))
            } else {
                binding.textViewEstado.text = context.getString(R.string.estado_inactivo)
                binding.textViewEstado.setBackgroundResource(R.drawable.bg_badge_gris)
                binding.textViewEstado.setTextColor(context.getColor(R.color.color_rojo))
                binding.btnEliminarActivar.setImageResource(R.drawable.ic_activar)
                binding.btnEliminarActivar.imageTintList =
                    ColorStateList.valueOf(context.getColor(R.color.color_verde))
            }

            val visibilidad = if (puedeGestionar) View.VISIBLE else View.GONE
            binding.btnEditar.visibility = visibilidad
            binding.btnEliminarActivar.visibility = visibilidad

            binding.root.setOnClickListener { onMaterialClick(material) }
            binding.btnEditar.setOnClickListener { onEditarClick(material) }
            binding.btnEliminarActivar.setOnClickListener { onEstadoClick(material) }
        }
    }
}