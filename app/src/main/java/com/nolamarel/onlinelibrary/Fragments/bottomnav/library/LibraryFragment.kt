package com.nolamarel.onlinelibrary.Fragments.bottomnav.library

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.nolamarel.onlinelibrary.Activities.ReadingActivity
import com.nolamarel.onlinelibrary.Activities.RecentBookContent
import com.nolamarel.onlinelibrary.Adapters.myBooks.MyBookAdapter
import com.nolamarel.onlinelibrary.Adapters.myBooks.UserBookAdapter
import com.nolamarel.onlinelibrary.OnItemClickListener.ItemClickListener
import com.nolamarel.onlinelibrary.RetrofitInstance
import com.nolamarel.onlinelibrary.Adapters.myBooks.UserBookDTO
import com.nolamarel.onlinelibrary.databinding.FragmentLibraryBinding
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private var binding: FragmentLibraryBinding? = null
    private val books = mutableListOf<UserBookDTO>()
    private lateinit var adapter: UserBookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLibraryBinding.inflate(inflater, container, false)

        setupRecyclerView()
        loadBooksFromServer()

        return binding!!.root
    }

    private fun setupRecyclerView() {
        adapter = UserBookAdapter(books, object : ItemClickListener {
            override fun onItemClick(position: Int) {
                val book = books[position]
                val intent = Intent(requireContext(), ReadingActivity::class.java)
                RecentBookContent.name = book.title
                RecentBookContent.variable = book.localPath ?: ""
                startActivity(intent)
            }
        })

        binding!!.libraryRv.layoutManager = GridLayoutManager(context, 2)
        binding!!.libraryRv.adapter = adapter
    }

    private fun loadBooksFromServer() {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.serverApi.getUserBooks()

                if (response.isSuccessful) {
                    val bookList = response.body() ?: emptyList()
                    books.clear()
                    books.addAll(bookList)
                    adapter.notifyDataSetChanged()

                    if (bookList.isEmpty()) {
                        Toast.makeText(requireContext(), "У вас пока нет книг", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorText = response.errorBody()?.string()
                    Log.e("LibraryFragment", "Ошибка сервера: ${response.code()} $errorText")
                    Toast.makeText(
                        requireContext(),
                        "Ошибка загрузки книг: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Сервер недоступен", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}