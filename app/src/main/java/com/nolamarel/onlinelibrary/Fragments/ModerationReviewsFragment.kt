package com.nolamarel.onlinelibrary.Fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.FragmentModerationReviewsBinding
import com.nolamarel.onlinelibrary.network.ReviewResponse
import com.nolamarel.onlinelibrary.network.UpdateReviewStatusRequest
import kotlinx.coroutines.launch

class ModerationReviewsFragment : Fragment() {

    private var _binding: FragmentModerationReviewsBinding? = null
    private val binding get() = _binding!!

    private val reviews = mutableListOf<ReviewResponse>()
    private lateinit var adapter: ModerationReviewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModerationReviewsBinding.inflate(inflater, container, false)

        binding.arrowBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        adapter = ModerationReviewsAdapter(reviews) { review ->
            showStatusDialog(review)
        }

        binding.reviewsRv.layoutManager = LinearLayoutManager(requireContext())
        binding.reviewsRv.adapter = adapter

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

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getAllReviewsForModeration(bearer)

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    reviews.clear()
                    reviews.addAll(response.body().orEmpty())
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "Не удалось загрузить отзывы", Toast.LENGTH_LONG).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(requireContext(), "Ошибка подключения к серверу", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showStatusDialog(review: ReviewResponse) {
        val statuses = arrayOf("PENDING", "APPROVED", "REJECTED")
        val currentIndex = statuses.indexOf(review.status.uppercase()).coerceAtLeast(0)

        AlertDialog.Builder(requireContext())
            .setTitle("Изменить статус")
            .setSingleChoiceItems(statuses, currentIndex) { dialog, which ->
                updateStatus(review.reviewId, statuses[which])
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateStatus(reviewId: Long, status: String) {
        val token = SessionManager(requireContext()).getToken() ?: return
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.updateReviewStatus(
                    token = bearer,
                    reviewId = reviewId.toString(),
                    body = UpdateReviewStatusRequest(status)
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Статус обновлён", Toast.LENGTH_SHORT).show()
                    loadReviews()
                } else {
                    Toast.makeText(requireContext(), "Не удалось обновить статус", Toast.LENGTH_LONG).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(requireContext(), "Ошибка подключения к серверу", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}