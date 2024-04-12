package com.nolamarel.onlinelibrary.Adapters.books;

import android.widget.ImageView;

public class Book {
    public String bookId, bookAuthor, bookName, bookImage;

    public Book(String bookId, String bookAuthor, String bookName, String bookImage) {
        this.bookId = bookId;
        this.bookAuthor = bookAuthor;
        this.bookName = bookName;
        this.bookImage = bookImage;
    }

    public void setBookImage(String bookImage) {
        this.bookImage = bookImage;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
}