package com.nolamarel.onlinelibrary

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.nolamarel.onlinelibrary.Adapters.myBooks.MyBook

class DatabaseHelper1(context: Context?, userName: String) :
    SQLiteOpenHelper(context, DATABASE_NAME + "_" + userName, null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE books (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_AUTHOR + " TEXT, " + COLUMN_TITLE + " TEXT, " + COLUMN_IMAGE
                + " TEXT, " + COLUMN_CONTENT + " TEXT)")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS  books")
        onCreate(db)
    }

    fun addBook(myBook: MyBook): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COLUMN_AUTHOR, myBook.bookAuthor)
        cv.put(COLUMN_TITLE, myBook.bookName)
        cv.put(COLUMN_IMAGE, myBook.bookImage)
        cv.put(COLUMN_CONTENT, myBook.bookContent)


        val result = db.insert("books", null, cv)
        db.close()
        return result != -1L
    }

    val allMyBooks: MutableList<MyBook>
        get() {
            val myBookList = mutableListOf<MyBook>()
            val db = this.readableDatabase
            val cursor = db.rawQuery("SELECT * FROM books", null)
            if (cursor.moveToFirst()) {
                do {
                    val myBook = MyBook(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4)
                    )
                    myBookList.add(myBook)
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
            return myBookList
        }

    fun getRowCount(db: SQLiteDatabase): Int {
        val cursor = db.rawQuery("SELECT COUNT(*) FROM books", null)
        cursor.moveToFirst()
        val rowCount = cursor.getInt(0)
        cursor.close()
        return rowCount
    }

    companion object {
        private const val DATABASE_NAME = "books.db"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_AUTHOR = "_author"
        private const val COLUMN_TITLE = "_title"
        private const val COLUMN_IMAGE = "_image"
        private const val COLUMN_CONTENT = "_content"
    }
}
