package com.nolamarel.onlinelibrary.Fragments.bottomnav.search

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
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
import com.nolamarel.onlinelibrary.databinding.FragmentSearchBinding
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var lastSearchQuery: String = ""

    private val PREFS_NAME = "search_prefs"
    private val HISTORY_KEY = "search_history"
    private val MAX_HISTORY_SIZE = 10

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { searchRequest() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root = binding.root

        val query = savedInstanceState?.getString("search_query") ?: ""
        binding.serchView.setQuery(query, false)

        binding.serchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                saveSearchQuery(query)
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

        binding.serchView.setOnCloseListener {
            binding.serchView.setQuery("", false)
            binding.serchView.clearFocus()
            loadSections()
            true
        }

        binding.serchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val history = getSearchHistory()
                if (history.isNotEmpty()) {
                    showHistory(history)
                }
            }
        }

        binding.clearHistoryButton?.setOnClickListener {
            val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(HISTORY_KEY).apply()
            showHistory(emptyList())
        }

        if (query.isEmpty()) {
            loadSections()
        } else {
            searchBook(query)
        }

        binding.retryButton?.setOnClickListener {
            searchBook(lastSearchQuery)
        }

        return root
    }

    private fun searchRequest() {
        val query = binding.serchView.query.toString()
        if (query.isNotEmpty()) {
            binding.placeholderError?.visibility = View.GONE
            binding.placeholderNoResults?.visibility = View.GONE
            binding.booksRv.visibility = View.GONE
            binding.progressBar?.visibility = View.VISIBLE
            handler.postDelayed({
                searchBook(query)
            }, 1000)
        } else {
            binding.placeholderNoResults?.visibility = View.VISIBLE
            binding.progressBar?.visibility = View.GONE
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("search_query", binding.serchView.query.toString())
    }

    private fun saveSearchQuery(query: String) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val history = prefs.getStringSet(HISTORY_KEY, LinkedHashSet())!!.toMutableList()

        history.remove(query)
        history.add(0, query)

        if (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(history.lastIndex)
        }

        prefs.edit().putStringSet(HISTORY_KEY, history.toSet()).apply()
    }

    private fun getSearchHistory(): List<String> {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(HISTORY_KEY, emptySet())!!.toList()
    }

    private fun showHistory(history: List<String>) {
        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(android.R.layout.simple_list_item_1, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val textView = holder.itemView.findViewById<TextView>(android.R.id.text1)
                textView.text = history[position]
                textView.setOnClickListener {
                    val query = history[position]
                    binding.serchView.setQuery(query, true)
                    searchBook(query)
                }
            }

            override fun getItemCount(): Int = history.size
        }

        binding.booksRv.layoutManager = LinearLayoutManager(context)
        binding.booksRv.adapter = adapter
    }

    private fun searchBook(query: String) {
        lastSearchQuery = query
        saveSearchQuery(query)

        binding.progressBar?.visibility = View.VISIBLE
        binding.booksRv.visibility = View.GONE
        binding.placeholderError?.visibility = View.GONE
        binding.placeholderNoResults?.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.searchBooks(query)

                binding.progressBar?.visibility = View.GONE

                val books: List<Book> = response.body()?.map {
                    Book(
                        bookId = it.bookId.toString(),
                        bookAuthor = it.author,
                        bookName = it.title,
                        bookImage = it.coverUrl
                    )
                } ?: emptyList()

                if (books.isEmpty()) {
                    binding.booksRv.visibility = View.GONE
                    binding.placeholderNoResults?.visibility = View.VISIBLE
                } else {
                    binding.booksRv.visibility = View.VISIBLE
                    binding.placeholderNoResults?.visibility = View.GONE

                    binding.booksRv.layoutManager = LinearLayoutManager(context)
                    binding.booksRv.adapter = BookAdapter(
                        ArrayList(books),
                        object : ItemClickListener {
                            override fun onItemClick(position: Int) {
                                val selectedBookId = books[position].bookId
                                val fragment = BookDescriptionFragment()
                                fragment.arguments = Bundle().apply {
                                    putString("bookId", selectedBookId)
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
                e.printStackTrace()
                binding.progressBar?.visibility = View.GONE
                binding.booksRv.visibility = View.GONE
                binding.placeholderError?.visibility = View.VISIBLE
            }
        }
    }

    private fun loadSections() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getGenres()

                if (response.isSuccessful) {
                    val sections = ArrayList<Section>()

                    response.body()?.forEach {
                        sections.add(
                            Section(
                                sectionName = it.name,
                                sectionIv = "",
                                sectionId = it.genreId.toString()
                            )
                        )
                    }

                    binding.booksRv.layoutManager = GridLayoutManager(context, 2)
                    binding.booksRv.adapter = SectionAdapter(sections, object : ItemClickListener {
                        override fun onItemClick(position: Int) {
                            val selectedSection = sections[position].sectionId
                            val selectedSectionName = sections[position].sectionName
                            val fragment = BooksFragment().apply {
                                arguments = Bundle().apply {
                                    putString("sectionId", selectedSection)
                                    putString("sectionName", selectedSectionName)
                                }
                            }
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    })
                } else {
                    binding.placeholderError?.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.placeholderError?.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}