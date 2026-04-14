package com.nolamarel.onlinelibrary.Activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.nolamarel.onlinelibrary.Fragments.bottomnav.library.LibraryFragment
import com.nolamarel.onlinelibrary.Fragments.bottomnav.main.MainFragment
import com.nolamarel.onlinelibrary.Fragments.bottomnav.profile.ProfileFragment
import com.nolamarel.onlinelibrary.Fragments.bottomnav.search.SearchFragment
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = SessionManager(this)
        val token = sessionManager.getToken()

        if (token.isNullOrBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val selectedItemId = savedInstanceState?.getInt("selected_item") ?: R.id.main
        binding.bottonNav.selectedItemId = selectedItemId

        if (savedInstanceState == null) {
            when (selectedItemId) {
                R.id.main -> replaceFragment(MainFragment())
                R.id.profile -> replaceFragment(ProfileFragment())
                R.id.library -> replaceFragment(LibraryFragment())
                R.id.search -> replaceFragment(SearchFragment())
            }
        }

        binding.bottonNav.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.main -> replaceFragment(MainFragment())
                R.id.profile -> replaceFragment(ProfileFragment())
                R.id.library -> replaceFragment(LibraryFragment())
                R.id.search -> replaceFragment(SearchFragment())
                R.id.reader -> openLastBook()
            }
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selected_item", binding.bottonNav.selectedItemId)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun openLastBook() {
        val sessionManager = SessionManager(this)

        val lastBookId = sessionManager.getLastBookId()
        val lastBookTitle = sessionManager.getLastBookTitle()
        val lastBookPage = sessionManager.getLastBookPage()
        val lastBookPath = sessionManager.getLastBookPath()

        if (lastBookId == -1L || lastBookPath.isNullOrBlank()) {
            Toast.makeText(this, "Последняя открытая книга не найдена", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, PdfReaderActivity::class.java).apply {
            putExtra("book_id", lastBookId)
            putExtra("book_title", lastBookTitle)
            putExtra("current_page", lastBookPage)

            if (lastBookPath.startsWith("content://")) {
                putExtra("file_uri", lastBookPath)
            } else {
                putExtra("file_path", lastBookPath)
            }
        }

        startActivity(intent)
    }
}