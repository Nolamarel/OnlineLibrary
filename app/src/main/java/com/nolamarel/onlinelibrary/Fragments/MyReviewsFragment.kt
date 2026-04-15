package com.nolamarel.onlinelibrary.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nolamarel.onlinelibrary.Adapters.reviews.MyReviewsAdapter
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.FragmentMyReviewsBinding
import kotlinx.coroutines.launch

class MyReviewsFragment : Fragment() {

    private var _binding: FragmentMyReviewsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyReviewsBinding.inflate(inflater, container, false)

        binding.arrowBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.reviewsRv.layoutManager = LinearLayoutManager(requireContext())

        loadReviews()

        return binding.root
    }

    private fun loadReviews() {
        val token = SessionManager(requireContext()).getToken()

        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        binding.progressBar.visibility = View.VISIBLE
        binding.emptyReviews.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getMyReviews(bearer)

                if (!isAdded || _binding == null) return@launch

                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val reviews = response.body().orEmpty()

                    if (reviews.isEmpty()) {
                        binding.emptyReviews.visibility = View.VISIBLE
                    } else {
                        binding.reviewsRv.adapter = MyReviewsAdapter(reviews) { review ->
                            val fragment = BookDescriptionFragment().apply {
                                arguments = Bundle().apply {
                                    putString("bookId", review.bookId.toString())
                                    putString("source", "local")
                                }
                            }

                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось загрузить отзывы",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Ошибка подключения к серверу",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}