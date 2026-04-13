package com.nolamarel.onlinelibrary.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.nolamarel.onlinelibrary.databinding.FragmentSupportServiceBinding

class SupportServiceFragment : Fragment() {
    private var binding: FragmentSupportServiceBinding? = null
    private var problemText: String? = null
    private var emailText: String? = null
    private var messageText: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSupportServiceBinding.inflate(inflater, container, false)

        val userId = FirebaseAuth.getInstance().currentUser!!.uid.toString()

        binding!!.arrowBack.setOnClickListener { parentFragmentManager.popBackStack() }

        binding!!.supSerMessageEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val characterCount = s.length
                binding!!.supSerSymbolNow.text = "$characterCount/20000"
            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        val problem = binding!!.supSerThemeEt
        val email = binding!!.supSerEmailEt
        val message = binding!!.supSerMessageEt

        binding!!.button.setOnClickListener {
            var allFieldsValid = true
            problemText = binding!!.supSerThemeEt.text.toString()
            emailText = binding!!.supSerEmailEt.text.toString()
            messageText = binding!!.supSerMessageEt.text.toString()

            if (problemText!!.isEmpty()) {
                problem.error = "Введите проблему"
                allFieldsValid = false
            }
            if (emailText!!.isEmpty()) {
                email.error = "Введите адрес электронной почты"
                allFieldsValid = false
            }
            if (messageText!!.length < 20) {
                message.error = "Слишком короткое описание"
                allFieldsValid = false
            }
            if (allFieldsValid) {
                sendMessage(userId, problemText!!, emailText!!, messageText!!)
                Toast.makeText(context, "Сообщение успешно отправлено", Toast.LENGTH_SHORT).show()
            }
        }




        return binding!!.root
    }

    fun sendMessage(userId: String?, problemText: String, emailText: String, messageText: String) {
        if (userId == null) return
        val messageInfo = HashMap<String, String>()
        messageInfo["userId"] = userId
        messageInfo["problem"] = problemText
        messageInfo["email"] = emailText
        messageInfo["message"] = messageText
        FirebaseDatabase.getInstance().reference.child("Messages").push().setValue(messageInfo)
    }
}