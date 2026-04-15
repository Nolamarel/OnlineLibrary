package com.nolamarel.onlinelibrary.Fragments

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.FragmentBookDescriptionBinding
import com.nolamarel.onlinelibrary.network.CreateReviewRequest
import com.nolamarel.onlinelibrary.network.ReviewResponse
import com.nolamarel.onlinelibrary.network.UpdateStatusRequest
import kotlinx.coroutines.launch

class BookDescriptionFragment : Fragment() {

    private var _binding: FragmentBookDescriptionBinding? = null
    private val binding get() = _binding!!

    private var bookId: String = ""
    private var source: String = "local"
    private var localBookId: Long? = null

    private lateinit var contextRef: Context

    private var titleFromArgs: String? = null
    private var authorFromArgs: String? = null
    private var descriptionFromArgs: String? = null
    private var coverUrlFromArgs: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDescriptionBinding.inflate(inflater, container, false)

        bookId = arguments?.getString("bookId") ?: ""
        source = arguments?.getString("source") ?: "local"
        contextRef = requireContext()

        titleFromArgs = arguments?.getString("title")
        authorFromArgs = arguments?.getString("author")
        descriptionFromArgs = arguments?.getString("description")
        coverUrlFromArgs = arguments?.getString("coverUrl")

        if (source == "local") {
            localBookId = bookId.toLongOrNull()
        }

        binding.bookDownloadBtn.text = "Добавить в библиотеку"
        binding.leaveReviewBtn.setOnClickListener {
            showReviewDialog()
        }

        if (!titleFromArgs.isNullOrBlank() || !authorFromArgs.isNullOrBlank()) {
            showBookFromArguments()
        } else {
            if (source == "google") {
                loadGoogleBook(bookId)
            } else {
                loadBook(bookId)
            }
        }

        binding.bookDownloadBtn.setOnClickListener {
            addToLibraryAndFavorite(bookId)
        }

        binding.arrowBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        updateReviewAvailability()
        checkLibraryState(bookId)

