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
import com.nolamarel.onlinelibrary.databinding.FragmentEditProfileBinding
import com.nolamarel.onlinelibrary.network.UpdateProfileRequest
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        binding.arrowBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.saveProfileButton.setOnClickListener {
            validateAndSave()
        }

        loadCurrentProfile()

        return binding.root
    }

    private fun loadCurrentProfile() {
        val token = SessionManager(requireContext()).getToken()

        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getMe(bearer)

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    binding.nameEt.setText(user.name)
                    binding.emailEt.setText(user.email)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось загрузить профиль",
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

    private fun validateAndSave() {
        val name = binding.nameEt.text.toString().trim()
        val email = binding.emailEt.text.toString().trim()

        binding.nameEt.error = null
        binding.emailEt.error = null

        var isValid = true

        if (name.isBlank()) {
            binding.nameEt.error = "Введите имя"
            isValid = false
        }

        if (email.isBlank()) {
            binding.emailEt.error = "Введите email"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEt.error = "Введите корректный email"
            isValid = false
        }

        if (!isValid) return

        saveProfile(name, email)
    }

    private fun saveProfile(name: String, email: String) {
        val token = SessionManager(requireContext()).getToken()

        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        binding.saveProfileButton.isEnabled = false
        binding.saveProfileButton.text = "Сохранение..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.updateProfile(
                    token = bearer,
                    body = UpdateProfileRequest(
                        name = name,
                        email = email
                    )
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(
                        requireContext(),
                        "Профиль обновлён",
                        Toast.LENGTH_LONG
                    ).show()
                    parentFragmentManager.popBackStack()
                } else {
                    val errorText = response.errorBody()?.string().orEmpty()

                    val message = when {
                        response.code() == 409 ->
                            "Этот email уже используется"

                        errorText.isNotBlank() ->
                            errorText

                        else ->
                            "Не удалось обновить профиль"
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
                    binding.saveProfileButton.isEnabled = true
                    binding.saveProfileButton.text = "Сохранить"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}