package com.nolamarel.onlinelibrary.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nolamarel.onlinelibrary.App
import com.nolamarel.onlinelibrary.Fragments.bottomnav.library.LibraryFragment
import com.nolamarel.onlinelibrary.Fragments.bottomnav.main.MainFragment
import com.nolamarel.onlinelibrary.Fragments.bottomnav.profile.ProfileFragment
import com.nolamarel.onlinelibrary.Fragments.bottomnav.search.SearchFragment
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.databinding.ActivityMainBinding

//class MainActivity : AppCompatActivity() {
//    private var binding: ActivityMainBinding? = null
//    private var mAuth: FirebaseAuth? = null
//    private var user: FirebaseUser? = null
//    private var context: Context? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding!!.root)
//
//        mAuth = FirebaseAuth.getInstance()
//        context = this
//
//        mAuth!!.addAuthStateListener { firebaseAuth ->
//            user = firebaseAuth.currentUser
//            if (user == null) {
//                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
//            }
//        }
//
//        // Восстанавливаем выбранный пункт нижнего меню, если есть
//        val selectedItemId = savedInstanceState?.getInt("selected_item") ?: R.id.main
//        binding!!.bottonNav.selectedItemId = selectedItemId
//
//// Только если activity создаётся впервые — загружаем фрагмент
//        if (savedInstanceState == null) {
//            when (selectedItemId) {
//                R.id.main -> replaceFragment(MainFragment())
//                R.id.profile -> replaceFragment(ProfileFragment())
//                R.id.library -> replaceFragment(LibraryFragment())
//                R.id.search -> replaceFragment(SearchFragment())
//            }
//        }
//
//        // Обработка нажатий в нижнем меню
//        binding!!.bottonNav.setOnItemSelectedListener { item: MenuItem ->
//            when (item.itemId) {
//                R.id.main -> replaceFragment(MainFragment())
//                R.id.profile -> replaceFragment(ProfileFragment())
//                R.id.reader -> {
//                    val intent = Intent(this@MainActivity, ReadingActivity::class.java)
//                    startActivity(intent)
//                }
//                R.id.library -> replaceFragment(LibraryFragment())
//                R.id.search -> replaceFragment(SearchFragment())
//            }
//            true
//        }
//    }
//
//    // Сохраняем выбранный пункт нижнего меню при повороте экрана
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putInt("selected_item", binding!!.bottonNav.selectedItemId)
//    }
//
//    private fun replaceFragment(fragment: Fragment) {
//        val fragmentManager = supportFragmentManager
//        val fragmentTransaction = fragmentManager.beginTransaction()
//        fragmentTransaction.replace(R.id.fragment_container, fragment)
//        fragmentTransaction.commit()
//    }
//}

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔑 Проверка: если токена нет — открываем LoginActivity
        val sharedPref = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = sharedPref.getString("token", null)

        if (token == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // закрываем MainActivity, чтобы нельзя было вернуться назад
            return
        }

        // Восстановление выбранного пункта нижнего меню
        val selectedItemId = savedInstanceState?.getInt("selected_item") ?: R.id.main
        binding.bottonNav.selectedItemId = selectedItemId

        // Загрузка фрагмента при первом запуске
        if (savedInstanceState == null) {
            when (selectedItemId) {
                R.id.main -> replaceFragment(MainFragment())
                R.id.profile -> replaceFragment(ProfileFragment())
                R.id.library -> replaceFragment(LibraryFragment())
                R.id.search -> replaceFragment(SearchFragment())
            }
        }

        // Обработка нажатий нижнего меню
        binding.bottonNav.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.main -> replaceFragment(MainFragment())
                R.id.profile -> replaceFragment(ProfileFragment())
                R.id.reader -> {
                    val intent = Intent(this, ReadingActivity::class.java)
                    startActivity(intent)
                }
                R.id.library -> replaceFragment(LibraryFragment())
                R.id.search -> replaceFragment(SearchFragment())
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
}
