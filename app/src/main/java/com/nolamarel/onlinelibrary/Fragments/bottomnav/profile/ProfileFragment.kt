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
import androidx.fragment.app.Fragment
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
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.App
import com.nolamarel.onlinelibrary.RetrofitInstance
import kotlinx.coroutines.launch
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

        binding.cardViewFavorites.setOnClickListener {
            replaceFragment(FavoriteBooksFragment())
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
        val sessionManager = com.nolamarel.onlinelibrary.auth.SessionManager(requireContext())
        val token = sessionManager.getToken()
        val userId = sessionManager.getUserId()

        if (token.isNullOrBlank() || userId == -1L) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getUserById(bearer, userId.toString())
                if (response.isSuccessful) {
                    val user = response.body() ?: return@launch
                    binding.profileUsername.text = user.username
                    Log.d("ProfileFragment", "User loaded: ${user.username}")
                } else {
                    Toast.makeText(context, "Failed to load user", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
            }
        }

        val dbHelper = DatabaseHelper1(context, userId.toString())
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