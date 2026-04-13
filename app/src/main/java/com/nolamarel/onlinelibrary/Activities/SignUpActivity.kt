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

//class SignUpActivity : AppCompatActivity() {
//    private var binding: ActivitySignUpBinding? = null
//    var activity: Activity? = null
//    private var email: String? = null
//    private var password: String? = null
//    private var repPassword: String? = null
//    private var userName: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivitySignUpBinding.inflate(
//            layoutInflater
//        )
//        setContentView(binding!!.root)
//        activity = this
//        binding!!.arrowBack.setOnClickListener { (activity as SignUpActivity).finish() }
//
//        binding!!.signUpBt.setOnClickListener {
//            email = binding!!.email.text.toString()
//            password = binding!!.password.text.toString()
//            repPassword = binding!!.passwordRep.text.toString()
//            userName = binding!!.userName.text.toString()
//            if (checkPassword(password!!, repPassword!!, email!!, userName!!) == true) {
//                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email!!, password!!)
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            val userInfo = HashMap<String, String>()
//                            userInfo["email"] = email!!
//                            userInfo["profileImage"] = ""
//                            userInfo["books"] = ""
//                            userInfo["username"] = userName!!
//                            FirebaseDatabase.getInstance().reference.child("Users").child(
//                                FirebaseAuth.getInstance().currentUser!!.uid
//                            )
//                                .setValue(userInfo).addOnCompleteListener { databaseTask ->
//                                    if (databaseTask.isSuccessful) {
//                                        startActivity(
//                                            Intent(
//                                                this@SignUpActivity,
//                                                MainActivity::class.java
//                                            )
//                                        )
//                                    } else {
//                                        Toast.makeText(
//                                            this@SignUpActivity,
//                                            "Saving user error",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
//                                }
//                            val userRole = HashMap<String, String>()
//                            userRole["role"] = "user"
//                            FirebaseDatabase.getInstance().reference.child("Roles").child(
//                                FirebaseAuth.getInstance().currentUser!!.uid
//                            ).setValue(userRole)
//                        }
//                    }
//            }
//        }
//    }
//
//    fun checkPassword(pas1: String, pas2: String, email: String, userName: String): Boolean {
//        if (email.isEmpty() || userName.isEmpty() || pas1.isEmpty() || pas2.isEmpty()) {
//            Toast.makeText(this, "Fields can't be empty", Toast.LENGTH_SHORT).show()
//        } else {
//            if (pas1.length < 8) {
//                Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show()
//            } else {
//                if (pas1 != pas2) {
//                    Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
//                } else {
//                    return true
//                }
//            }
//        }
//        return false
//    }
//}

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
        val call = ApiClient.authApi.register(RegisterRequest(login, password, userName, email))
        call.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@SignUpActivity, "Email is not valid", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(this@SignUpActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.d("API", "Error: ${t.message}")
            }
        })
    }
}