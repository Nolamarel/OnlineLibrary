package com.nolamarel.onlinelibrary.Adapters.books

import com.google.gson.annotations.SerializedName

data class Book(
    @SerializedName("id")
    val bookId: String,

    @SerializedName("author")
    val bookAuthor: String,

    @SerializedName("title")
    val bookName: String,

    @SerializedName("image")
    val bookImage: String,

    @SerializedName("genreId")
    val bookGenre: String? = null,

    @SerializedName("description")
    val bookDesc: String? = null,

    @SerializedName("localPath")
    val bookContent: String? = null
)
