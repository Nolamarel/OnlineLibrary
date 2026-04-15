package com.nolamarel.onlinelibrary.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.nolamarel.onlinelibrary.databinding.FragmentSupportServiceBinding

class SupportServiceFragment : Fragment() {

    private var _binding: FragmentSupportServiceBinding? = null
    private val binding get() = _binding!!

    private val supportEmail = "support@onlinelibrary.local"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSupportServiceBinding.inflate(inflater, container, false)

        binding.arrowBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.supSerMessageEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val characterCount = s?.length ?: 0
                binding.supSerSymbolNow.text = "$characterCount/20000"
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        binding.button.setOnClickListener {
            validateAndSend()
        }

        return binding.root
    }

    private fun validateAndSend() {
        val problem = binding.supSerThemeEt.text.toString().trim()
        val email = binding.supSerEmailEt.text.toString().trim()
        val message = binding.supSerMessageEt.text.toString().trim()

        clearErrors()

        var isValid = true

        if (problem.isEmpty()) {
            binding.supSerThemeEt.error = "Введите тему обращения"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.supSerEmailEt.error = "Введите email"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.supSerEmailEt.error = "Введите корректный email"
            isValid = false
        }

        if (message.length < 20) {
            binding.supSerMessageEt.error = "Слишком короткое описание"
            isValid = false
        }

        if (!isValid) return

        sendSupportEmail(
            problemText = problem,
            emailText = email,
            messageText = message
        )
    }

    private fun clearErrors() {
        binding.supSerThemeEt.error = null
        binding.supSerEmailEt.error = null
        binding.supSerMessageEt.error = null
    }

    private fun sendSupportEmail(
        problemText: String,
        emailText: String,
        messageText: String
    ) {
        val subject = "Обращение в поддержку: $problemText"
        val body = buildString {
            appendLine("Тема: $problemText")
            appendLine("Email для ответа: $emailText")
            appendLine()
            appendLine("Сообщение:")
            appendLine(messageText)
        }

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$supportEmail")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(
                requireContext(),
                "На устройстве нет почтового приложения",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}