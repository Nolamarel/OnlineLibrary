package com.nolamarel.onlinelibrary.Fragments.bottomnav.search

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nolamarel.onlinelibrary.Adapters.books.Book
import com.nolamarel.onlinelibrary.Adapters.books.BookAdapter
import com.nolamarel.onlinelibrary.Adapters.sections.Section
import com.nolamarel.onlinelibrary.Adapters.sections.SectionAdapter
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.Fragments.BookDescriptionFragment
import com.nolamarel.onlinelibrary.Fragments.BooksFragment
import com.nolamarel.onlinelibrary.OnItemClickListener.ItemClickListener
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.FragmentSearchBinding
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var lastSearchQuery: String = ""

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 800L
    }

    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { searchRequest() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.serchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                searchBook(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotEmpty()) {
                    searchDebounce()
                } else {
                    loadSections()
                }
                return true
            }
        })

        binding.serchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                loadSearchHistory()
            }
        }

//        binding.clearHistoryButton?.setOnClickListener {
//            clearHistory()
//        }

        loadSections()

        return binding.root
    }

    private fun getToken(): String? {
        val token = SessionManager(requireContext()).getToken()
        return token?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun searchRequest() {
        val query = binding.serchView.query.toString()
        if (query.isNotEmpty()) {
            searchBook(query)
        }
    }

    private fun searchBook(query: String) {
        lastSearchQuery = query

        binding.progressBar?.visibility = View.VISIBLE
        binding.booksRv.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.searchBooks(query)

                if (!response.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Ошибка сервера: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                binding.progressBar?.visibility = View.GONE

                val books = response.body()?.map {
                    Book(
                        bookId = it.externalId,
                        bookAuthor = it.author,
                        bookName = it.title,
                        bookImage = it.coverUrl,
                        source = "google"
                    )
                } ?: emptyList()

                if (books.isEmpty()) {
                    binding.placeholderNoResults?.visibility = View.VISIBLE
                } else {
                    binding.placeholderNoResults?.visibility = View.GONE
                    binding.booksRv.visibility = View.VISIBLE

                    binding.booksRv.layoutManager = LinearLayoutManager(context)
                    binding.booksRv.adapter = BookAdapter(
                        ArrayList(books),
                        object : ItemClickListener {
                            override fun onItemClick(position: Int) {
                                val selectedBookId = books[position].bookId
                                val fragment = BookDescriptionFragment().apply {
                                    arguments = Bundle().apply {
                                        putString("bookId", selectedBookId)
                                        putString("source", books[position].source)
                                    }
                                }
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit()
                            }
                        }
                    )
                }

            } catch (e: Exception) {
                binding.progressBar?.visibility = View.GONE
                binding.placeholderError?.visibility = View.VISIBLE
            }
        }
    }

    // 🔥 ЗАГРУЗКА ИСТОРИИ С СЕРВЕРА
    private fun loadSearchHistory() {
        val token = getToken() ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getSearchHistory(token)

                if (response.isSuccessful) {
                    val history = response.body()?.map { it.query } ?: emptyList()
                    showHistory(history)
                }

            } catch (_: Exception) {}
        }
    }

    // 🔥 ОЧИСТКА ИСТОРИИ
//    private fun clearHistory() {
//        val token = getToken() ?: return
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            try {
//                val response = ApiClient.serverApi.clearSearchHistory(token)
//
//                if (response.isSuccessful) {
//                    showHistory(emptyList())
//                    Toast.makeText(context, "История очищена", Toast.LENGTH_SHORT).show()
//                }
//
//            } catch (_: Exception) {}
//        }
//    }

    private fun showHistory(history: List<String>) {
        binding.booksRv.layoutManager = LinearLayoutManager(context)
        binding.booksRv.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(android.R.layout.simple_list_item_1, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val textView = holder.itemView.findViewById<TextView>(android.R.id.text1)
                val query = history[position]

                textView.text = query

                holder.itemView.setOnClickListener {
                    binding.serchView.setQuery(query, true)
                    searchBook(query)
                }
            }

            override fun getItemCount(): Int = history.size
        }
    }

    private fun loadSections() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getGenres()

                if (response.isSuccessful) {
                    val sections = response.body()?.map {
                        Section(
                            sectionName = it.name,
                            sectionIv = "",
                            sectionId = it.genreId.toString()
                        )
                    } ?: emptyList()

                    binding.booksRv.layoutManager = GridLayoutManager(context, 2)
                    binding.booksRv.adapter = SectionAdapter(sections as ArrayList<Section>, object : ItemClickListener {
                        override fun onItemClick(position: Int) {
                            val fragment = BooksFragment().apply {
                                arguments = Bundle().apply {
                                    putString("sectionId", sections[position].sectionId)
                                    putString("sectionName", sections[position].sectionName)
                                }
                            }
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    })
                }

            } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(
                requireContext(),
                "Ошибка: ${e.message}",
                Toast.LENGTH_LONG
            ).show()

            binding.progressBar?.visibility = View.GONE
            binding.placeholderError?.visibility = View.VISIBLE
        }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}