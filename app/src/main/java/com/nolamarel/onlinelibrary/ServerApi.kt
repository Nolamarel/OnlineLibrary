package com.nolamarel.onlinelibrary

import com.nolamarel.onlinelibrary.Adapters.myBooks.UserBookDto
import com.nolamarel.onlinelibrary.network.CreateLocalBookRequest
import com.nolamarel.onlinelibrary.network.CreateLocalBookResponse
import com.nolamarel.onlinelibrary.network.MessageResponse
import com.nolamarel.onlinelibrary.network.UpdateProgressRequest
import com.nolamarel.onlinelibrary.network.UpdateStatusRequest
import retrofit2.Response
import retrofit2.http.*

interface ServerApi {

    @GET("genres")
    suspend fun getGenres(): Response<List<GenreDTO>>

    @GET("books/{genreId}")
    suspend fun getBooksByGenre(
        @Path("genreId") genreId: String
    ): Response<List<BookDTO>>

    @GET("book/{id}")
    suspend fun getBookById(
        @Path("id") id: String
    ): Response<BookDTO>

    @POST("my-books/{bookId}")
    suspend fun addBookToLibrary(
        @Header("Authorization") token: String,
        @Path("bookId") bookId: String
    ): Response<MessageResponse>

    @GET("my-books")
    suspend fun getMyBooks(
        @Header("Authorization") token: String
    ): Response<List<UserBookDto>>

    @PATCH("my-books/{bookId}/progress")
    suspend fun updateReadingProgress(
        @Header("Authorization") token: String,
        @Path("bookId") bookId: String,
        @Body body: UpdateProgressRequest
    ): Response<MessageResponse>

    @DELETE("my-books/{bookId}")
    suspend fun deleteBookFromLibrary(
        @Header("Authorization") token: String,
        @Path("bookId") bookId: String
    ): Response<MessageResponse>

    @GET("user/{id}")
    suspend fun getUserById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<UserDTO>

    @POST("books/local")
    suspend fun createLocalBook(
        @Header("Authorization") token: String,
        @Body body: CreateLocalBookRequest
    ): Response<CreateLocalBookResponse>

    @GET("search")
    suspend fun searchBooks(
        @Query("q") query: String
    ): Response<List<BookDTO>>

    @PATCH("my-books/{bookId}/status")
    suspend fun updateBookStatus(
        @Header("Authorization") token: String,
        @Path("bookId") bookId: String,
        @Body body: UpdateStatusRequest
    ): Response<MessageResponse>

}