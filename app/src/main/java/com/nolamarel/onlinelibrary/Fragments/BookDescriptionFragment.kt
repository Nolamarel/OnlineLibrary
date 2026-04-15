package com.nolamarel.onlinelibrary.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.network.UpdateStatusRequest
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.FragmentBookDescriptionBinding
import kotlinx.coroutines.launch

class BookDescriptionFragment : Fragment() {

    private lateinit var binding: FragmentBookDescriptionBinding
    private var bookId: String = ""
    private lateinit var contextRef: Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookDescriptionBinding.inflate(inflater, container, false)

        bookId = arguments?.getString("bookId") ?: ""
        contextRef = requireContext()

        binding.bookDownloadBtn.text = "В избранное"

        val source = arguments?.getString("source") ?: "local"

        if (source == "google") {
            loadGoogleBook(bookId)
        } else {
            loadBook(bookId)
        }
        checkFavoriteState(bookId)

        binding.bookDownloadBtn.setOnClickListener {
            addToFavorites(bookId)
        }

        binding.arrowBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    private fun loadBook(bookId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getBookById(bookId)

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
                } else {
                    Toast.makeText(contextRef, "Книга не найдена", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(contextRef, "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadGoogleBook(externalId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.searchBooks(externalId)

                if (response.isSuccessful) {
                    val book = response.body()?.firstOrNull { it.externalId == externalId }

                    if (book != null) {
                        binding.bookName.text = book.title
                        binding.bookAuthor.text = book.author
                        binding.bookDesc.text = book.description ?: "Описание отсутствует"

                        Glide.with(requireContext())
                            .load(book.coverUrl)
                            .placeholder(R.drawable.books)
                            .error(R.drawable.books)
                            .into(binding.bookIv)
                    } else {
                        Toast.makeText(contextRef, "Книга не найдена", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(contextRef, "Ошибка загрузки Google книги", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkFavoriteState(bookId: String) {
        val token = SessionManager(requireContext()).getToken()
        if (token.isNullOrBlank()) return

        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getMyBooks(bearer)

                if (response.isSuccessful) {
                    val foundBook = response.body()?.firstOrNull { it.bookId.toString() == bookId }
                    if (foundBook?.status == "FAVORITE") {
                        binding.bookDownloadBtn.text = "Уже в избранном"
                        binding.bookDownloadBtn.isEnabled = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addToFavorites(bookId: String) {
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
                    myBooksResponse.body()?.any { it.bookId.toString() == bookId } == true
                } else {
                    false
                }

                if (!alreadyExists) {
                    val addResponse = ApiClient.serverApi.addBookToLibrary(
                        token = bearer,
                        bookId = bookId
                    )

                    if (!addResponse.isSuccessful) {
                        Toast.makeText(contextRef, "Не удалось добавить книгу", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                }

                val statusResponse = ApiClient.serverApi.updateBookStatus(
                    token = bearer,
                    bookId = bookId,
                    body = UpdateStatusRequest(status = "FAVORITE")
                )

                if (statusResponse.isSuccessful) {
                    binding.bookDownloadBtn.text = "Уже в избранном"
                    binding.bookDownloadBtn.isEnabled = false
                    Toast.makeText(contextRef, "Книга добавлена в избранное", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(contextRef, "Не удалось добавить в избранное", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(contextRef, "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
