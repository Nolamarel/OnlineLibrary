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
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.FragmentAdminGenresBinding
import com.nolamarel.onlinelibrary.network.GenreRequest
import com.nolamarel.onlinelibrary.network.GenreResponse
import kotlinx.coroutines.launch

class AdminGenresFragment : Fragment() {

    private var _binding: FragmentAdminGenresBinding? = null
    private val binding get() = _binding!!

    private val genres = mutableListOf<GenreResponse>()
    private lateinit var adapter: AdminGenresAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminGenresBinding.inflate(inflater, container, false)

        binding.arrowBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.addGenreBtn.setOnClickListener {
            showGenreDialog()
        }

        adapter = AdminGenresAdapter(
            items = genres,
            onEditClick = { genre -> showGenreDialog(genre) },
            onDeleteClick = { genre -> showDeleteDialog(genre) }
        )

        binding.genresRv.layoutManager = LinearLayoutManager(requireContext())
        binding.genresRv.adapter = adapter

        loadGenres()

        return binding.root
    }

    private fun getBearerToken(): String? {
        val token = SessionManager(requireContext()).getToken()
        return token?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }
    }

    private fun loadGenres() {
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getGenres()

                if (!isAdded || _binding == null) return@launch

                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    genres.clear()
                    genres.addAll(response.body().orEmpty())
                    adapter.notifyDataSetChanged()

                    binding.emptyText.visibility =
                        if (genres.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось загрузить жанры",
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

    private fun showGenreDialog(genre: GenreResponse? = null) {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 24, 40, 8)
        }

        val nameEt = EditText(requireContext()).apply {
            hint = "Название жанра"
            setText(genre?.name.orEmpty())
        }

        val imageUrlEt = EditText(requireContext()).apply {
            hint = "URL изображения"
            setText(genre?.imageUrl.orEmpty())
        }

        container.addView(nameEt)
        container.addView(imageUrlEt)

        AlertDialog.Builder(requireContext())
            .setTitle(if (genre == null) "Добавить жанр" else "Редактировать жанр")
            .setView(container)
            .setPositiveButton("Сохранить") { _, _ ->
                val name = nameEt.text.toString().trim()
                val imageUrl = imageUrlEt.text.toString().trim().ifBlank { null }

                if (name.isBlank()) {
                    Toast.makeText(
                        requireContext(),
                        "Название жанра обязательно",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setPositiveButton
                }

                if (genre == null) {
                    createGenre(name, imageUrl)
                } else {
                    updateGenre(genre.genreId, name, imageUrl)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun createGenre(name: String, imageUrl: String?) {
        val bearer = getBearerToken()
        if (bearer.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.createGenre(
                    token = bearer,
                    body = GenreRequest(
                        name = name,
                        imageUrl = imageUrl
                    )
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Жанр добавлен",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadGenres()
                } else {
                    val errorText = response.errorBody()?.string().orEmpty()
                    Toast.makeText(
                        requireContext(),
                        if (errorText.isNotBlank()) errorText else "Не удалось добавить жанр",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(
                    requireContext(),
                    "Ошибка подключения к серверу",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateGenre(genreId: Long, name: String, imageUrl: String?) {
        val bearer = getBearerToken()
        if (bearer.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.updateGenre(
                    token = bearer,
                    genreId = genreId.toString(),
                    body = GenreRequest(
                        name = name,
                        imageUrl = imageUrl
                    )
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Жанр обновлён",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadGenres()
                } else {
                    val errorText = response.errorBody()?.string().orEmpty()
                    Toast.makeText(
                        requireContext(),
                        if (errorText.isNotBlank()) errorText else "Не удалось обновить жанр",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(
                    requireContext(),
                    "Ошибка подключения к серверу",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showDeleteDialog(genre: GenreResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить жанр")
            .setMessage("Удалить жанр \"${genre.name}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteGenre(genre.genreId)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteGenre(genreId: Long) {
        val bearer = getBearerToken()
        if (bearer.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.deleteGenre(
                    token = bearer,
                    genreId = genreId.toString()
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Жанр удалён",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadGenres()
                } else {
                    val errorText = response.errorBody()?.string().orEmpty()
                    Toast.makeText(
                        requireContext(),
                        if (errorText.isNotBlank()) errorText else "Не удалось удалить жанр",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(
                    requireContext(),
                    "Ошибка подключения к серверу",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}