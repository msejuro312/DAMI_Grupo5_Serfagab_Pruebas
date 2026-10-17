package com.serfagab.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.serfagab.app.databinding.ItemOrdenBinding
import com.serfagab.app.model.OrdenCompra
import java.util.Locale

interface OnOrdenAccion {
    fun onEditar(orden: OrdenCompra)
    fun onBorrar(orden: OrdenCompra)
}

class OrdenCompraAdapter (

    private val listener: OnOrdenAccion,
    private val puedeGestionar: Boolean
) : RecyclerView.Adapter<OrdenCompraAdapter.OrdenViewHolder>() {

    private val ordenes = mutableListOf<OrdenCompra>()

    fun actualizar (lista: List<OrdenCompra>) {
        ordenes.clear()
        ordenes.addAll(lista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrdenViewHolder {
        val binding = ItemOrdenBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false
            )
        return OrdenViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: OrdenViewHolder,
        position: Int
    ) {
        holder.vincular(ordenes[position])
    }

    override fun getItemCount(): Int = ordenes.size

    inner class OrdenViewHolder(
        private val binding: ItemOrdenBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun vincular (orden: OrdenCompra) {
            binding.textViewFechaOrden.text = orden.fecha
            binding.textViewProveedorOrden.text = orden.proveedor.razonSocial

            binding.textViewTotalOrden.text =
                String.format(Locale.getDefault(),"S/ %.2f", orden.total)


            binding.chipEstadoOrden.text = orden.estado.uppercase(Locale.ROOT)

            binding.chipPendienteSync.visibility = View.GONE

            if (puedeGestionar) {
                binding.root.setOnClickListener {
                    listener.onEditar(orden)
                }

                binding.root.setOnLongClickListener {
                    listener.onBorrar(orden)
                    true
                }
            } else {
                binding.root.setOnClickListener(null)
                binding.root.setOnLongClickListener(null)
            }

                }


        }


    }





