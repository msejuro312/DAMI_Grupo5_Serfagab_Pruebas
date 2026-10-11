package com.serfagab.app.sync

import android.content.Context
import com.serfagab.app.local.MaterialStore
import com.serfagab.app.local.ProveedorStore
import com.serfagab.app.local.TipoMaterialStore
import com.serfagab.app.remote.ApiClient

object Sincronizador {

    fun sincronizarCatalogos(context: Context): Boolean {
        val tipos = ApiClient.listarTiposMaterial()
        val proveedores = ApiClient.listarProveedores()
        val materiales = ApiClient.listarMateriales()

        tipos.datos?.let { TipoMaterialStore(context).reemplazarTodos(it) }
        proveedores.datos?.let { ProveedorStore(context).reemplazarTodos(it) }
        materiales.datos?.let { MaterialStore(context).reemplazarTodos(it) }

        return tipos.exito && proveedores.exito && materiales.exito
    }
}