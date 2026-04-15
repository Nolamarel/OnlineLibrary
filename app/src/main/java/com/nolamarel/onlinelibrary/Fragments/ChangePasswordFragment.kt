package com.nolamarel.onlinelibrary.Fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.FragmentChangePasswordBinding
import com.nolamarel.onlinelibrary.network.ChangePasswordRequest
import kotlinx.coroutines.launch

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)

        binding.arrowBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.button.setOnClickListener {
            validateAndSubmit()
        }

        return binding.root
    }

    private fun validateAndSubmit() {
        val email = binding.myEmail.text.toString().trim()
        val oldPassword = binding.oldPassword.text.toString().trim()
        val newPassword = binding.newPassword.text.toString().trim()
        val repeatPassword = binding.newPasswordRep.text.toString().trim()

        clearErrors()

        var isValid = true

        if (email.isEmpty()) {
            binding.myEmail.error = "Введите email"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.myEmail.error = "Введите корректный email"
            isValid = false
        }

        if (oldPassword.isEmpty()) {
            binding.oldPassword.error = "Введите текущий пароль"
            isValid = false
        }

        if (newPassword.isEmpty()) {
            binding.newPassword.error = "Введите новый пароль"
            isValid = false
        } else if (newPassword.length < 6) {
            binding.newPassword.error = "Пароль должен содержать минимум 6 символов"
            isValid = false
        }

        if (repeatPassword.isEmpty()) {
            binding.newPasswordRep.error = "Повторите новый пароль"
            isValid = false
        }

        if (oldPassword.isNotEmpty() && newPassword.isNotEmpty() && oldPassword == newPassword) {
            binding.newPassword.error = "Новый пароль должен отличаться от старого"
            isValid = false
        }

        if (newPassword.isNotEmpty() && repeatPassword.isNotEmpty() && newPassword != repeatPassword) {
            binding.newPasswordRep.error = "Пароли не совпадают"
            isValid = false
        }

        if (!isValid) return

        submitChangePassword(
            email = email,
            oldPassword = oldPassword,
            newPassword = newPassword
        )
    }

    private fun clearErrors() {
        binding.myEmail.error = null
        binding.oldPassword.error = null
        binding.newPassword.error = null
        binding.newPasswordRep.error = null
    }

    private fun submitChangePassword(
        email: String,
        oldPassword: String,
        newPassword: String
    ) {
        val token = SessionManager(requireContext()).getToken()

        if (token.isNullOrBlank()) {
            Toast.makeText(
                requireContext(),
                "Сессия не найдена. Войдите снова",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        binding.button.isEnabled = false
        binding.button.text = "Сохранение..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.changePassword(
                    token = bearer,
                    body = ChangePasswordRequest(
                        email = email,
                        oldPassword = oldPassword,
                        newPassword = newPassword
                    )
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Пароль успешно изменён",
                        Toast.LENGTH_LONG
                    ).show()
                    parentFragmentManager.popBackStack()
                } else {
                    val errorText = response.errorBody()?.string().orEmpty()

                    val message = when {
                        response.code() == 400 && errorText.contains("старый пароль", ignoreCase = true) ->
                            "Неверный текущий пароль"

                        response.code() == 400 && errorText.contains("email", ignoreCase = true) ->
                            "Email не совпадает с аккаунтом"

                        response.code() == 401 ->
                            "Сессия истекла. Войдите снова"

                        response.code() == 404 ->
                            "Пользователь не найден"

                        errorText.isNotBlank() ->
                            "Ошибка: $errorText"

                        else ->
                            "Не удалось изменить пароль"
                    }

                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(
                    requireContext(),
                    "Ошибка подключения к серверу",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                if (isAdded && _binding != null) {
                    binding.button.isEnabled = true
                    binding.button.text = "Изменить пароль"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}