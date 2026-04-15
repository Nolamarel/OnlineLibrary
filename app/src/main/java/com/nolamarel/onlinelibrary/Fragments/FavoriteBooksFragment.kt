package com.nolamarel.onlinelibrary.Fragments.bottomnav.profile

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.nolamarel.onlinelibrary.Adapters.myBooks.UserBookAdapter
import com.nolamarel.onlinelibrary.Adapters.myBooks.UserBookDto
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.Fragments.BookDescriptionFragment
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.FragmentFavoriteBooksBinding
import kotlinx.coroutines.launch

class FavoriteBooksFragment : Fragment() {

    private var _binding: FragmentFavoriteBooksBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UserBookAdapter
    private val favoriteBooks = mutableListOf<UserBookDto>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBooksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.arrowBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupRecycler()
        loadFavorites()
    }

    private fun setupRecycler() {
        adapter = UserBookAdapter(
            items = favoriteBooks,
            onBookClick = { book ->
                val fragment = BookDescriptionFragment().apply {
                    arguments = Bundle().apply {
                        putString("bookId", book.bookId.toString())
                        putString("source", "local")
                    }
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onDeleteClick = { book ->
                showDeleteDialog(book)
            }
        )

        binding.favoritesRv.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.favoritesRv.adapter = adapter
    }

    private fun loadFavorites() {
        val token = SessionManager(requireContext()).getToken()

        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Вы не авторизованы", Toast.LENGTH_SHORT).show()
            return
        }

        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getMyBooks(bearer)

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    val allBooks = response.body().orEmpty()
                    favoriteBooks.clear()
                    favoriteBooks.addAll(allBooks.filter { it.status == "FAVORITE" })
                    adapter.notifyDataSetChanged()

                    binding.emptyFavorites.visibility =
                        if (favoriteBooks.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось загрузить избранное",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(requireContext(), "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteDialog(book: UserBookDto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить из избранного")
            .setMessage("Удалить \"${book.title}\" из избранного?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteFavorite(book)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteFavorite(book: UserBookDto) {
        val token = SessionManager(requireContext()).getToken()

        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Вы не авторизованы", Toast.LENGTH_SHORT).show()
            return
        }

        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.deleteBookFromLibrary(
                    token = bearer,
                    bookId = book.bookId.toString()
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    favoriteBooks.removeAll { it.bookId == book.bookId }
                    adapter.notifyDataSetChanged()

                    binding.emptyFavorites.visibility =
                        if (favoriteBooks.isEmpty()) View.VISIBLE else View.GONE

                    Toast.makeText(
                        requireContext(),
                        "Книга удалена из избранного",
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
                Toast.makeText(requireContext(), "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}