package com.nolamarel.onlinelibrary.Activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.AuthResponse
import com.nolamarel.onlinelibrary.RegisterRequest
import com.nolamarel.onlinelibrary.databinding.ActivitySignUpBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.arrowBack.setOnClickListener { finish() }

        binding.signUpBt.setOnClickListener {
            val login = binding.login.text.toString() // новое поле логина
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val repPassword = binding.passwordRep.text.toString()
            val userName = binding.userName.text.toString()

            if (checkPassword(password, repPassword, email, userName, login)) {
                registerUser(login, password, email, userName)
            }
        }
    }

    private fun checkPassword(
        pas1: String,
        pas2: String,
        email: String,
        userName: String,
        login: String
    ): Boolean {
        return when {
            login.isEmpty() || email.isEmpty() || userName.isEmpty() || pas1.isEmpty() || pas2.isEmpty() -> {
                Toast.makeText(this, "Fields can't be empty", Toast.LENGTH_SHORT).show()
                false
            }
            pas1.length < 8 -> {
                Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show()
                false
            }
            pas1 != pas2 -> {
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun registerUser(login: String, password: String, email: String, userName: String) {
        val displayName = if (userName.isNotBlank()) userName else login
        Log.d("SignUpActivity", "REGISTER name=$displayName email=${email.trim()} password=$password")
        Toast.makeText(this, "Отправка на сервер", Toast.LENGTH_SHORT).show()
        val call = ApiClient.authApi.register(
            RegisterRequest(
                name = displayName,
                email = email.trim(),
                password = password
            )
        )

        call.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@SignUpActivity, "Регистрация успешна", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                    finish()
                } else {
                    val errorText = response.errorBody()?.string()
                    Toast.makeText(
                        this@SignUpActivity,
                        "Ошибка регистрации: ${response.code()} ${errorText ?: ""}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(
                    this@SignUpActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}