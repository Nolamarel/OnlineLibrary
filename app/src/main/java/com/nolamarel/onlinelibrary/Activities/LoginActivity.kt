package com.nolamarel.onlinelibrary.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.AuthResponse
import com.nolamarel.onlinelibrary.LoginRequest
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//class LoginActivity : AppCompatActivity() {
//    private var binding: ActivityLoginBinding? = null
//    private var toolbar: Toolbar? = null
//    private var email: String? = null
//    private var password: String? = null
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityLoginBinding.inflate(
//            layoutInflater
//        )
//        setContentView(binding!!.root)
//
//        toolbar = findViewById(R.id.myToolbar)
//        setSupportActionBar(toolbar)
//        title = "Sign In"
//
//        binding!!.signUp.setOnClickListener {
//            startActivity(
//                Intent(
//                    this@LoginActivity,
//                    SignUpActivity::class.java
//                )
//            )
//        }
//
//        binding!!.signInButton.setOnClickListener {
//            email = binding!!.emailEt.text.toString()
//            password = binding!!.passwordEt.text.toString()
//            if (email!!.isEmpty() || password!!.isEmpty()) {
//                Toast.makeText(this@LoginActivity, "This fields can't be empty", Toast.LENGTH_SHORT)
//                    .show()
//            } else if (password!!.length < 8) {
//                Toast.makeText(this@LoginActivity, "Password too short", Toast.LENGTH_SHORT).show()
//            } else {
//                FirebaseAuth.getInstance().signInWithEmailAndPassword(email!!, password!!)
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
//                        } else {
//                            Toast.makeText(
//                                this@LoginActivity,
//                                "You need to sign up first",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
//            }
//        }
//    }
//}

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
            val login = binding.loginEt.text.toString()  // новое поле логина
            val password = binding.passwordEt.text.toString()

            if (login.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "This fields can't be empty", Toast.LENGTH_SHORT).show()
            } else if (password.length < 8) {
                Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(login, password)
            }
        }
    }

    private fun loginUser(login: String, password: String) {
        val call = ApiClient.authApi.login(LoginRequest(login, password))
        call.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    val userId = response.body()!!.userId  // ← добавь это

                    // Сохраняем токен
                    val sharedPref = getSharedPreferences("auth", Context.MODE_PRIVATE)
                    sharedPref.edit()
                        .putString("token", token)
                        .putString("userId", userId)  // ← и это
                        .apply()
                    Log.d("LoginActivity", "Получен userId: $userId")

                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.d("LoginActivity", "Network error: ${t.message}")
            }
        })
    }
}