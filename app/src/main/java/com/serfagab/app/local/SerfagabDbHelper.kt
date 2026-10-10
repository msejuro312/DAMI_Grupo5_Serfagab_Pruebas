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

}