package com.nolamarel.onlinelibrary.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.nolamarel.onlinelibrary.Adapters.books.Book
import com.nolamarel.onlinelibrary.Adapters.books.BookAdapter
import com.nolamarel.onlinelibrary.BookDTO
import com.nolamarel.onlinelibrary.OnItemClickListener
import com.nolamarel.onlinelibrary.OnItemClickListener.ItemClickListener
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.RetrofitInstance
import com.nolamarel.onlinelibrary.databinding.FragmentBooksBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        RetrofitInstance.serverApi.getBooksByGenre(sectionId).enqueue(object :
            Callback<List<BookDTO>> {
            override fun onResponse(call: Call<List<BookDTO>>, response: Response<List<BookDTO>>) {
                if (response.isSuccessful) {
                    val bookDTOs = response.body() ?: emptyList()
                    val books = ArrayList(bookDTOs.map {
                        Book(it.id, it.author, it.title, it.image)
                    })

                    binding.sectionName.text = "$sectionName"

                    binding.booksRv.layoutManager = LinearLayoutManager(context)
                    binding.booksRv.adapter = BookAdapter(books, object : OnItemClickListener.ItemClickListener {
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
                    })
                } else {
                    Toast.makeText(context, "Ошибка загрузки книг", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<BookDTO>>, t: Throwable) {
                Toast.makeText(context, "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

