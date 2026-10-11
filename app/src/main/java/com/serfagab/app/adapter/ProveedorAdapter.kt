package com.serfagab.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.serfagab.app.databinding.ItemProveedorBinding
import com.serfagab.app.model.Proveedor
import com.serfagab.app.R

class ProveedorAdapter(
    private val onProveedorclick: (Proveedor) -> Unit
) : RecyclerView.Adapter<ProveedorAdapter.ProveedorViewHolder>(){

    private val proveedores = mutableListOf<Proveedor>()

    fun actualizar(nuevosProveedores: List<Proveedor>) {
        val cantidadAnterior = proveedores.size
        proveedores.clear()
        if (cantidadAnterior > 0) {
            notifyItemRangeRemoved(0, cantidadAnterior)
        }

        proveedores.addAll(nuevosProveedores)
        if (proveedores.isNotEmpty()) {
            notifyItemRangeInserted(0, proveedores.size)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProveedorAdapter.ProveedorViewHolder {
        val binding = ItemProveedorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProveedorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProveedorAdapter.ProveedorViewHolder, position: Int) {
        holder.vincular(proveedores[position])
    }

    override fun getItemCount(): Int {
        return proveedores.size
    }

    inner class ProveedorViewHolder(
        private val binding: ItemProveedorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun vincular(proveedor: Proveedor) {
            val context = binding.root.context

            binding.textViewId.text =
                context.getString(R.string.codigo_formato_proveedor, proveedor.idProveedor)
            binding.textViewRazonSocial.text = proveedor.razonSocial
            binding.textViewRuc.text = proveedor.ruc
            binding.textViewCelular.text = proveedor.celular
            binding.textViewEmail.text = proveedor.email

            if (proveedor.activo) {
                binding.textViewEstado.text = context.getString(R.string.estado_activo)
                binding.textViewEstado.setBackgroundResource(R.drawable.bg_badge_activo)
                binding.textViewEstado.setTextColor(context.getColor(R.color.color_verde))
            } else {
                binding.textViewEstado.text = context.getString(R.string.estado_inactivo)
                binding.textViewEstado.setBackgroundResource(R.drawable.bg_badge_gris)
                binding.textViewEstado.setTextColor(context.getColor(R.color.color_rojo))
            }

            binding.root.setOnClickListener {
                onProveedorclick(proveedor)
            }
        }
    }

}