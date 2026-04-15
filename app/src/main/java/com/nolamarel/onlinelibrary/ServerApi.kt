package com.nolamarel.onlinelibrary

import com.nolamarel.onlinelibrary.Adapters.myBooks.UserBookDto
import com.nolamarel.onlinelibrary.network.AdminSearchHistoryResponse
import com.nolamarel.onlinelibrary.network.AdminUpdateReviewRequest
import com.nolamarel.onlinelibrary.network.AdminUpdateUserBookRequest
import com.nolamarel.onlinelibrary.network.AdminUpdateUserRequest
import com.nolamarel.onlinelibrary.network.AdminUserBookResponse
import com.nolamarel.onlinelibrary.network.BookReviewsResponse
import com.nolamarel.onlinelibrary.network.ChangePasswordRequest
import com.nolamarel.onlinelibrary.network.CreateLocalBookRequest
import com.nolamarel.onlinelibrary.network.CreateLocalBookResponse
import com.nolamarel.onlinelibrary.network.CreateReviewRequest
import com.nolamarel.onlinelibrary.network.GenreDto
import com.nolamarel.onlinelibrary.network.GenreRequest
import com.nolamarel.onlinelibrary.network.GenreResponse
import com.nolamarel.onlinelibrary.network.GoogleBookResponse
import com.nolamarel.onlinelibrary.network.MessageResponse
import com.nolamarel.onlinelibrary.network.ReviewResponse
import com.nolamarel.onlinelibrary.network.SearchBookResponse
import com.nolamarel.onlinelibrary.network.SearchHistoryDto
import com.nolamarel.onlinelibrary.network.UpdateProfileRequest
import com.nolamarel.onlinelibrary.network.UpdateProgressRequest
import com.nolamarel.onlinelibrary.network.UpdateReviewStatusRequest
import com.nolamarel.onlinelibrary.network.UpdateStatusRequest
import com.nolamarel.onlinelibrary.network.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ServerApi {

    @GET("genres")
    suspend fun getGenres(): Response<List<GenreResponse>>

    @GET("books/{genreId}")
    suspend fun getBooksByGenre(
        @Path("genreId") genreId: String
    ): Response<List<SearchBookResponse>>

    @GET("book/{id}")
    suspend fun getBookById(
        @Path("id") id: String
    ): Response<SearchBookResponse>

    @GET("google-book/{id}")
    suspend fun getGoogleBookById(
        @Path("id") externalId: String,
        @Header("Authorization") token: String? = null
    ): Response<GoogleBookResponse>

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

    @PATCH("my-books/{bookId}/status")
    suspend fun updateBookStatus(
        @Header("Authorization") token: String,
        @Path("bookId") bookId: String,
        @Body body: UpdateStatusRequest
    ): Response<MessageResponse>

    @GET("me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): Response<UserProfileResponse>

    @PATCH("me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body body: UpdateProfileRequest
    ): Response<UserResponse>

    @POST("books/local")
    suspend fun createLocalBook(
        @Header("Authorization") token: String,
        @Body body: CreateLocalBookRequest
    ): Response<CreateLocalBookResponse>

    @GET("search")
    suspend fun searchBooks(
        @Query("query") query: String,
        @Header("Authorization") token: String? = null
    ): Response<List<SearchBookResponse>>

    @GET("my-search-history")
    suspend fun getSearchHistory(
        @Header("Authorization") token: String
    ): Response<List<SearchHistoryDto>>

    @DELETE("my-books/{bookId}")
    suspend fun deleteBookFromLibrary(
        @Header("Authorization") token: String,
        @Path("bookId") bookId: String
    ): Response<MessageResponse>

    @PATCH("me/password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body body: ChangePasswordRequest
    ): Response<MessageResponse>

    @GET("books/{bookId}/reviews")
    suspend fun getBookReviews(
        @Path("bookId") bookId: String
    ): Response<BookReviewsResponse>

    @GET("books/{bookId}/my-review")
    suspend fun getMyReviewForBook(
        @Header("Authorization") token: String,
        @Path("bookId") bookId: String
    ): Response<ReviewResponse>

    @GET("me/reviews")
    suspend fun getMyReviews(
        @Header("Authorization") token: String
    ): Response<List<ReviewResponse>>

    @POST("books/{bookId}/reviews")
    suspend fun createReview(
        @Header("Authorization") token: String,
        @Path("bookId") bookId: String,
        @Body body: CreateReviewRequest
    ): Response<MessageResponse>

    @DELETE("books/{bookId}/reviews")
    suspend fun deleteMyReview(
        @Header("Authorization") token: String,
        @Path("bookId") bookId: String
    ): Response<MessageResponse>

    @PATCH("books/{bookId}/reviews")
    suspend fun updateReview(
        @Header("Authorization") token: String,
        @Path("bookId") bookId: String,
        @Body body: CreateReviewRequest
    ): Response<MessageResponse>

    @GET("moderation/reviews")
    suspend fun getAllReviewsForModeration(
        @Header("Authorization") token: String
    ): Response<List<ReviewResponse>>

    @PATCH("moderation/reviews/{reviewId}/status")
    suspend fun updateReviewStatus(
        @Header("Authorization") token: String,
        @Path("reviewId") reviewId: String,
        @Body body: UpdateReviewStatusRequest
    ): Response<MessageResponse>

    @POST("admin/genres")
    suspend fun createGenre(
        @Header("Authorization") token: String,
        @Body body: GenreRequest
    ): Response<GenreResponse>

    @PATCH("admin/genres/{genreId}")
    suspend fun updateGenre(
        @Header("Authorization") token: String,
        @Path("genreId") genreId: String,
        @Body body: GenreRequest
    ): Response<GenreResponse>

    @DELETE("admin/genres/{genreId}")
    suspend fun deleteGenre(
        @Header("Authorization") token: String,
        @Path("genreId") genreId: String
    ): Response<MessageResponse>

    @GET("admin/books")
    suspend fun getAdminBooks(
        @Header("Authorization") token: String
    ): retrofit2.Response<List<com.nolamarel.onlinelibrary.BookResponse>>

    @POST("admin/books")
    suspend fun createAdminBook(
        @Header("Authorization") token: String,
        @Body body: com.nolamarel.onlinelibrary.network.AdminBookRequest
    ): retrofit2.Response<com.nolamarel.onlinelibrary.BookResponse>

    @PATCH("admin/books/{bookId}")
    suspend fun updateAdminBook(
        @Header("Authorization") token: String,
        @Path("bookId") bookId: String,
        @Body body: com.nolamarel.onlinelibrary.network.AdminBookRequest
    ): retrofit2.Response<com.nolamarel.onlinelibrary.BookResponse>

    @DELETE("admin/books/{bookId}")
    suspend fun deleteAdminBook(
        @Header("Authorization") token: String,
        @Path("bookId") bookId: String
    ): retrofit2.Response<com.nolamarel.onlinelibrary.network.MessageResponse>

    @GET("admin/users")
    suspend fun getAdminUsers(
        @Header("Authorization") token: String
    ): Response<List<UserResponse>>

    @PATCH("admin/users/{userId}")
    suspend fun updateAdminUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Body body: AdminUpdateUserRequest
    ): Response<UserResponse>

    @DELETE("admin/users/{userId}")
    suspend fun deleteAdminUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Response<MessageResponse>

    @GET("admin/search-history")
    suspend fun getAdminSearchHistory(
        @Header("Authorization") token: String
    ): Response<List<AdminSearchHistoryResponse>>

    @GET("admin/userbooks")
    suspend fun getAdminUserBooks(
        @Header("Authorization") token: String
    ): Response<List<AdminUserBookResponse>>

    @PATCH("admin/userbooks/{userId}/{bookId}")
    suspend fun updateAdminUserBook(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Path("bookId") bookId: String,
        @Body body: AdminUpdateUserBookRequest
    ): Response<MessageResponse>

    @DELETE("admin/userbooks/{userId}/{bookId}")
    suspend fun deleteAdminUserBook(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Path("bookId") bookId: String
    ): Response<MessageResponse>

    @GET("admin/reviews")
    suspend fun getAdminReviews(
        @Header("Authorization") token: String
    ): Response<List<ReviewResponse>>

    @PATCH("admin/reviews/{reviewId}")
    suspend fun updateAdminReview(
        @Header("Authorization") token: String,
        @Path("reviewId") reviewId: String,
        @Body body: AdminUpdateReviewRequest
    ): Response<MessageResponse>

    @DELETE("admin/reviews/{reviewId}")
    suspend fun deleteAdminReview(
        @Header("Authorization") token: String,
        @Path("reviewId") reviewId: String
    ): Response<MessageResponse>

}