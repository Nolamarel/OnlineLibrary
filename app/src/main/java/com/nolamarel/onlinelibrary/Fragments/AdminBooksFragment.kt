package com.nolamarel.onlinelibrary.Fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.BookResponse
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.FragmentAdminBooksBinding
import com.nolamarel.onlinelibrary.network.AdminBookRequest
import kotlinx.coroutines.launch

class AdminBooksFragment : Fragment() {

    private var _binding: FragmentAdminBooksBinding? = null
    private val binding get() = _binding!!

    private val books = mutableListOf<BookResponse>()
    private lateinit var adapter: AdminBooksAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBooksBinding.inflate(inflater, container, false)

        binding.arrowBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.addBookBtn.setOnClickListener {
            showBookDialog()
        }

        adapter = AdminBooksAdapter(
            items = books,
            onEditClick = { book -> showBookDialog(book) },
            onDeleteClick = { book -> showDeleteDialog(book) }
        )

        binding.booksRv.layoutManager = LinearLayoutManager(requireContext())
        binding.booksRv.adapter = adapter

        loadBooks()

        return binding.root
    }

    private fun getBearerToken(): String? {
        val token = SessionManager(requireContext()).getToken()
        return token?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }
    }

    private fun loadBooks() {
        binding.progressBar.visibility = View.VISIBLE

        val bearer = getBearerToken()
        if (bearer.isNullOrBlank()) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getAdminBooks(bearer)

                if (!isAdded || _binding == null) return@launch

                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    books.clear()
                    books.addAll(response.body().orEmpty())
                    adapter.notifyDataSetChanged()
                    binding.emptyText.visibility =
                        if (books.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось загрузить книги",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Ошибка подключения к серверу",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showBookDialog(book: BookResponse? = null) {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 24, 40, 8)
        }

        val titleEt = EditText(requireContext()).apply {
            hint = "Название книги"
            setText(book?.title.orEmpty())
        }

        val authorEt = EditText(requireContext()).apply {
            hint = "Автор"
            setText(book?.author.orEmpty())
        }

        val descriptionEt = EditText(requireContext()).apply {
            hint = "Описание"
            setText(book?.description.orEmpty())
        }

        val coverUrlEt = EditText(requireContext()).apply {
            hint = "URL обложки"
            setText(book?.coverUrl.orEmpty())
        }

        val isbnEt = EditText(requireContext()).apply {
            hint = "ISBN"
            setText(book?.isbn.orEmpty())
        }

        val yearEt = EditText(requireContext()).apply {
            hint = "Год публикации"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(book?.publishedYear?.toString().orEmpty())
        }

        val pageCountEt = EditText(requireContext()).apply {
            hint = "Количество страниц"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(book?.pageCount?.toString().orEmpty())
        }

        val languageEt = EditText(requireContext()).apply {
            hint = "Язык"
            setText(book?.language.orEmpty())
        }

        container.addView(titleEt)
        container.addView(authorEt)
        container.addView(descriptionEt)
        container.addView(coverUrlEt)
        container.addView(isbnEt)
        container.addView(yearEt)
        container.addView(pageCountEt)
        container.addView(languageEt)

        AlertDialog.Builder(requireContext())
            .setTitle(if (book == null) "Добавить книгу" else "Редактировать книгу")
            .setView(container)
            .setPositiveButton("Сохранить") { _, _ ->
                val title = titleEt.text.toString().trim()
                val author = authorEt.text.toString().trim()

                if (title.isBlank() || author.isBlank()) {
                    Toast.makeText(
                        requireContext(),
                        "Название и автор обязательны",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setPositiveButton
                }

                val request = AdminBookRequest(
                    genreId = book?.genreId,
                    title = title,
                    author = author,
                    description = descriptionEt.text.toString().trim().ifBlank { null },
                    coverUrl = coverUrlEt.text.toString().trim().ifBlank { null },
                    isbn = isbnEt.text.toString().trim().ifBlank { null },
                    source = book?.source ?: "manual",
                    publishedYear = yearEt.text.toString().trim().toIntOrNull(),
                    pageCount = pageCountEt.text.toString().trim().toIntOrNull(),
                    language = languageEt.text.toString().trim().ifBlank { null }
                )

                if (book == null) {
                    createBook(request)
                } else {
                    updateBook(book.bookId.toString(), request)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun createBook(request: AdminBookRequest) {
        val bearer = getBearerToken()
        if (bearer.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.createAdminBook(
                    token = bearer,
                    body = request
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Книга добавлена", Toast.LENGTH_SHORT).show()
                    loadBooks()
                } else {
                    val errorText = response.errorBody()?.string().orEmpty()
                    Toast.makeText(
                        requireContext(),
                        if (errorText.isNotBlank()) errorText else "Не удалось добавить книгу",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(requireContext(), "Ошибка подключения к серверу", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateBook(bookId: String, request: AdminBookRequest) {
        val bearer = getBearerToken()
        if (bearer.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.updateAdminBook(
                    token = bearer,
                    bookId = bookId,
                    body = request
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Книга обновлена", Toast.LENGTH_SHORT).show()
                    loadBooks()
                } else {
                    val errorText = response.errorBody()?.string().orEmpty()
                    Toast.makeText(
                        requireContext(),
                        if (errorText.isNotBlank()) errorText else "Не удалось обновить книгу",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(requireContext(), "Ошибка подключения к серверу", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showDeleteDialog(book: BookResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить книгу")
            .setMessage("Удалить книгу \"${book.title}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteBook(book.bookId.toString())
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteBook(bookId: String) {
        val bearer = getBearerToken()
        if (bearer.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.deleteAdminBook(
                    token = bearer,
                    bookId = bookId
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Книга удалена", Toast.LENGTH_SHORT).show()
                    loadBooks()
                } else {
                    val errorText = response.errorBody()?.string().orEmpty()
                    Toast.makeText(
                        requireContext(),
                        if (errorText.isNotBlank()) errorText else "Не удалось удалить книгу",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(requireContext(), "Ошибка подключения к серверу", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}