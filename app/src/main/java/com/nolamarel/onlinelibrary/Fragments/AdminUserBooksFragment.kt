package com.nolamarel.onlinelibrary.Fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
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
import com.nolamarel.onlinelibrary.databinding.FragmentAdminUserBooksBinding
import com.nolamarel.onlinelibrary.network.AdminUpdateUserBookRequest
import com.nolamarel.onlinelibrary.network.AdminUserBookResponse
import kotlinx.coroutines.launch

class AdminUserBooksFragment : Fragment() {

    private var _binding: FragmentAdminUserBooksBinding? = null
    private val binding get() = _binding!!

    private val items = mutableListOf<AdminUserBookResponse>()
    private lateinit var adapter: AdminUserBooksAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUserBooksBinding.inflate(inflater, container, false)

        binding.arrowBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        adapter = AdminUserBooksAdapter(
            items = items,
            onEditClick = { item -> showEditDialog(item) },
            onDeleteClick = { item -> showDeleteDialog(item) }
        )

        binding.userBooksRv.layoutManager = LinearLayoutManager(requireContext())
        binding.userBooksRv.adapter = adapter

        loadUserBooks()

        return binding.root
    }

    private fun getBearerToken(): String? {
        val token = SessionManager(requireContext()).getToken()
        return token?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }
    }

    private fun loadUserBooks() {
        binding.progressBar.visibility = View.VISIBLE

        val bearer = getBearerToken()
        if (bearer.isNullOrBlank()) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getAdminUserBooks(bearer)

                if (!isAdded || _binding == null) return@launch

                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    items.clear()
                    items.addAll(response.body().orEmpty())
                    adapter.notifyDataSetChanged()
                    binding.emptyText.visibility =
                        if (items.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось загрузить userbooks",
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

    private fun showEditDialog(item: AdminUserBookResponse) {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 24, 40, 8)
        }

        val statusEt = EditText(requireContext()).apply {
            hint = "Статус"
            setText(item.status)
        }

        val progressEt = EditText(requireContext()).apply {
            hint = "Прогресс"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(item.progress)
        }

        val currentPageEt = EditText(requireContext()).apply {
            hint = "Текущая страница"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(item.currentPage?.toString().orEmpty())
        }

        container.addView(statusEt)
        container.addView(progressEt)
        container.addView(currentPageEt)

        AlertDialog.Builder(requireContext())
            .setTitle("Редактировать запись")
            .setView(container)
            .setPositiveButton("Сохранить") { _, _ ->
                val request = AdminUpdateUserBookRequest(
                    status = statusEt.text.toString().trim(),
                    progress = progressEt.text.toString().trim(),
                    currentPage = currentPageEt.text.toString().trim().toIntOrNull()
                )

                updateUserBook(
                    userId = item.userId.toString(),
                    bookId = item.bookId.toString(),
                    request = request
                )
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateUserBook(
        userId: String,
        bookId: String,
        request: AdminUpdateUserBookRequest
    ) {
        val bearer = getBearerToken()
        if (bearer.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.updateAdminUserBook(
                    token = bearer,
                    userId = userId,
                    bookId = bookId,
                    body = request
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Запись обновлена",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadUserBooks()
                } else {
                    val errorText = response.errorBody()?.string().orEmpty()
                    Toast.makeText(
                        requireContext(),
                        if (errorText.isNotBlank()) errorText else "Не удалось обновить запись",
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

    private fun showDeleteDialog(item: AdminUserBookResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить запись")
            .setMessage("Удалить запись пользователя ${item.userName} для книги \"${item.bookTitle}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteUserBook(
                    userId = item.userId.toString(),
                    bookId = item.bookId.toString()
                )
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteUserBook(userId: String, bookId: String) {
        val bearer = getBearerToken()
        if (bearer.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.deleteAdminUserBook(
                    token = bearer,
                    userId = userId,
                    bookId = bookId
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Запись удалена",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadUserBooks()
                } else {
                    val errorText = response.errorBody()?.string().orEmpty()
                    Toast.makeText(
                        requireContext(),
                        if (errorText.isNotBlank()) errorText else "Не удалось удалить запись",
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