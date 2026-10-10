package com.serfagab.app.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.serfagab.app.model.OrdenCompra
import com.serfagab.app.model.Proveedor
import com.serfagab.app.model.Tipo
import com.serfagab.app.model.Usuario



class OrdenStore(context: Context) {

    private val dbHelper = SerfagabDbHelper(context.applicationContext)
    private val proveedorStore = ProveedorStore(context.applicationContext)
    private val detalleStore = OrdenDetalleStore(context.applicationContext)

    fun insertarOrden(orden: OrdenCompra): Long {
        val db = dbHelper.writableDatabase
        var idOrden = -1L

        db.beginTransaction()
        try {
            val valores = ContentValues().apply {
                if (orden.idOrdenCompra != 0) {
                    put(Contract.COLUMNA_ORDEN_COMPRA_ID, orden.idOrdenCompra)

                }
                    put(Contract.COLUMNA_ORDEN_COMPRA_ID_PROVEEDOR, orden.proveedor.idProveedor)
                    put(Contract.COLUMNA_ORDEN_COMPRA_ID_USUARIO, orden.usuario.idUsuario)
                    put(Contract.COLUMNA_ORDEN_COMPRA_OBSERVACIONES, orden.observaciones)
                    put(Contract.COLUMNA_ORDEN_COMPRA_FECHA, orden.fecha)
                    put(Contract.COLUMNA_ORDEN_COMPRA_TOTAL, orden.total)
                    put(Contract.COLUMNA_ORDEN_COMPRA_ESTADO, orden.estado)
                    put(Contract.COLUMNA_ORDEN_COMPRA_PENDIENTE_SYNC, 1)
                }

                idOrden = db.insert(Contract.TABLA_ORDEN_COMPRA, null, valores)

                if (idOrden == -1L) {
                    throw IllegalStateException("No se pudo insertar la orden")

                }
                detalleStore.insertarVarias(
                    db,
                    idOrden.toInt(),
                    orden.detalles
                )
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }

            return idOrden
        }





}
