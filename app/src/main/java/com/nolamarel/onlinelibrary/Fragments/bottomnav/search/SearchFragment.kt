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

    private var lastSearchedQuery: String = ""
    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 800L
    }

    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { searchRequest() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        setupSearch()
        loadSections()

        return binding.root
    }

    private fun setupSearch() {
        binding.serchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                handler.removeCallbacks(searchRunnable)

                val trimmed = query.trim()
                if (trimmed.isNotEmpty() && trimmed != lastSearchedQuery) {
                    searchBook(trimmed)
                }
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isBlank()) {
                    lastSearchedQuery = ""
                    loadSections()
                } else {
                    searchDebounce()
                }
                return true
            }
        })

        binding.serchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                loadSearchHistory()
            }
        }
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
        if (_binding == null) return

        val query = binding.serchView.query?.toString()?.trim().orEmpty()
        if (query.isNotEmpty() && query != lastSearchedQuery) {
            searchBook(query)
        }
    }

    private fun hidePlaceholders() {
        binding.progressBar?.visibility = View.GONE
        binding.placeholderError?.visibility = View.GONE
        binding.placeholderNoResults?.visibility = View.GONE
    }

    private fun searchBook(query: String) {
        lastSearchedQuery = query
        hidePlaceholders()
        binding.progressBar?.visibility = View.VISIBLE
        binding.booksRv.visibility = View.GONE

        val token = getToken()
        if (token.isNullOrBlank()) {
            binding.progressBar?.visibility = View.GONE
            binding.placeholderError?.visibility = View.VISIBLE
            Toast.makeText(
                requireContext(),
                "Сессия не найдена. Войдите снова",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.searchBooks(
                    query = query,
                    token = token
                )

                if (!isAdded || _binding == null) return@launch

                binding.progressBar?.visibility = View.GONE

                if (!response.isSuccessful) {
                    binding.placeholderError?.visibility = View.VISIBLE
                    Toast.makeText(
                        requireContext(),
                        "Ошибка сервера: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                val items = response.body().orEmpty()

                val books = items.mapNotNull { item ->
                    val resolvedBookId = item.externalId ?: item.bookId?.toString()

                    if (resolvedBookId.isNullOrBlank()) {
                        null
                    } else {
                        Book(
                            bookId = resolvedBookId,
                            bookAuthor = item.author,
                            bookName = item.title,
                            bookImage = item.coverUrl,
                            source = item.source
                        )
                    }
                }

                if (books.isEmpty()) {
                    binding.booksRv.visibility = View.GONE
                    binding.placeholderNoResults?.visibility = View.VISIBLE
                } else {
                    binding.placeholderNoResults?.visibility = View.GONE
                    binding.booksRv.visibility = View.VISIBLE
                    binding.booksRv.layoutManager = LinearLayoutManager(context)
                    binding.booksRv.adapter = BookAdapter(
                        ArrayList(books),
                        object : ItemClickListener {
                            override fun onItemClick(position: Int) {
                                val selectedItem = items[position]
                                val selectedBook = books[position]

                                val fragment = BookDescriptionFragment().apply {
                                    arguments = Bundle().apply {
                                        putString("bookId", selectedBook.bookId)
                                        putString("source", selectedBook.source)

                                        putString("title", selectedItem.title)
                                        putString("author", selectedItem.author)
                                        putString("description", selectedItem.description)
                                        putString("coverUrl", selectedItem.coverUrl)
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

            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch

                binding.progressBar?.visibility = View.GONE
                binding.booksRv.visibility = View.GONE
                binding.placeholderError?.visibility = View.VISIBLE

                Toast.makeText(
                    requireContext(),
                    "Ошибка подключения к серверу",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadSearchHistory() {
        val token = getToken() ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getSearchHistory(token)

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    val history = response.body()?.map { it.query } ?: emptyList()
                    if (history.isEmpty()) {
                        loadSections()
                    } else {
                        showHistory(history)
                    }
                }

            } catch (_: Exception) {
            }
        }
    }

    private fun showHistory(history: List<String>) {
        hidePlaceholders()

        binding.booksRv.visibility = View.VISIBLE
        binding.booksRv.layoutManager = LinearLayoutManager(context)
        binding.booksRv.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
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
        hidePlaceholders()
        binding.progressBar?.visibility = View.VISIBLE
        binding.booksRv.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getGenres()

                if (!isAdded || _binding == null) return@launch

                binding.progressBar?.visibility = View.GONE

                if (response.isSuccessful) {
                    val sections = response.body()?.map {
                        Section(
                            sectionName = it.name,
                            sectionIv = it.imageUrl,
                            sectionId = it.genreId.toString()
                        )
                    } ?: emptyList()

                    binding.booksRv.visibility = View.VISIBLE
                    binding.booksRv.layoutManager = GridLayoutManager(context, 2)
                    binding.booksRv.adapter = SectionAdapter(
                        ArrayList(sections),
                        object : ItemClickListener {
                            override fun onItemClick(position: Int) {
                                val selectedSection = sections[position]

                                val fragment = BooksFragment().apply {
                                    arguments = Bundle().apply {
                                        putString("sectionId", selectedSection.sectionId)
                                        putString("sectionName", selectedSection.sectionName)
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
                    binding.placeholderError?.visibility = View.VISIBLE
                    Toast.makeText(
                        requireContext(),
                        "Ошибка загрузки жанров",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch

                binding.progressBar?.visibility = View.GONE
                binding.placeholderError?.visibility = View.VISIBLE

                Toast.makeText(
                    requireContext(),
                    "Ошибка подключения к серверу",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        handler.removeCallbacks(searchRunnable)
        super.onDestroyView()
        _binding = null
    }
}