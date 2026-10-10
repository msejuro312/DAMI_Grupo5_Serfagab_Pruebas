package com.serfagab.app.local

import android.content.ContentValues
import android.content.Context
import com.serfagab.app.model.Proveedor

class ProveedorStore(context: Context) {

    private val dbHelper = SerfagabDbHelper(context.applicationContext)

    fun obtenerTodos(): List<Proveedor> {
        val proveedores = mutableListOf<Proveedor>()
        val columnas = arrayOf(
            Contract.COLUMNA_PROVEEDOR_ID,
            Contract.COLUMNA_PROVEEDOR_RAZON_SOCIAL,
            Contract.COLUMNA_PROVEEDOR_RUC,
            Contract.COLUMNA_PROVEEDOR_CELULAR,
            Contract.COLUMNA_PROVEEDOR_EMAIL,
            Contract.COLUMNA_PROVEEDOR_DESCRIPCION,
            Contract.COLUMNA_PROVEEDOR_ACTIVO
        )

        dbHelper.readableDatabase.query(
            Contract.TABLA_PROVEEDOR,
            columnas,
            null,
            null,
            null,
            null,
            "${Contract.COLUMNA_PROVEEDOR_ID} DESC"
        ).use { cursor ->
            val indiceId = cursor.getColumnIndexOrThrow(Contract.COLUMNA_PROVEEDOR_ID)
            val indiceRazonSocial = cursor.getColumnIndexOrThrow(Contract.COLUMNA_PROVEEDOR_RAZON_SOCIAL)
            val indiceRuc = cursor.getColumnIndexOrThrow(Contract.COLUMNA_PROVEEDOR_RUC)
            val indiceCelular = cursor.getColumnIndexOrThrow(Contract.COLUMNA_PROVEEDOR_CELULAR)
            val indiceEmail = cursor.getColumnIndexOrThrow(Contract.COLUMNA_PROVEEDOR_EMAIL)
            val indiceDescripcion = cursor.getColumnIndexOrThrow(Contract.COLUMNA_PROVEEDOR_DESCRIPCION)
            val indiceActivo = cursor.getColumnIndexOrThrow(Contract.COLUMNA_PROVEEDOR_ACTIVO)

            while (cursor.moveToNext()) {
                proveedores.add(
                    Proveedor(
                        idProveedor = cursor.getInt(indiceId),
                        razonSocial = cursor.getString(indiceRazonSocial),
                        ruc = cursor.getString(indiceRuc),
                        celular = cursor.getString(indiceCelular),
                        email = cursor.getString(indiceEmail),
                        descripcion = cursor.getString(indiceDescripcion) ?: "",
                        activo = cursor.getInt(indiceActivo) == 1
                    )
                )
            }
        }
        return proveedores
    }

    fun insertar(proveedor: Proveedor): Long {
        val valores = ContentValues().apply {
            if (proveedor.idProveedor != 0) put(Contract.COLUMNA_PROVEEDOR_ID, proveedor.idProveedor)
            put(Contract.COLUMNA_PROVEEDOR_RAZON_SOCIAL, proveedor.razonSocial)
            put(Contract.COLUMNA_PROVEEDOR_RUC, proveedor.ruc)
            put(Contract.COLUMNA_PROVEEDOR_CELULAR, proveedor.celular)
            put(Contract.COLUMNA_PROVEEDOR_EMAIL, proveedor.email)
            put(Contract.COLUMNA_PROVEEDOR_DESCRIPCION, proveedor.descripcion)
            put(Contract.COLUMNA_PROVEEDOR_ACTIVO, if (proveedor.activo) 1 else 0)
        }

        return dbHelper.writableDatabase.insert(
            Contract.TABLA_PROVEEDOR,
            null,
            valores
        )
    }

    fun reemplazarTodos(proveedor: List<Proveedor>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(Contract.TABLA_PROVEEDOR, null,null)
            proveedor.forEach { insertar(it) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

    }
}