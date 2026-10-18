package com.serfagab.app.sync

import android.content.Context
import com.serfagab.app.local.MaterialStore
import com.serfagab.app.local.OrdenStore
import com.serfagab.app.local.ProveedorStore
import com.serfagab.app.local.TipoMaterialStore
import com.serfagab.app.model.Material
import com.serfagab.app.model.OrdenCompra
import com.serfagab.app.remote.ApiClient
import com.serfagab.app.remote.Respuesta
import com.serfagab.app.util.SessionStore

object SyncManager {

    fun sincronizarCatalogos(context: Context): Boolean {
        if (SessionStore.esSesionGoogle(context)) return true

        val subidasMateriales = subirMaterialesPendientes(context)
        val subidaOrdenes = runCatching { subirOrdenesPendientes(context) }.getOrDefault(false)

        val tipos = ApiClient.listarTiposMaterial()
        val proveedores = ApiClient.listarProveedores()
        val materiales = ApiClient.listarTodosLosMateriales()

        tipos.datos?.let { TipoMaterialStore(context).reemplazarTodos(it) }
        proveedores.datos?.let { ProveedorStore(context).reemplazarTodos(it) }
        materiales.datos?.let { MaterialStore(context).reemplazarTodos(it) }

        val ordenesDescargadas = runCatching { descargarOrdenes(context) }.getOrDefault(false)

        return subidasMateriales && subidaOrdenes && tipos.exito && proveedores.exito && materiales.exito && ordenesDescargadas
    }

    private fun subirMaterialesPendientes(context: Context): Boolean {
        val store = MaterialStore(context)
        var todoCorrecto = true

        store.obtenerPendientes().forEach { material ->
            val respuesta = if (material.idMaterial < 0) {
                ApiClient.crearMaterial(material)
            } else {
                subirCambiosMaterial(material)
            }

            val materialServidor = respuesta.datos
            if (respuesta.exito && materialServidor != null) {
                store.confirmarSincronizado(material.idMaterial, materialServidor)
            } else {
                todoCorrecto = false
            }
        }
        return todoCorrecto
    }

    private fun subirCambiosMaterial(material: Material): Respuesta<Material> {
        val actualizado = ApiClient.actualizarMaterial(material.idMaterial, material)
        val materialServidor = actualizado.datos
        if (!actualizado.exito || materialServidor == null) return actualizado
        if (materialServidor.activo == material.activo) return actualizado

        return if (material.activo) {
            ApiClient.activarMaterial(material.idMaterial)
        } else {
            ApiClient.desactivarMaterial(material.idMaterial)
        }
    }

    private fun subirOrdenesPendientes(context: Context): Boolean {
        val store = OrdenStore(context)
        var todoCorrecto = true
        store.listarPendientes().forEach { orden ->
            if (!subirOrden(store, orden)) todoCorrecto = false
        }
        return todoCorrecto
    }

    private fun subirOrden(store: OrdenStore, orden: OrdenCompra): Boolean {
        val cabecera = orden.copy(detalles = emptyList())
        var idServidor = orden.idOrdenCompra

        if (idServidor > 0) {
            val editada = ApiClient.editarOrden(idServidor, cabecera)
            if (editada.codigo == 404) {
                idServidor = crearEnServidor(store, orden, cabecera) ?: return false
            } else if (!editada.exito) {
                return false
            }
        } else {
            idServidor = crearEnServidor(store, orden, cabecera) ?: return false
        }

        val existente = ApiClient.obtenerOrden(idServidor)
        val ordenActual = existente.datos
        if (!existente.exito || ordenActual == null) return false
        val yaSubidos = ordenActual.detalles.map { it.material.idMaterial }.toSet()

        val lineasCorrectas = orden.detalles
            .filter { it.material.idMaterial !in yaSubidos }
            .all { detalle ->
                ApiClient.agregarDetalleOrden(
                    idServidor,
                    detalle.copy(idDetalle = 0, ordenCompra = cabecera.copy(idOrdenCompra = idServidor))
                ).exito
            }
        if (!lineasCorrectas) return false
        store.marcarSincronizada(idServidor)
        return true
    }

    private fun descargarOrdenes(context: Context): Boolean {
        val idUsuario = SessionStore.idUsuario(context)
        if (idUsuario <= 0) return true

        val respuesta = ApiClient.listarOrdenesPorUsuario(idUsuario)
        val ordenes = respuesta.datos ?: return false
        val store = OrdenStore(context)

        ordenes.firstOrNull()?.usuario?.let { store.guardarUsuarioLocal(it.copy(clave = "")) }
        store.reemplazarDe(idUsuario, ordenes)
        return respuesta.exito
    }

    private fun crearEnServidor(store: OrdenStore, orden: OrdenCompra, cabecera: OrdenCompra): Int? {
        val creada = ApiClient.crearOrdenCompra(
            orden.usuario.idUsuario, cabecera.copy(idOrdenCompra = 0)
        )
        val ordenServidor = creada.datos
        if (!creada.exito || ordenServidor == null) return  null
        store.cambiarId(orden.idOrdenCompra, ordenServidor.idOrdenCompra)
        return ordenServidor.idOrdenCompra
    }
}