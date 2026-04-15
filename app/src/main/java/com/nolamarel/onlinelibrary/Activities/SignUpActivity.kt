package com.nolamarel.onlinelibrary.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.AuthResponse
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.RegisterRequest
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.ActivitySignUpBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var sessionManager: SessionManager
    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        binding.arrowBack.setOnClickListener { finish() }

        binding.signUpBt.setOnClickListener {
            val userName = binding.userName.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val login = binding.login.text.toString().trim()
            val password = binding.password.text.toString().trim()
            val repeatPassword = binding.passwordRep.text.toString().trim()

            if (!validateInput(userName, email, login, password, repeatPassword)) {
                return@setOnClickListener
            }

            registerUser(
                login = login,
                password = password,
                email = email,
                userName = userName
            )
        }
    }

    private fun validateInput(
        userName: String,
        email: String,
        login: String,
        password: String,
        repeatPassword: String
    ): Boolean {
        when {
            userName.isEmpty() -> {
                binding.userName.error = "Введите имя пользователя"
                binding.userName.requestFocus()
                return false
            }

            userName.length < 2 -> {
                binding.userName.error = "Имя должно содержать минимум 2 символа"
                binding.userName.requestFocus()
                return false
            }

            email.isEmpty() -> {
                binding.email.error = "Введите email"
                binding.email.requestFocus()
                return false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.email.error = "Введите корректный email"
                binding.email.requestFocus()
                return false
            }

            login.isEmpty() -> {
                binding.login.error = "Введите логин"
                binding.login.requestFocus()
                return false
            }

            login.length < 3 -> {
                binding.login.error = "Логин должен содержать минимум 3 символа"
                binding.login.requestFocus()
                return false
            }

            password.isEmpty() -> {
                binding.password.error = "Введите пароль"
                binding.password.requestFocus()
                return false
            }

            password.length < 6 -> {
                binding.password.error = "Пароль должен содержать минимум 6 символов"
                binding.password.requestFocus()
                return false
            }

            repeatPassword.isEmpty() -> {
                binding.passwordRep.error = "Повторите пароль"
                binding.passwordRep.requestFocus()
                return false
            }

            password != repeatPassword -> {
                binding.passwordRep.error = "Пароли не совпадают"
                binding.passwordRep.requestFocus()
                return false
            }
        }

        return true
    }

    private fun registerUser(
        login: String,
        password: String,
        email: String,
        userName: String
    ) {
        setLoading(true)

        val displayName = if (userName.isNotBlank()) userName else login

        val call = ApiClient.authApi.register(
            RegisterRequest(
                name = displayName,
                email = email,
                password = password
            )
        )

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
                        this@SignUpActivity,
                        "Регистрация прошла успешно",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
                    finish()
                } else {
                    val errorText = response.errorBody()?.string()

                    Toast.makeText(
                        this@SignUpActivity,
                        when {
                            response.code() == 409 -> "Пользователь с таким email уже существует"
                            !errorText.isNullOrBlank() -> "Ошибка регистрации: $errorText"
                            else -> "Не удалось зарегистрироваться"
                        },
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                setLoading(false)

                Toast.makeText(
                    this@SignUpActivity,
                    "Ошибка сети. Проверьте подключение к интернету",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun setLoading(isLoading: Boolean) {
        binding.signUpBt.isEnabled = !isLoading
        binding.arrowBack.isEnabled = !isLoading

        binding.signUpBt.text = if (isLoading) {
            "Регистрация..."
        } else {
            getString(R.string.sign_in_do)
        }
    }
}