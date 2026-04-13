package com.nolamarel.onlinelibrary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.nolamarel.onlinelibrary.Adapters.myBooks.MyBook;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper1 extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "books.db";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_AUTHOR = "_author";
    private static final String COLUMN_TITLE = "_title";
    private static final String COLUMN_IMAGE = "_image";
    private static final String COLUMN_CONTENT = "_content";

    public DatabaseHelper1(@Nullable Context context, String userName) {
        super(context, DATABASE_NAME + "_" + userName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE books (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_AUTHOR + " TEXT, " + COLUMN_TITLE + " TEXT, " + COLUMN_IMAGE
                + " TEXT, " + COLUMN_CONTENT + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS  books");
        onCreate(db);
    }
    public boolean addBook(MyBook myBook){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_AUTHOR, myBook.getBookAuthor());
        cv.put(COLUMN_TITLE, myBook.getBookName());
        cv.put(COLUMN_IMAGE, myBook.getBookImage());
        cv.put(COLUMN_CONTENT, myBook.getBookContent());


        long result = db.insert("books", null, cv);
        db.close();
        return result != -1;
    }

    public List<MyBook> getAllMyBooks(){
        List<MyBook> myBookList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM books", null);
        if(cursor.moveToFirst()){
            do {
                MyBook myBook = new MyBook(cursor.getInt(0), cursor.getString(1),
                        cursor.getString(2), cursor.getString(3), cursor.getString(4));
                myBookList.add(myBook);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return myBookList;
    }

    public int getRowCount(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM books", null);
        cursor.moveToFirst();
        int rowCount = cursor.getInt(0);
        cursor.close();
        return rowCount;
    }
}
