package com.nolamarel.onlinelibrary.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = findViewById(R.id.myToolbar)
        setSupportActionBar(toolbar)
        title = "Sign In"

        binding.signUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.signInButton.setOnClickListener {
            val email = binding.loginEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            } else if (password.length < 8) {
                Toast.makeText(this, "Пароль слишком короткий", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        binding.signInButton.isEnabled = false

        val call = ApiClient.authApi.login(LoginRequest(email, password))

        call.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(
                call: Call<AuthResponse>,
                response: Response<AuthResponse>
            ) {
                binding.signInButton.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    val sessionManager = SessionManager(this@LoginActivity)
                    sessionManager.saveToken(body.token)
                    sessionManager.saveUserId(body.userId)

                    Log.d("LoginActivity", "Получен userId: ${body.userId}")

                    Toast.makeText(
                        this@LoginActivity,
                        "Вход выполнен успешно",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    val errorText = response.errorBody()?.string()
                    Toast.makeText(
                        this@LoginActivity,
                        "Ошибка входа: ${errorText ?: response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                binding.signInButton.isEnabled = true

                Toast.makeText(
                    this@LoginActivity,
                    "Ошибка сети: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()

                Log.d("LoginActivity", "Network error: ${t.message}")
            }
        })
    }
}