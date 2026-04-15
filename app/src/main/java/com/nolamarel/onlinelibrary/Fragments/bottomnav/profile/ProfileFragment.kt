package com.nolamarel.onlinelibrary.Fragments.bottomnav.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nolamarel.onlinelibrary.Activities.LoginActivity
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.App
import com.nolamarel.onlinelibrary.Fragments.AboutAppFragment
import com.nolamarel.onlinelibrary.Fragments.AdminPanelFragment
import com.nolamarel.onlinelibrary.Fragments.ChangePasswordFragment
import com.nolamarel.onlinelibrary.Fragments.EditProfileFragment
import com.nolamarel.onlinelibrary.Fragments.ModerationReviewsFragment
import com.nolamarel.onlinelibrary.Fragments.MyReviewsFragment
import com.nolamarel.onlinelibrary.Fragments.SupportServiceFragment
import com.nolamarel.onlinelibrary.LocalizationManager
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch
import java.io.IOException

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var filePath: Uri? = null
    private lateinit var localizationManager: LocalizationManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        localizationManager = LocalizationManager(requireContext())

        setupClicks()
        setupThemeSwitcher()
        loadUserInfo()

        return binding.root
    }

    private fun setupThemeSwitcher() {
        val themeSwitcher = binding.root.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.themeSwitcher)
        themeSwitcher.isChecked = (requireActivity().application as App).darkTheme
        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            (requireActivity().application as App).switchTheme(isChecked)
        }
    }

    private fun setupClicks() {
        binding.cardViewIn21.setOnClickListener { replaceFragment(EditProfileFragment()) }
        binding.cardViewIn24.setOnClickListener { selectImage() }
        binding.cardViewIn2.setOnClickListener { replaceFragment(MyReviewsFragment()) }
        binding.cardViewFavorites.setOnClickListener { replaceFragment(FavoriteBooksFragment()) }
        binding.cardViewIn22.setOnClickListener { replaceFragment(ChangePasswordFragment()) }
        binding.cardViewIn3.setOnClickListener { replaceFragment(AboutAppFragment()) }
        binding.languageTv.setOnClickListener { toggleLanguage() }
        binding.cardViewModeration.setOnClickListener {
            replaceFragment(ModerationReviewsFragment())
        }
        binding.cardViewAdmin.setOnClickListener {
            replaceFragment(AdminPanelFragment())
        }

        binding.cardViewIn23.setOnClickListener {
            SessionManager(requireContext()).clear()

            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
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

        parentFragmentManager.beginTransaction()
            .detach(this)
            .attach(this)
            .commit()
    }

    private fun loadUserInfo() {
        val sessionManager = SessionManager(requireContext())
        val token = sessionManager.getToken()

        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_SHORT).show()
            return
        }

        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getMe(bearer)

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    val user = response.body()

                    if (user == null) {
                        Toast.makeText(
                            requireContext(),
                            "Не удалось загрузить профиль",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    binding.profileUsername.text = user.name
                    binding.profileStatus.text = buildStatusText(user.role, user.isActive)
                    binding.cardViewModeration.visibility =
                        if (user.role.uppercase() == "MODERATOR" || user.role.uppercase() == "ADMIN") {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }

                    binding.cardViewAdmin.visibility =
                        if (user.role.uppercase() == "ADMIN") View.VISIBLE else View.GONE

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка загрузки профиля",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(
                    requireContext(),
                    "Ошибка сети при загрузке профиля",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun buildStatusText(role: String, isActive: Boolean): String {
        if (!isActive) return "Пользователь неактивен"

        return when (role.uppercase()) {
            "ADMIN" -> "Администратор"
            "MODERATOR" -> "Модератор"
            else -> "Пользователь"
        }
    }

    private fun selectImage() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        pickImageActivityResultLauncher.launch(intent)
    }

    private val pickImageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
                        Toast.makeText(
                            requireContext(),
                            "Не удалось загрузить изображение",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}