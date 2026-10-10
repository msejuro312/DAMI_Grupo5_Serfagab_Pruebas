package com.serfagab.app.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.serfagab.app.model.OrdenCompra

class OrdenDetalleStore(context: Context){

    private val appContext = context.applicationContext
    private val dbHelper = SerfagabDbHelper(context.applicationContext)

    fun insertarVarias(
        db: SQLiteDatabase,
        idOrdenCompra: Int,
        detalles: List<com.serfagab.app.model.DetalleOrdenCompra>
    ){
        detalles.forEach { detalle ->
            val valores = ContentValues().apply {

                put(
                    Contract.COLUMNA_DETALLE_ORDEN_ID_MATERIAL,
                    detalle.material.idMaterial
                )

                put(
                    Contract.COLUMNA_DETALLE_ORDEN_ID_ORDEN,
                    idOrdenCompra
                )

                put(
                    Contract.COLUMNA_DETALLE_ORDEN_CANTIDAD,
                    detalle.cantidad
                )

                put(
                    Contract.COLUMNA_DETALLE_ORDEN_PRECIO,
                    detalle.precioUnitario
                )

                put(
                    Contract.COLUMNA_DETALLE_ORDEN_SUBTOTAL,
                    detalle.subtotal
                )

            }

            val resultado = db.insert(
                Contract.TABLA_DETALLE_ORDEN,
                null,
                valores
            )

            check(resultado != -1L){
                "No se pudo insertar un detalle de la orden"
            }
        }
    }


}