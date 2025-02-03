package com.example.guru2

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// DatabaseHelper 클래스 : SQLite 데이터베이스를 관리하는 클래스
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

    // 데이터베이스가 처음 생성될 때 호출, 테이블을 생성
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

    // 데이터베이스 버전이 변경될 때 호출, 테이블을 삭제하고 새로 생성
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME") // 기존 테이블 삭제
        onCreate(db) // 새 테이블 생성
    }

    // 아이템 목록을 모두 가져오는 함수
    @SuppressLint("Range")
    fun getItems(): List<Item> {
        val items = mutableListOf<Item>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null) // 모든 아이템 조회

        // 커서가 첫 번째 아이템으로 이동하면 반복문 시작
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                val category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY))
                val quantity = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTITY))
                val memo = cursor.getString(cursor.getColumnIndex(COLUMN_MEMO))
                val isCompleted = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_COMPLETED)) == 1

                // 아이템 객체 생성 후 리스트에 추가
                items.add(Item(id, name, category, quantity, memo, isCompleted))
            } while (cursor.moveToNext()) // 다음 아이템으로 이동
        }
        cursor.close() // 커서 닫기
        db.close() // 데이터베이스 닫기
        return items // 아이템 리스트 반환
    }

    // 특정 카테고리의 아이템 목록을 가져오는 함수
    @SuppressLint("Range")
    fun getItemsByCategory(category: String): List<Item> {
        val items = mutableListOf<Item>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_CATEGORY = ?", arrayOf(category))  // 카테고리 조건에 맞는 아이템 조회

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

    // 아이템을 추가하는 함수
    fun addItem(item: Item) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, item.name)
            put(COLUMN_CATEGORY, item.category)
            put(COLUMN_QUANTITY, item.quantity)
            put(COLUMN_MEMO, item.memo)
            put(COLUMN_IS_COMPLETED, if (item.isCompleted) 1 else 0)
        }
        db.insert(TABLE_NAME, null, values) // 새 아이템 데이터베이스에 삽입
        db.close() // 데이터베이스 닫기
    }

    // 아이템을 업데이트하는 함수
    fun updateItem(item: Item) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, item.name)
            put(COLUMN_CATEGORY, item.category)
            put(COLUMN_QUANTITY, item.quantity)
            put(COLUMN_MEMO, item.memo)
            put(COLUMN_IS_COMPLETED, if (item.isCompleted) 1 else 0)
        }
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(item.id.toString())) // 아이템 업데이트
        db.close() // 데이터베이스 닫기
    }

    // 아이템을 삭제하는 함수
    fun deleteItem(itemId: Int): Boolean {
        val db = writableDatabase
        val result = db.delete("items", "id = ?", arrayOf(itemId.toString()))
        db.close()
        return result > 0 // 삭제 성공 여부 반환
    }

}
