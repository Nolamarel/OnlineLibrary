package com.nolamarel.onlinelibrary.Fragments

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.nolamarel.onlinelibrary.Adapters.myBooks.MyBook
import com.nolamarel.onlinelibrary.BookDetailsDTO
import com.nolamarel.onlinelibrary.DatabaseHelper1
import com.nolamarel.onlinelibrary.RetrofitInstance
import com.nolamarel.onlinelibrary.databinding.FragmentBookDescriptionBinding
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
class BookDescriptionFragment : Fragment() {

    private lateinit var binding: FragmentBookDescriptionBinding
    private var bookId: String = ""

    private lateinit var context: Context
    private lateinit var resolver: ContentResolver

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookDescriptionBinding.inflate(inflater, container, false)

        bookId = arguments?.getString("bookId") ?: ""
        context = requireContext()
        resolver = context.contentResolver

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
        RetrofitInstance.serverApi.getBookDetails(bookId).enqueue(object : Callback<BookDetailsDTO> {
            override fun onResponse(call: Call<BookDetailsDTO>, response: Response<BookDetailsDTO>) {
                if (response.isSuccessful) {
                    val book = response.body()
                    if (book != null) {
                        binding.bookName.text = book.title
                        binding.bookAuthor.text = book.author
                        binding.bookDesc.text = book.description
                        Glide.with(requireContext())
                            .load(book.image)
                            .into(binding.bookIv)
                    }
                } else {
                    Toast.makeText(context, "Книга не найдена", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BookDetailsDTO>, t: Throwable) {
                Toast.makeText(context, "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addBookToLibrary(bookId: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.serverApi.addBookToUser(bookId)
                Toast.makeText(context, "Книга успешно добавлена", Toast.LENGTH_SHORT).show()
            } catch (e: HttpException) {
                if (e.code() == 409) {
                    Toast.makeText(context, "Книга уже в библиотеке", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Не удалось добавить книгу: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
            }
        }
    }
}