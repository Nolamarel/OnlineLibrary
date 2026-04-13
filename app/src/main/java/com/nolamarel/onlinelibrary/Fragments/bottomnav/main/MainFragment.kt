package com.nolamarel.onlinelibrary.Fragments.bottomnav.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nolamarel.onlinelibrary.Adapters.sections.Section
import com.nolamarel.onlinelibrary.Adapters.sections.SectionAdapter
import com.nolamarel.onlinelibrary.Fragments.BooksFragment
import com.nolamarel.onlinelibrary.GenreDTO
import com.nolamarel.onlinelibrary.OnItemClickListener.ItemClickListener
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.RetrofitInstance
import com.nolamarel.onlinelibrary.databinding.FragmentMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainFragment : Fragment() {
    private var binding: FragmentMainBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)


        loadSections()


        return binding!!.root
    }


    private fun loadSections() {
        RetrofitInstance.serverApi.getGenres().enqueue(object : Callback<List<GenreDTO>> {
            override fun onResponse(call: Call<List<GenreDTO>>, response: Response<List<GenreDTO>>) {
                if (response.isSuccessful) {
                    val genres = response.body() ?: emptyList()
                    val sections = ArrayList<Section>()

                    for (genre in genres) {
                        sections.add(Section(genre.name, genre.image, genre.id))
                    }

                    binding!!.booksMainRv.layoutManager = GridLayoutManager(context, 2)
                    binding!!.booksMainRv.adapter = SectionAdapter(sections, object : ItemClickListener {
                        override fun onItemClick(position: Int) {
                            val selectedSection = sections[position].sectionId
                            val selectedSectionName = sections[position].sectionName
                            val fragment: Fragment = BooksFragment()
                            val args = Bundle().apply {
                                putString("sectionId", selectedSection)
                                putString("sectionName", selectedSectionName) // 👈 добавили
                            }
                            fragment.arguments = args
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    })
                } else {
                    Toast.makeText(context, "Ошибка загрузки жанров", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<GenreDTO>>, t: Throwable) {
                Toast.makeText(context, "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
            }
        })
    }
}