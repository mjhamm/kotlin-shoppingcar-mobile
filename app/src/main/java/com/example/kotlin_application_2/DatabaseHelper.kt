package com.example.kotlin_application_2

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, TABLE_NAME, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    fun addItem(item: Item): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_PRODUCT, item.getProduct())
        db.update(TABLE_NAME, values, "$COLUMN_PRODUCT= ?", arrayOf(item.getProduct()))

        val result = db.insert(TABLE_NAME, null, values)

        return if (result.equals(-1)) {
            false
        } else {
            db.close()
            true
        }
    }

    fun loadItems(): Cursor {
        val db = this.writableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        return db.rawQuery(query, null)
    }

    fun deleteItems() {
        val db = this.writableDatabase
        val query = "DELETE FROM $TABLE_NAME WHERE 1"
        db.execSQL(query)
        db.close()
    }

    companion object {

        private const val TABLE_NAME = "items"
        private const val COLUMN_PRODUCT = "productname"

        private const val SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_PRODUCT + " TEXT PRIMARY KEY);"

        private const val SQL_DELETE_ENTRIES = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
    }
}