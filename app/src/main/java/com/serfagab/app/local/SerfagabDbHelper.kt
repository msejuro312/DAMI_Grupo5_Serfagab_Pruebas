package com.serfagab.app.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SerfagabDbHelper(context: Context) : SQLiteOpenHelper(
    context,
    Contract.NOMBRE_BASE_DATOS,
    null,
    Contract.VERSION_BASE_DATOS
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(Contract.SQL_CREAR_TABLA_TIPOU)
        db.execSQL(Contract.SQL_CREAR_TABLA_USUARIO)
        db.execSQL(Contract.SQL_CREAR_TABLA_TIPO_MATERIAL)
        db.execSQL(Contract.SQL_CREAR_TABLA_PROVEEDOR)
        db.execSQL(Contract.SQL_CREAR_TABLA_MATERIAL)
        db.execSQL(Contract.SQL_CREAR_TABLA_ORDEN_COMPRA)
        db.execSQL(Contract.SQL_CREAR_TABLA_DETALLE_ORDEN)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        if (db.isReadOnly) return
        if (!existeColumna(db, Contract.TABLA_MATERIAL, Contract.COLUMNA_MATERIAL_PENDIENTE_SYNC)) {
            db.execSQL("ALTER TABLE ${Contract.TABLA_MATERIAL} " +  "ADD COLUMN ${Contract.COLUMNA_MATERIAL_PENDIENTE_SYNC} INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    private fun existeColumna(db: SQLiteDatabase, tabla: String, columna: String): Boolean {
        db.rawQuery("PRAGMA table_info($tabla)", null).use { cursor ->
            val indice = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) {
                if (cursor.getString(indice) == columna) return true
            }
        }
        return false
    }
}