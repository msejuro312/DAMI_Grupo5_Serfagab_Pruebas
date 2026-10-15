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
            put(Contract.COLUMNA_MATERIAL_PENDIENTE_SYNC, if (material.pendienteSync) 1 else 0)
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
            Contract.COLUMNA_MATERIAL_ACTIVO,
            Contract.COLUMNA_MATERIAL_PENDIENTE_SYNC
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
            val indicePendiente = cursor.getColumnIndexOrThrow(Contract.COLUMNA_MATERIAL_PENDIENTE_SYNC)

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
                        version = cursor.getInt(indiceVersion),
                        pendienteSync = cursor.getInt(indicePendiente) == 1
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
            put(Contract.COLUMNA_MATERIAL_PENDIENTE_SYNC, if (material.pendienteSync) 1 else 0)
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
            put(Contract.COLUMNA_MATERIAL_PENDIENTE_SYNC, 1)
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
            put(Contract.COLUMNA_MATERIAL_PENDIENTE_SYNC, 1)
        }
        return dbHelper.writableDatabase.update(
            Contract.TABLA_MATERIAL,
            valores,
            "${Contract.COLUMNA_MATERIAL_ID} = ?",
            arrayOf(idMaterial.toString())
        )
    }

    fun reemplazarTodos(materiales: List<Material>) {
        val idsPendientes = obtenerPendientes().map { it.idMaterial }.toSet()
        val inactivosLocales = obtenerTodos().filter { !it.activo && !it.pendienteSync }
        val idsServidor = materiales.map { it.idMaterial }.toSet()
        val inactivosNoRecibidos = inactivosLocales.filter { it.idMaterial !in idsServidor }

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(
                Contract.TABLA_MATERIAL,
                "${Contract.COLUMNA_MATERIAL_PENDIENTE_SYNC} = 0",
                null
            )

            val materialesAInterior = (
                    materiales + inactivosNoRecibidos
                    )
                .filter { it.idMaterial !in idsPendientes }
                .distinctBy { it.idMaterial }

            materialesAInterior.forEach { material ->
                agregar(material.copy(pendienteSync = false))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun crearLocal(material: Material): Long {
        return agregar(material.copy(idMaterial = siguienteIdTemporal(), pendienteSync = true))
    }

    private fun siguienteIdTemporal(): Int {
        val minimo = obtenerTodos().minOfOrNull { it.idMaterial } ?:0
        return if (minimo < 0) minimo - 1 else -2
    }

    fun obtenerPendientes(): List<Material> {
        return obtenerTodos()
            .filter { it.pendienteSync }
            .sortedBy { it.idMaterial }
    }

    fun confirmarSincronizado(idLocal: Int, materialServidor: Material) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete(
                Contract.TABLA_MATERIAL,
                "${Contract.COLUMNA_MATERIAL_ID} = ?",
                arrayOf(idLocal.toString())
            )
            agregar(materialServidor.copy(pendienteSync = false))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}