        return binding.root
    }

    private fun showBookFromArguments() {
        binding.bookName.text = titleFromArgs ?: "Без названия"
        binding.bookAuthor.text = authorFromArgs ?: "Неизвестный автор"
        binding.bookDesc.text = descriptionFromArgs ?: "Описание отсутствует"

        Glide.with(requireContext())
            .load(coverUrlFromArgs)
            .placeholder(R.drawable.books)
            .error(R.drawable.books)
            .into(binding.bookIv)
    }

    private fun loadBook(bookId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getBookById(bookId)

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful && response.body() != null) {
                    val book = response.body()!!

                    binding.bookName.text = book.title
                    binding.bookAuthor.text = book.author
                    binding.bookDesc.text = book.description ?: "Описание отсутствует"

                    Glide.with(requireContext())
                        .load(book.coverUrl)
                        .placeholder(R.drawable.books)
                        .error(R.drawable.books)
                        .into(binding.bookIv)

                    localBookId = book.bookId
                    updateReviewAvailability()
                    loadReviews()
                } else {
                    Toast.makeText(contextRef, "Книга не найдена", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(
                    contextRef,
                    "Ошибка подключения к серверу",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadGoogleBook(externalId: String) {
        val token = SessionManager(requireContext()).getToken()
        val bearer = token?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getGoogleBookById(
                    externalId = externalId,
                    token = bearer
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful && response.body() != null) {
                    val book = response.body()!!

                    binding.bookName.text = book.title
                    binding.bookAuthor.text = book.author
                    binding.bookDesc.text = book.description ?: "Описание отсутствует"

                    Glide.with(requireContext())
                        .load(book.coverUrl)
                        .placeholder(R.drawable.books)
                        .error(R.drawable.books)
                        .into(binding.bookIv)

                    updateReviewAvailability()
                } else {
                    Toast.makeText(contextRef, "Книга не найдена", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(contextRef, "Ошибка загрузки книги", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateReviewAvailability() {
        val available = localBookId != null

        binding.leaveReviewBtn.isEnabled = available
        binding.leaveReviewBtn.alpha = if (available) 1f else 0.5f

        if (!available) {
            binding.ratingSummaryTv.text = "Рейтинг: —"
            binding.reviewsText.text = "Отзывы доступны после добавления книги в библиотеку"
        }
    }

    private fun loadReviews() {
        val actualBookId = localBookId ?: run {
            binding.ratingSummaryTv.text = "Рейтинг: —"
            binding.reviewsText.text = "Отзывы доступны после добавления книги в библиотеку"
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getBookReviews(actualBookId.toString())

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful && response.body() != null) {
                    val payload = response.body()!!
                    val reviews: List<ReviewResponse> = payload.reviews

                    binding.ratingSummaryTv.text =
                        "Рейтинг: %.1f (%d отзывов)".format(
                            payload.averageRating,
                            payload.reviewsCount
                        )

                    binding.reviewsText.text = if (reviews.isEmpty()) {
                        "Пока нет отзывов"
                    } else {
                        reviews.joinToString("\n\n") { review ->
                            buildString {
                                append("${review.userName} • ${review.rating}/5")
                                append("\n")
                                append(review.comment ?: "Без текста")
                            }
                        }
                    }
                } else {
                    binding.ratingSummaryTv.text = "Рейтинг: —"
                    binding.reviewsText.text = "Не удалось загрузить отзывы"
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                binding.ratingSummaryTv.text = "Рейтинг: —"
                binding.reviewsText.text = "Ошибка загрузки отзывов"
            }
        }
    }

    private fun loadMyReviewAndShowActions(
        actualBookId: Long,
        onLoaded: (ReviewResponse?) -> Unit
    ) {
        val token = SessionManager(requireContext()).getToken()
        if (token.isNullOrBlank()) {
            onLoaded(null)
            return
        }

        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getMyReviewForBook(
                    token = bearer,
                    bookId = actualBookId.toString()
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful && response.body() != null) {
                    onLoaded(response.body())
                } else {
                    onLoaded(null)
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                onLoaded(null)
            }
        }
    }

    private fun showReviewDialog() {
        val token = SessionManager(requireContext()).getToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Нужно войти в аккаунт", Toast.LENGTH_LONG).show()
            return
        }

        val actualBookId = localBookId
        if (actualBookId == null) {
            Toast.makeText(
                requireContext(),
                "Сначала добавьте книгу в библиотеку",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        loadMyReviewAndShowActions(actualBookId) { existingReview ->
            val ratings = arrayOf("1", "2", "3", "4", "5")
            var selectedRating = existingReview?.rating ?: 5

            val commentEt = EditText(requireContext()).apply {
                hint = "Текст отзыва"
                minLines = 4
                maxLines = 6
                setPadding(40, 24, 40, 24)
                setText(existingReview?.comment.orEmpty())
            }

            val checkedItem = (existingReview?.rating ?: 5) - 1

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(if (existingReview == null) "Оставить отзыв" else "Редактировать отзыв")
                .setSingleChoiceItems(ratings, checkedItem) { _, which ->
                    selectedRating = which + 1
                }
                .setView(commentEt)
                .setPositiveButton("Сохранить") { _, _ ->
                    if (existingReview == null) {
                        submitReview(
                            bookId = actualBookId.toString(),
                            rating = selectedRating,
                            comment = commentEt.text.toString().trim()
                        )
                    } else {
                        updateReview(
                            bookId = actualBookId.toString(),
                            rating = selectedRating,
                            comment = commentEt.text.toString().trim()
                        )
                    }
                }
                .setNegativeButton("Отмена", null)

            if (existingReview != null) {
                dialog.setNeutralButton("Удалить") { _, _ ->
                    deleteReview(actualBookId.toString())
                }
            }

            dialog.show()
        }
    }

    private fun submitReview(bookId: String, rating: Int, comment: String) {
        val token = SessionManager(requireContext()).getToken() ?: return
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.createReview(
                    token = bearer,
                    bookId = bookId,
                    body = CreateReviewRequest(
                        rating = rating,
                        comment = comment.ifBlank { null }
                    )
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Отзыв сохранён", Toast.LENGTH_LONG).show()
                    loadReviews()
                } else {
                    Toast.makeText(requireContext(), "Не удалось сохранить отзыв", Toast.LENGTH_LONG).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(
                    requireContext(),
                    "Ошибка подключения к серверу",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateReview(bookId: String, rating: Int, comment: String) {
        val token = SessionManager(requireContext()).getToken() ?: return
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.updateReview(
                    token = bearer,
                    bookId = bookId,
                    body = CreateReviewRequest(
                        rating = rating,
                        comment = comment.ifBlank { null }
                    )
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Отзыв обновлён", Toast.LENGTH_LONG).show()
                    loadReviews()
                } else {
                    Toast.makeText(requireContext(), "Не удалось обновить отзыв", Toast.LENGTH_LONG).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(
                    requireContext(),
                    "Ошибка подключения к серверу",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun deleteReview(bookId: String) {
        val token = SessionManager(requireContext()).getToken() ?: return
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.deleteMyReview(
                    token = bearer,
                    bookId = bookId
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Отзыв удалён", Toast.LENGTH_LONG).show()
                    loadReviews()
                } else {
                    Toast.makeText(requireContext(), "Не удалось удалить отзыв", Toast.LENGTH_LONG).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(
                    requireContext(),
                    "Ошибка подключения к серверу",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun checkLibraryState(bookId: String) {
        val token = SessionManager(requireContext()).getToken() ?: return
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getMyBooks(bearer)

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    val books = response.body().orEmpty()

                    val foundBook = books.firstOrNull {
                        when (source) {
                            "google" -> it.externalId == bookId
                            else -> it.bookId.toString() == bookId
                        }
                    }

                    if (foundBook != null) {
                        localBookId = foundBook.bookId
                        updateReviewAvailability()
                        loadReviews()

                        binding.bookDownloadBtn.text = when (foundBook.status.uppercase()) {
                            "FAVORITE" -> "Уже в библиотеке"
                            "READING" -> "Читаю"
                            "COMPLETED" -> "Прочитано"
                            else -> "Уже в библиотеке"
                        }
                        binding.bookDownloadBtn.isEnabled = false
                    } else {
                        updateReviewAvailability()
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun addToLibraryAndFavorite(bookId: String) {
        val token = SessionManager(requireContext()).getToken()

        if (token.isNullOrBlank()) {
            Toast.makeText(contextRef, "Вы не авторизованы", Toast.LENGTH_SHORT).show()
            return
        }

        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val myBooksResponse = ApiClient.serverApi.getMyBooks(bearer)

                val alreadyExists = if (myBooksResponse.isSuccessful) {
                    myBooksResponse.body()?.any {
                        when (source) {
                            "google" -> it.externalId == bookId
                            else -> it.bookId.toString() == bookId
                        }
                    } == true
                } else {
                    false
                }

                if (!alreadyExists) {
                    val addResponse = ApiClient.serverApi.addBookToLibrary(
                        token = bearer,
                        bookId = bookId
                    )

                    if (!addResponse.isSuccessful) {
                        Toast.makeText(
                            contextRef,
                            "Не удалось добавить книгу в библиотеку",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }
                }

                val updatedBooksResponse = ApiClient.serverApi.getMyBooks(bearer)
                val savedBook = if (updatedBooksResponse.isSuccessful) {
                    updatedBooksResponse.body()?.firstOrNull {
                        when (source) {
                            "google" -> it.externalId == bookId
                            else -> it.bookId.toString() == bookId
                        }
                    }
                } else {
                    null
                }

                if (savedBook != null) {
                    localBookId = savedBook.bookId
                }

                val actualBookIdForStatus = localBookId?.toString() ?: bookId

                val statusResponse = ApiClient.serverApi.updateBookStatus(
                    token = bearer,
                    bookId = actualBookIdForStatus,
                    body = UpdateStatusRequest(status = "FAVORITE")
                )

                if (!isAdded || _binding == null) return@launch

                if (statusResponse.isSuccessful) {
                    binding.bookDownloadBtn.text = "Уже в библиотеке"
                    binding.bookDownloadBtn.isEnabled = false
                    updateReviewAvailability()
                    loadReviews()

                    Toast.makeText(
                        contextRef,
                        "Книга добавлена в библиотеку",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        contextRef,
                        "Не удалось обновить статус книги",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(
                    contextRef,
                    "Ошибка подключения к серверу",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}