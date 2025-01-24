package com.example.guru2

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "items_db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "items"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_QUANTITY = "quantity"
        private const val COLUMN_MEMO = "memo"
        private const val COLUMN_IS_COMPLETED = "is_completed"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_NAME TEXT," +
                "$COLUMN_CATEGORY TEXT," +
                "$COLUMN_QUANTITY INTEGER," +
                "$COLUMN_MEMO TEXT," +
                "$COLUMN_IS_COMPLETED INTEGER)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    @SuppressLint("Range")
    fun getItems(): List<Item> {
        val items = mutableListOf<Item>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                val category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY))
                val quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY))
                val memo = cursor.getString(cursor.getColumnIndex(COLUMN_MEMO))
                val isCompleted = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_COMPLETED)) == 1

                items.add(Item(id, name, category, quantity, memo, isCompleted))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return items
    }

    fun addItem(item: Item) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, item.name)
            put(COLUMN_CATEGORY, item.category)
            put(COLUMN_QUANTITY, item.quantity)
            put(COLUMN_MEMO, item.memo)
            put(COLUMN_IS_COMPLETED, if (item.isCompleted) 1 else 0)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun updateItem(item: Item) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, item.name)
            put(COLUMN_CATEGORY, item.category)
            put(COLUMN_QUANTITY, item.quantity)
            put(COLUMN_MEMO, item.memo)
            put(COLUMN_IS_COMPLETED, if (item.isCompleted) 1 else 0)
        }
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(item.id.toString()))
        db.close()
    }

    fun deleteItem(itemId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete("items", "id = ?", arrayOf(itemId.toString()))
        db.close()
        return result > 0 // 삭제 성공 여부 반환
    }

}
