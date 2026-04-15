package com.nolamarel.onlinelibrary.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.databinding.FragmentAdminPanelBinding

class AdminPanelFragment : Fragment() {

    private var _binding: FragmentAdminPanelBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPanelBinding.inflate(inflater, container, false)

        binding.arrowBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.cardGenres.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AdminGenresFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardBooks.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AdminBooksFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardUsers.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AdminUsersFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardReviews.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AdminReviewsFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardUserBooks.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AdminUserBooksFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardSearchHistory.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AdminSearchHistoryFragment())
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}