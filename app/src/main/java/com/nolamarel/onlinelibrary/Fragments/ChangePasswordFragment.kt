package com.nolamarel.onlinelibrary.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.nolamarel.onlinelibrary.databinding.FragmentChangePasswordBinding

class ChangePasswordFragment : Fragment() {
    private var binding: FragmentChangePasswordBinding? = null
    private var mAuth: FirebaseAuth? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)

        mAuth = FirebaseAuth.getInstance()
        binding!!.arrowBack.setOnClickListener { parentFragmentManager.popBackStack() }

        binding!!.button.setOnClickListener {
            val email = binding!!.myEmail.text.toString()
            val oldPassword = binding!!.oldPassword.text.toString()
            val newPassword = binding!!.newPassword.text.toString()
            val newPasswordRep = binding!!.newPasswordRep.text.toString()


            var allFieldsValid = true

            if (email.isEmpty()) {
                binding!!.myEmail.error = "Поле не может быть пустым"
                allFieldsValid = false
            }
            if (oldPassword.isEmpty()) {
                binding!!.oldPassword.error = "Поле не может быть пустым"
                allFieldsValid = false
            }
            if (newPassword.length < 8) {
                binding!!.newPassword.error = "Минимум 8 символов"
                allFieldsValid = false
            }
            if (oldPassword == newPassword) {
                binding!!.newPassword.error = "Пароли не могут совпадать"
                allFieldsValid = false
            }
            if (newPassword != newPasswordRep) {
                binding!!.newPassword.error = "Пароли не совпадают"
                allFieldsValid = false
            }
            if (allFieldsValid) {
                // Вызываем функцию
                changePassword(email, oldPassword, newPassword)
            }
        }


        return binding!!.root
    }

    private fun changePassword(email: String, oldPassword: String, newPassword: String) {
        val user = mAuth!!.currentUser
        val credential = EmailAuthProvider.getCredential(email, oldPassword)
        if (oldPassword === newPassword) {
            Toast.makeText(context, "Введите пароль, отличный от старого", Toast.LENGTH_SHORT)
                .show()
        } else {
            user!!.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.updatePassword(newPassword).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Пароль успешно изменен", Toast.LENGTH_SHORT)
                                .show()
                            parentFragmentManager.popBackStack()
                        } else {
                            Toast.makeText(context, "Некорректный пароль", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Некорректна почта и/или пароль", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}