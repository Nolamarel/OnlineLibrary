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

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        loadSections()

        return binding.root
    }

    private fun loadSections() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getGenres()

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    val genres = response.body().orEmpty()

                    val sections = ArrayList(
                        genres.map { genre ->
                            Section(
                                sectionName = genre.name,
                                sectionIv = genre.imageUrl,
                                sectionId = genre.genreId.toString()
                            )
                        }
                    )

                    binding.booksMainRv.layoutManager = GridLayoutManager(context, 2)
                    binding.booksMainRv.adapter = SectionAdapter(
                        sections,
                        object : ItemClickListener {
                            override fun onItemClick(position: Int) {
                                val selectedSection = sections[position]

                                val fragment = BooksFragment().apply {
                                    arguments = Bundle().apply {
                                        putString("sectionId", selectedSection.sectionId)
                                        putString("sectionName", selectedSection.sectionName)
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
                    Toast.makeText(
                        requireContext(),
                        "Ошибка загрузки жанров",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch

                Toast.makeText(
                    requireContext(),
                    "Ошибка подключения к серверу",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}