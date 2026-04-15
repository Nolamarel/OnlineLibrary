package com.nolamarel.onlinelibrary.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.AuthResponse
import com.nolamarel.onlinelibrary.LoginRequest
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            openMainScreen()
            return
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        setupClicks()
    }

    private fun setupClicks() {
        binding.signUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.signInButton.setOnClickListener {
            val email = binding.loginEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()

            if (!validateInput(email, password)) return@setOnClickListener

            loginUser(email, password)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        when {
            email.isEmpty() -> {
                binding.loginEt.error = "Введите email"
                binding.loginEt.requestFocus()
                return false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.loginEt.error = "Введите корректный email"
                binding.loginEt.requestFocus()
                return false
            }

            password.isEmpty() -> {
                binding.passwordEt.error = "Введите пароль"
                binding.passwordEt.requestFocus()
                return false
            }

            password.length < 6 -> {
                binding.passwordEt.error = "Пароль должен содержать минимум 6 символов"
                binding.passwordEt.requestFocus()
                return false
            }
        }

        return true
    }

    private fun loginUser(email: String, password: String) {
        setLoading(true)

        val call = ApiClient.authApi.login(LoginRequest(email, password))

        call.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(
                call: Call<AuthResponse>,
                response: Response<AuthResponse>
            ) {
                setLoading(false)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    sessionManager.saveToken(body.token)
                    sessionManager.saveUserId(body.userId)

                    Toast.makeText(
                        this@LoginActivity,
                        "Вход выполнен успешно",
                        Toast.LENGTH_SHORT
                    ).show()

                    openMainScreen()
                } else {
                    val errorText = response.errorBody()?.string()

                    Toast.makeText(
                        this@LoginActivity,
                        when {
                            response.code() == 401 -> "Неверный email или пароль"
                            !errorText.isNullOrBlank() -> "Ошибка входа: $errorText"
                            else -> "Не удалось выполнить вход"
                        },
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                setLoading(false)

                Toast.makeText(
                    this@LoginActivity,
                    "Ошибка сети. Проверьте подключение к интернету",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun setLoading(isLoading: Boolean) {
        binding.signInButton.isEnabled = !isLoading
        binding.signUp.isEnabled = !isLoading

        binding.signInButton.text = if (isLoading) {
            "Вход..."
        } else {
            getString(R.string.sign_in_btn)
        }
    }

    private fun openMainScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}