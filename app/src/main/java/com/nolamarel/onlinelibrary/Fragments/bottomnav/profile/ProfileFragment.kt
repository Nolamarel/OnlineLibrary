package com.nolamarel.onlinelibrary.Fragments.bottomnav.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.nolamarel.onlinelibrary.Activities.LoginActivity
import com.nolamarel.onlinelibrary.DatabaseHelper1
import com.nolamarel.onlinelibrary.Fragments.AboutAppFragment
import com.nolamarel.onlinelibrary.Fragments.ChangePasswordFragment
import com.nolamarel.onlinelibrary.Fragments.SupportServiceFragment
import com.nolamarel.onlinelibrary.LocalizationManager
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.databinding.FragmentProfileBinding
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import com.nolamarel.onlinelibrary.App
import com.nolamarel.onlinelibrary.RetrofitInstance
import com.nolamarel.onlinelibrary.User
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private var username: String? = null
    private var userImage: String? = null
    private var status: String? = null
    private var filePath: Uri? = null
    private lateinit var localizationManager: LocalizationManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val themeSwitcher = view.findViewById<SwitchMaterial>(R.id.themeSwitcher)
        themeSwitcher.isChecked = (requireActivity().application as App).darkTheme
        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            (requireActivity().application as App).switchTheme(isChecked)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        localizationManager = LocalizationManager(requireContext())

        loadUserInfo()

        binding.cardViewIn21.setOnClickListener { selectImage() }

        binding.cardViewIn2.setOnClickListener {
            replaceFragment(SupportServiceFragment())
        }

        binding.cardViewIn23.setOnClickListener {
            val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        binding.cardViewIn22.setOnClickListener {
            replaceFragment(ChangePasswordFragment())
        }

        binding.cardViewIn3.setOnClickListener {
            replaceFragment(AboutAppFragment())
        }

        binding.languageTv.setOnClickListener {
            toggleLanguage()
        }

        return binding.root
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun toggleLanguage() {
        val newLanguage = if (localizationManager.currentLanguage == "ru") "en" else "ru"
        localizationManager.setLanguage(newLanguage)
        // Обновляем UI после смены языка
        updateUIAfterLanguageChange()
    }

    private fun updateUIAfterLanguageChange() {
        // Перезагружаем фрагмент для применения языковых изменений
        parentFragmentManager.beginTransaction()
            .detach(this)
            .attach(this)
            .commit()
    }

    private fun loadUserInfo() {
        val sharedPrefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        val userId = sharedPrefs.getString("userId", null)

        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.serverApi.getUserById(userId)
                if (response.isSuccessful) {
                    val user = response.body() ?: return@launch
                    binding.profileUsername.text = user.username
                    Log.d("ProfileFragment", "User loaded: ${user.username}")
//                    // Если profileImage есть в DTO
//                    val profileImage = user.profileImage // <-- добавь в UserDTO, если ещё не сделал
//                    profileImage?.let {
//                        Glide.with(requireContext())
//                            .load(it)
//                            .into(binding.profileImage)
//                    }
                } else {
                    Toast.makeText(context, "Failed to load user", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
            }
        }

        // Рассчитываем статус из локальной SQLite базы
        val dbHelper = DatabaseHelper1(context, userId)
        val rowCount = dbHelper.getRowCount(dbHelper.writableDatabase)

        status = when {
            rowCount == 0 -> getString(R.string.beginner)
            rowCount in 1..3 -> getString(R.string.reader_)
            rowCount in 4..7 -> getString(R.string.bookworm)
            rowCount in 8..11 -> getString(R.string.librarian)
            else -> getString(R.string.reading_wizard)
        }

        binding.profileStatus.text = status
    }


    private fun selectImage() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        pickImageActivityResultLauncher.launch(intent)
    }

    private val pickImageActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                filePath = uri
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        requireContext().contentResolver,
                        uri
                    )
                    binding.profileImage.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}