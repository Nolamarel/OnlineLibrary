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
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.FragmentBookDescriptionBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException

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

        loadBook(bookId)

        binding.bookDownloadBtn.setOnClickListener {
            addBookToLibrary(bookId)
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

                if (response.isSuccessful) {
                    val book = response.body()
                    if (book != null) {
                        binding.bookName.text = book.title
                        binding.bookAuthor.text = book.author
                        binding.bookDesc.text = book.description ?: ""

                        Glide.with(requireContext())
                            .load(book.image)
                            .into(binding.bookIv)
                    } else {
                        Toast.makeText(contextRef, "Книга не найдена", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(contextRef, "Книга не найдена", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(contextRef, "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addBookToLibrary(bookId: String) {
        val token = SessionManager(requireContext()).getToken()

        if (token.isNullOrBlank()) {
            Toast.makeText(contextRef, "Вы не авторизованы", Toast.LENGTH_SHORT).show()
            return
        }

        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.addBookToLibrary(
                    token = bearer,
                    bookId = bookId
                )

                if (response.isSuccessful) {
                    Toast.makeText(contextRef, "Книга успешно добавлена", Toast.LENGTH_SHORT).show()
                } else if (response.code() == 409) {
                    Toast.makeText(contextRef, "Книга уже в библиотеке", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        contextRef,
                        "Не удалось добавить книгу: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: HttpException) {
                if (e.code() == 409) {
                    Toast.makeText(contextRef, "Книга уже в библиотеке", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        contextRef,
                        "Не удалось добавить книгу: ${e.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(contextRef, "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
            }
        }
    }
}