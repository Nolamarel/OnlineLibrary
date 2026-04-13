package com.nolamarel.onlinelibrary.Fragments.bottomnav.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.nolamarel.onlinelibrary.Adapters.sections.Section
import com.nolamarel.onlinelibrary.Adapters.sections.SectionAdapter
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.Fragments.BooksFragment
import com.nolamarel.onlinelibrary.OnItemClickListener.ItemClickListener
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.databinding.FragmentMainBinding
import kotlinx.coroutines.launch

class MainFragment : Fragment() {
    private var binding: FragmentMainBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)

        loadSections()

        return binding!!.root
    }

    private fun loadSections() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getGenres()

                if (response.isSuccessful) {
                    val genres = response.body().orEmpty()
                    val sections = ArrayList<Section>()

                    for (genre in genres) {
                        sections.add(
                            Section(
                                genre.name,
                                genre.image ?: "",
                                genre.genreId
                            )
                        )
                    }

                    binding?.booksMainRv?.layoutManager = GridLayoutManager(context, 2)
                    binding?.booksMainRv?.adapter = SectionAdapter(
                        sections,
                        object : ItemClickListener {
                            override fun onItemClick(position: Int) {
                                val selectedSection = sections[position].sectionId
                                val selectedSectionName = sections[position].sectionName

                                val fragment = BooksFragment().apply {
                                    arguments = Bundle().apply {
                                        putString("sectionId", selectedSection)
                                        putString("sectionName", selectedSectionName)
                                    }
                                }

                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit()
                            }
                        }
                    )
                } else {
                    Toast.makeText(context, "Ошибка загрузки жанров", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}