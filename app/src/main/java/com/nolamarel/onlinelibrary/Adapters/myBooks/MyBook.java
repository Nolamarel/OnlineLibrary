package com.nolamarel.onlinelibrary.Adapters.myBooks;

public class MyBook {
    String bookImage, bookId, bookPercent;

    public MyBook(String bookImage, String bookId, String bookPercent) {
        this.bookImage = bookImage;
        this.bookId = bookId;
        this.bookPercent = bookPercent;
    }

    public String getBookImage() {
        return bookImage;
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

    public String getBookPercent() {
        return bookPercent;
    }

    public void setBookPercent(String bookPercent) {
        this.bookPercent = bookPercent;
    }
}
