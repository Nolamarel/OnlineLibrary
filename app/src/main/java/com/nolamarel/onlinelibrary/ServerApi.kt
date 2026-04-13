package com.nolamarel.onlinelibrary

import com.nolamarel.onlinelibrary.Adapters.books.Book
import com.nolamarel.onlinelibrary.Adapters.myBooks.UserBookDTO
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ServerApi {
    @GET("genres")
    fun getGenres(): Call<List<GenreDTO>>

    @GET("books/{genreId}")
    fun getBooksByGenre(@Path("genreId") genreId: String): Call<List<BookDTO>>

    @GET("book/{id}")
    fun getBookDetails(@Path("id") bookId: String): Call<BookDetailsDTO>

    @POST("my-books/{bookId}")
    suspend fun addBookToUser(@Path("bookId") bookId: String): Response<Unit>

    @GET("my-books")
    suspend fun getUserBooks(): Response<List<UserBookDTO>>

    @GET("search")
    fun searchBooks(@Query("query") query: String): Call<List<Book>>

    @GET("user/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<UserDTO>
}