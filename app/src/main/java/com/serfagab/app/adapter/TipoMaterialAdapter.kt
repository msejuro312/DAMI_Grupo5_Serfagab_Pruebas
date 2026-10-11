package com.serfagab.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.serfagab.app.databinding.ItemTipoMaterialBinding
import com.serfagab.app.model.TipoMaterial
import com.serfagab.app.R

class TipoMaterialAdapter(
    private val onTipoMaterialClick: (TipoMaterial) -> Unit
) : RecyclerView.Adapter<TipoMaterialAdapter.TipoMaterialViewHolder>() {

    private val tiposMaterial = mutableListOf<TipoMaterial>()

    fun actualizar(nuevosTipos: List<TipoMaterial>) {
        val cantidadAnterior = tiposMaterial.size
        tiposMaterial.clear()
        if (cantidadAnterior > 0) {
            notifyItemRangeRemoved(0, cantidadAnterior)
        }
        tiposMaterial.addAll(nuevosTipos)
        if (tiposMaterial.isNotEmpty()) {
            notifyItemRangeInserted(0 , tiposMaterial.size)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TipoMaterialAdapter.TipoMaterialViewHolder {
        val binding = ItemTipoMaterialBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TipoMaterialViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TipoMaterialAdapter.TipoMaterialViewHolder,
        position: Int
    ) {
        holder.vincular(tiposMaterial[position])
    }

    override fun getItemCount(): Int {
        return tiposMaterial.size
    }

    inner class TipoMaterialViewHolder(private val binding: ItemTipoMaterialBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun vincular(tipoMaterial: TipoMaterial) {
            val context = binding.root.context

            binding.textViewId.text =
                context.getString(R.string.codigo_formato_tipomaterial, tipoMaterial.idTipoMaterial)
            binding.textViewNombre.text = tipoMaterial.nombre
            binding.textViewDescripcion.text =
                if (tipoMaterial.descripcion.isNullOrBlank()) {
                    context.getString(R.string.sin_descripcion)
                } else {
                    tipoMaterial.descripcion
                }
            if (tipoMaterial.activo) {
                binding.textViewEstado.text =
                    context.getString(R.string.estado_activo)
                binding.textViewEstado.setBackgroundResource(R.drawable.bg_badge_activo)
                binding.textViewEstado.setTextColor(context.getColor(R.color.color_verde))
            } else {
                binding.textViewEstado.text = context.getString(R.string.estado_inactivo)
                binding.textViewEstado.setBackgroundResource(R.drawable.bg_badge_gris)
                binding.textViewEstado.setTextColor(context.getColor(R.color.color_rojo))
            }

            binding.root.setOnClickListener {
                onTipoMaterialClick(tipoMaterial)
            }
        }
    }

}