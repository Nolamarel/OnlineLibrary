package com.nolamarel.onlinelibrary.Fragments.bottomnav.library

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.nolamarel.onlinelibrary.Activities.PdfReaderActivity
import com.nolamarel.onlinelibrary.Adapters.myBooks.UserBookAdapter
import com.nolamarel.onlinelibrary.Adapters.myBooks.UserBookDto
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.FragmentLibraryBinding
import com.nolamarel.onlinelibrary.network.CreateLocalBookRequest
import com.nolamarel.onlinelibrary.network.UpdateProgressRequest
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UserBookAdapter
    private val books = mutableListOf<UserBookDto>()

    private val pickPdfLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }

            uploadLocalPdfToLibrary(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        loadBooks()

        binding.btnAddPdf.setOnClickListener {
            pickPdfLauncher.launch(arrayOf("application/pdf"))
        }
    }

    private fun setupRecycler() {
        adapter = UserBookAdapter(
            items = books,
            onBookClick = { book ->
                openBook(book)
            },
            onDeleteClick = { book ->
                showDeleteDialog(book)
            }
        )

        binding.libraryRv.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.libraryRv.adapter = adapter
    }

    private fun loadBooks() {
        val token = SessionManager(requireContext()).getToken()

        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Вы не авторизованы", Toast.LENGTH_SHORT).show()
            return
        }

        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getMyBooks(bearer)

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    val body = response.body()

                    books.clear()
                    if (body != null) {
                        books.addAll(body.filter { it.status != "FAVORITE" })
                    }
                    adapter.notifyDataSetChanged()

                    if (books.isEmpty()) {
                        Toast.makeText(requireContext(), "Библиотека пуста", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка загрузки библиотеки: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(
                    requireContext(),
                    "Ошибка сети: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showDeleteDialog(book: UserBookDto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить книгу")
            .setMessage("Удалить \"${book.title}\" из библиотеки?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteBook(book)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteBook(book: UserBookDto) {
        val token = SessionManager(requireContext()).getToken()

        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Вы не авторизованы", Toast.LENGTH_SHORT).show()
            return
        }

        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.deleteBookFromLibrary(
                    token = bearer,
                    bookId = book.bookId.toString()
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    books.removeAll { it.bookId == book.bookId }
                    adapter.notifyDataSetChanged()

                    Toast.makeText(
                        requireContext(),
                        "Книга удалена из библиотеки",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val errorText = response.errorBody()?.string()
                    Toast.makeText(
                        requireContext(),
                        "Не удалось удалить книгу: ${errorText ?: response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(
                    requireContext(),
                    "Ошибка сети: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun uploadLocalPdfToLibrary(uri: Uri) {
        val token = SessionManager(requireContext()).getToken()

        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Вы не авторизованы", Toast.LENGTH_SHORT).show()
            return
        }

        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        val fileName = getFileName(uri) ?: "Локальный PDF"
        val title = fileName.substringBeforeLast(".pdf", fileName)
        val author = "Неизвестный автор"

        lifecycleScope.launch {
            try {
                val createBookResponse = ApiClient.serverApi.createLocalBook(
                    token = bearer,
                    body = CreateLocalBookRequest(
                        title = title,
                        author = author,
                        description = "Локально загруженный PDF"
                    )
                )

                if (!createBookResponse.isSuccessful || createBookResponse.body() == null) {
                    val errorText = createBookResponse.errorBody()?.string()
                    Toast.makeText(
                        requireContext(),
                        "Ошибка создания книги: ${createBookResponse.code()} ${errorText ?: ""}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val bookId = createBookResponse.body()!!.bookId

                val addBookResponse = ApiClient.serverApi.addBookToLibrary(
                    token = bearer,
                    bookId = bookId.toString()
                )

                if (!addBookResponse.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось добавить книгу в библиотеку",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val progressResponse = ApiClient.serverApi.updateReadingProgress(
                    token = bearer,
                    bookId = bookId.toString(),
                    body = UpdateProgressRequest(
                        progress = "0.00",
                        currentPage = 0,
                        locator = "page_0",
                        localFilePath = uri.toString(),
                        fileFormat = "pdf"
                    )
                )

                if (!progressResponse.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось сохранить путь к файлу",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                Toast.makeText(
                    requireContext(),
                    "PDF добавлен в библиотеку",
                    Toast.LENGTH_SHORT
                ).show()

                loadBooks()

                val intent = Intent(requireContext(), PdfReaderActivity::class.java).apply {
                    putExtra("book_id", bookId)
                    putExtra("book_title", title)
                    putExtra("current_page", 0)
                    putExtra("file_uri", uri.toString())
                }
                startActivity(intent)

            } catch (e: Exception) {
                e.printStackTrace()
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(
                    requireContext(),
                    "Ошибка загрузки PDF: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) {
                return it.getString(nameIndex)
            }
        }
        return null
    }

    private fun openBook(book: UserBookDto) {
        val localPath = book.localFilePath

        if (localPath.isNullOrBlank()) {
            Toast.makeText(
                requireContext(),
                "У книги нет локального файла для чтения",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val intent = Intent(requireContext(), PdfReaderActivity::class.java).apply {
            putExtra("book_id", book.bookId)
            putExtra("book_title", book.title ?: "Чтение PDF")
            putExtra("current_page", book.currentPage ?: 0)

            if (localPath.startsWith("content://")) {
                putExtra("file_uri", localPath)
            } else {
                putExtra("file_path", localPath)
            }
        }

        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}