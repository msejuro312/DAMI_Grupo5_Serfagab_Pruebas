package com.serfagab.app.local

import android.content.ContentValues
import android.content.Context
import com.serfagab.app.model.Material
import com.serfagab.app.model.TipoMaterial

class MaterialStore(context: Context) {

    private val dbHelper = SerfagabDbHelper(context.applicationContext)
    private val tipoMaterialStore = TipoMaterialStore(context.applicationContext)

    fun agregar(material: Material): Long {
        val valores = ContentValues().apply {
            if (material.idMaterial != 0) put(Contract.COLUMNA_MATERIAL_ID, material.idMaterial)
            put(Contract.COLUMNA_MATERIAL_ID_TIPO, material.tipoMaterial.idTipoMaterial)
            put(Contract.COLUMNA_MATERIAL_NOMBRE, material.nombre)
            put(Contract.COLUMNA_MATERIAL_UNIDAD, material.unidadMedida)
            put(Contract.COLUMNA_MATERIAL_STOCK, material.stockActual)
            put(Contract.COLUMNA_MATERIAL_PRECIO, material.precioReferencial)
            put(Contract.COLUMNA_MATERIAL_DESCRIPCION, material.descripcion)
            put(Contract.COLUMNA_MATERIAL_VERSION, material.version)
            put(Contract.COLUMNA_MATERIAL_ACTIVO, if (material.activo) 1 else 0)
        }

        return dbHelper.writableDatabase.insert(
            Contract.TABLA_MATERIAL,
            null,
            valores
        )
    }

    fun obtenerTodos(): List<Material> {
        val tiposPorId = tipoMaterialStore.obtenerTodos().associateBy { it.idTipoMaterial }
        val materiales = mutableListOf<Material>()
        val columnas = arrayOf(
            Contract.COLUMNA_MATERIAL_ID,
            Contract.COLUMNA_MATERIAL_ID_TIPO,
            Contract.COLUMNA_MATERIAL_NOMBRE,
            Contract.COLUMNA_MATERIAL_UNIDAD,
            Contract.COLUMNA_MATERIAL_STOCK,
            Contract.COLUMNA_MATERIAL_PRECIO,
            Contract.COLUMNA_MATERIAL_DESCRIPCION,
            Contract.COLUMNA_MATERIAL_VERSION,
            Contract.COLUMNA_MATERIAL_ACTIVO
        )

        dbHelper.readableDatabase.query(
            Contract.TABLA_MATERIAL,
            columnas,
            null,
            null,
            null,
            null,
            "${Contract.COLUMNA_MATERIAL_ID} DESC"
        ).use { cursor ->
            val indiceId = cursor.getColumnIndexOrThrow(Contract.COLUMNA_MATERIAL_ID)
            val indiceIdTipo = cursor.getColumnIndexOrThrow(Contract.COLUMNA_MATERIAL_ID_TIPO)
            val indiceNombre = cursor.getColumnIndexOrThrow(Contract.COLUMNA_MATERIAL_NOMBRE)
            val indiceUnidad = cursor.getColumnIndexOrThrow(Contract.COLUMNA_MATERIAL_UNIDAD)
            val indiceStock = cursor.getColumnIndexOrThrow(Contract.COLUMNA_MATERIAL_STOCK)
            val indicePrecio = cursor.getColumnIndexOrThrow(Contract.COLUMNA_MATERIAL_PRECIO)
            val indiceDescripcion = cursor.getColumnIndexOrThrow(Contract.COLUMNA_MATERIAL_DESCRIPCION)
            val indiceVersion = cursor.getColumnIndexOrThrow(Contract.COLUMNA_MATERIAL_VERSION)
            val indiceActivo = cursor.getColumnIndexOrThrow(Contract.COLUMNA_MATERIAL_ACTIVO)

            while (cursor.moveToNext()) {
                val idTipo = cursor.getInt(indiceIdTipo)
                val tipoMaterial = tiposPorId[idTipo]
                    ?: TipoMaterial(idTipoMaterial = idTipo, nombre = "", descripcion = null, activo = true)

                materiales.add(
                    Material(
                        idMaterial = cursor.getInt(indiceId),
                        tipoMaterial = tipoMaterial,
                        nombre = cursor.getString(indiceNombre),
                        unidadMedida = cursor.getString(indiceUnidad)?: "",
                        stockActual = cursor.getDouble(indiceStock),
                        precioReferencial = cursor.getDouble(indicePrecio),
                        descripcion = cursor.getString(indiceDescripcion) ?: "",
                        activo = cursor.getInt(indiceActivo) == 1,
                        version = cursor.getInt(indiceVersion)
                    )
                )
            }
        }
        return materiales
    }

    fun actualizar(material: Material): Int {
        val valores = ContentValues().apply {
            put(Contract.COLUMNA_MATERIAL_ID_TIPO, material.tipoMaterial.idTipoMaterial)
            put(Contract.COLUMNA_MATERIAL_NOMBRE, material.nombre)
            put(Contract.COLUMNA_MATERIAL_UNIDAD, material.unidadMedida)
            put(Contract.COLUMNA_MATERIAL_STOCK, material.stockActual)
            put(Contract.COLUMNA_MATERIAL_PRECIO, material.precioReferencial)
            put(Contract.COLUMNA_MATERIAL_DESCRIPCION, material.descripcion)
            put(Contract.COLUMNA_MATERIAL_VERSION, material.version)
            put(Contract.COLUMNA_MATERIAL_ACTIVO, if (material.activo) 1 else 0)
        }

        return dbHelper.writableDatabase.update(
            Contract.TABLA_MATERIAL,
            valores,
            "${Contract.COLUMNA_MATERIAL_ID} = ?",
            arrayOf(material.idMaterial.toString())
        )
    }

    fun desactivar(idMaterial: Int): Int {
        val valores = ContentValues().apply {
            put(Contract.COLUMNA_MATERIAL_ACTIVO, 0)
        }
        return dbHelper.writableDatabase.update(
            Contract.TABLA_MATERIAL,
            valores,
            "${Contract.COLUMNA_MATERIAL_ID} = ?",
            arrayOf(idMaterial.toString())
        )
    }

    fun activar(idMaterial: Int): Int {
        val valores = ContentValues().apply {
            put(Contract.COLUMNA_MATERIAL_ACTIVO, 1)
        }
        return dbHelper.writableDatabase.update(
            Contract.TABLA_MATERIAL,
            valores,
            "${Contract.COLUMNA_MATERIAL_ID} = ?",
            arrayOf(idMaterial.toString())
        )
    }

    fun reemplazarTodos(materiales: List<Material>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(Contract.TABLA_MATERIAL, null, null)
            materiales.forEach { agregar(it) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}