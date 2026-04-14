package com.nolamarel.onlinelibrary.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nolamarel.onlinelibrary.Adapters.books.Book
import com.nolamarel.onlinelibrary.Adapters.books.BookAdapter
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.OnItemClickListener
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.databinding.FragmentBooksBinding
import kotlinx.coroutines.launch

class BooksFragment : Fragment() {
    private lateinit var binding: FragmentBooksBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBooksBinding.inflate(inflater, container, false)

        binding.arrowBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val sectionId = arguments?.getString("sectionId") ?: ""
        val sectionName = arguments?.getString("sectionName") ?: ""

        loadBooks(sectionId, sectionName)

        return binding.root
    }

    private fun loadBooks(sectionId: String, sectionName: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getBooksByGenre(sectionId)

                if (response.isSuccessful) {
                    val bookDTOs = response.body().orEmpty()
                    val books = ArrayList(bookDTOs.map {
                        Book(
                            bookId = it.bookId.toString(),
                            bookAuthor = it.author,
                            bookName = it.title,
                            bookImage = it.coverUrl
                        )
                    })

                    binding.sectionName.text = sectionName
                    binding.booksRv.layoutManager = LinearLayoutManager(context)
                    binding.booksRv.adapter = BookAdapter(
                        books,
                        object : OnItemClickListener.ItemClickListener {
                            override fun onItemClick(position: Int) {
                                val selectedBookId = books[position].bookId
                                val fragment = BookDescriptionFragment().apply {
                                    arguments = Bundle().apply {
                                        putString("bookId", selectedBookId)
                                    }
                                }
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit()
                            }
                        }
                    )
                } else {
                    Toast.makeText(context, "Ошибка загрузки книг", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
            }
        }
    }
}