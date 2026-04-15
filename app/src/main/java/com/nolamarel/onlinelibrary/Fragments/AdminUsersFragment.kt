package com.nolamarel.onlinelibrary.Fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.UserResponse
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.FragmentAdminUsersBinding
import com.nolamarel.onlinelibrary.network.AdminUpdateUserRequest
import kotlinx.coroutines.launch

class AdminUsersFragment : Fragment() {

    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!

    private val users = mutableListOf<UserResponse>()
    private lateinit var adapter: AdminUsersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)

        binding.arrowBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        adapter = AdminUsersAdapter(
            items = users,
            onEditClick = { user -> showEditDialog(user) },
            onDeleteClick = { user -> showDeleteDialog(user) }
        )

        binding.usersRv.layoutManager = LinearLayoutManager(requireContext())
        binding.usersRv.adapter = adapter

        loadUsers()

        return binding.root
    }

    private fun getBearerToken(): String? {
        val token = SessionManager(requireContext()).getToken()
        return token?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }
    }

    private fun loadUsers() {
        binding.progressBar.visibility = View.VISIBLE

        val bearer = getBearerToken()
        if (bearer.isNullOrBlank()) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getAdminUsers(bearer)

                if (!isAdded || _binding == null) return@launch

                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    users.clear()
                    users.addAll(response.body().orEmpty())
                    adapter.notifyDataSetChanged()
                    binding.emptyText.visibility =
                        if (users.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось загрузить пользователей",
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

    private fun showEditDialog(user: UserResponse) {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 24, 40, 8)
        }

        val nameEt = EditText(requireContext()).apply {
            hint = "Имя"
            setText(user.name)
        }

        val emailEt = EditText(requireContext()).apply {
            hint = "Email"
            setText(user.email)
        }

        val roleGroup = RadioGroup(requireContext()).apply {
            orientation = RadioGroup.VERTICAL
        }

        val userRb = RadioButton(requireContext()).apply {
            text = "USER"
            id = View.generateViewId()
        }

        val moderatorRb = RadioButton(requireContext()).apply {
            text = "MODERATOR"
            id = View.generateViewId()
        }

        val adminRb = RadioButton(requireContext()).apply {
            text = "ADMIN"
            id = View.generateViewId()
        }

        roleGroup.addView(userRb)
        roleGroup.addView(moderatorRb)
        roleGroup.addView(adminRb)

        when (user.role.uppercase()) {
            "ADMIN" -> roleGroup.check(adminRb.id)
            "MODERATOR" -> roleGroup.check(moderatorRb.id)
            else -> roleGroup.check(userRb.id)
        }

        val activeCb = CheckBox(requireContext()).apply {
            text = "Активен"
            isChecked = user.isActive
        }

        container.addView(nameEt)
        container.addView(emailEt)
        container.addView(roleGroup)
        container.addView(activeCb)

        AlertDialog.Builder(requireContext())
            .setTitle("Редактировать пользователя")
            .setView(container)
            .setPositiveButton("Сохранить") { _, _ ->
                val selectedRole = when (roleGroup.checkedRadioButtonId) {
                    adminRb.id -> "ADMIN"
                    moderatorRb.id -> "MODERATOR"
                    else -> "USER"
                }

                val request = AdminUpdateUserRequest(
                    name = nameEt.text.toString().trim(),
                    email = emailEt.text.toString().trim(),
                    role = selectedRole,
                    isActive = activeCb.isChecked
                )

                updateUser(user.userId.toString(), request)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateUser(userId: String, request: AdminUpdateUserRequest) {
        val bearer = getBearerToken()
        if (bearer.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.updateAdminUser(
                    token = bearer,
                    userId = userId,
                    body = request
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Пользователь обновлён",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadUsers()
                } else {
                    val errorText = response.errorBody()?.string().orEmpty()
                    Toast.makeText(
                        requireContext(),
                        if (errorText.isNotBlank()) errorText else "Не удалось обновить пользователя",
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

    private fun showDeleteDialog(user: UserResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить пользователя")
            .setMessage("Удалить пользователя \"${user.name}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteUser(user.userId.toString())
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteUser(userId: String) {
        val bearer = getBearerToken()
        if (bearer.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.deleteAdminUser(
                    token = bearer,
                    userId = userId
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Пользователь удалён",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadUsers()
                } else {
                    val errorText = response.errorBody()?.string().orEmpty()
                    Toast.makeText(
                        requireContext(),
                        if (errorText.isNotBlank()) errorText else "Не удалось удалить пользователя",
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