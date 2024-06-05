package com.nolamarel.onlinelibrary.Adapters.myBooks;

public class MyBook {
    int bookId;
    String bookName, bookAuthor, bookImage, bookContent;

    public MyBook(String bookImage, int bookId, String bookName, String bookAuthor) {
        this.bookImage = bookImage;
        this.bookId = bookId;
        this.bookName = bookName;
        this.bookAuthor = bookAuthor;
    }

    public MyBook(int bookId, String bookName, String bookAuthor, String bookImage, String bookContent) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.bookAuthor = bookAuthor;
        this.bookImage = bookImage;
        this.bookContent = bookContent;
    }

    public MyBook(int bookId, String bookImage) {
        this.bookId = bookId;
        this.bookImage = bookImage;
    }

    public String getBookImage() {
        return bookImage;
    }

    public void setBookImage(String bookImage) {
        this.bookImage = bookImage;
    }

    public String getBookContent() {
        return bookContent;
    }

    public void setBookContent(String bookContent) {
        this.bookContent = bookContent;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }


    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }
}
