package com.nolamarel.onlinelibrary.Adapters.myBooks

class MyBook {
    var bookId: Int
    var bookName: String? = null
    var bookAuthor: String? = null
    @kotlin.jvm.JvmField
    var bookImage: String
    var bookContent: String? = null

    constructor(bookImage: String, bookId: Int, bookName: String?, bookAuthor: String?) {
        this.bookImage = bookImage
        this.bookId = bookId
        this.bookName = bookName
        this.bookAuthor = bookAuthor
    }

    constructor(
        bookId: Int,
        bookName: String?,
        bookAuthor: String?,
        bookImage: String,
        bookContent: String?
    ) {
        this.bookId = bookId
        this.bookName = bookName
        this.bookAuthor = bookAuthor
        this.bookImage = bookImage
        this.bookContent = bookContent
    }

    constructor(bookId: Int, bookImage: String) {
        this.bookId = bookId
        this.bookImage = bookImage
    }
}
