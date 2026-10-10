package com.serfagab.app.local

import android.content.ContentValues
import android.content.Context
import com.serfagab.app.model.TipoMaterial

class TipoMaterialStore(context: Context) {
    private val dbHelper = SerfagabDbHelper(context.applicationContext)

    fun obtenerTodos(): List<TipoMaterial> {
        val tipos = mutableListOf<TipoMaterial>()
        val columnas = arrayOf(
            Contract.COLUMNA_TIPO_ID,
            Contract.COLUMNA_TIPO_NOMBRE,
            Contract.COLUMNA_TIPO_DESCRIPCION,
            Contract.COLUMNA_TIPO_ACTIVO
        )

        dbHelper.readableDatabase.query(
            Contract.TABLA_TIPO_MATERIAL,
            columnas,
            null,
            null,
            null,
            null,
            "${Contract.COLUMNA_TIPO_ID} DESC"
        ).use { cursor ->
            val indiceId = cursor.getColumnIndexOrThrow(Contract.COLUMNA_TIPO_ID)
            val indiceNombre = cursor.getColumnIndexOrThrow(Contract.COLUMNA_TIPO_NOMBRE)
            val indiceDescripcion = cursor.getColumnIndexOrThrow(Contract.COLUMNA_TIPO_DESCRIPCION)
            val indiceActivo = cursor.getColumnIndexOrThrow(Contract.COLUMNA_TIPO_ACTIVO)

            while (cursor.moveToNext()) {
                tipos.add(
                    TipoMaterial(
                        idTipoMaterial = cursor.getInt(indiceId),
                        nombre = cursor.getString(indiceNombre),
                        descripcion = cursor.getString(indiceDescripcion) ?: "",
                        activo = cursor.getInt(indiceActivo) == 1
                    )
                )
            }
        }
        return tipos
    }

    fun insertar(tipo: TipoMaterial): Long {
        val valores = ContentValues().apply {
            if (tipo.idTipoMaterial != 0) put(Contract.COLUMNA_TIPO_ID, tipo.idTipoMaterial)
            put(Contract.COLUMNA_TIPO_NOMBRE, tipo.nombre)
            put(Contract.COLUMNA_TIPO_DESCRIPCION, tipo.descripcion)
            put(Contract.COLUMNA_TIPO_ACTIVO, if (tipo.activo) 1 else 0)
        }
        return dbHelper.writableDatabase.insert(
            Contract.TABLA_TIPO_MATERIAL,
            null,
            valores
        )
    }

    fun reemplazarTodos(tipos: List<TipoMaterial>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(Contract.TABLA_TIPO_MATERIAL,null, null)
            tipos.forEach { insertar(it) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

    }

}