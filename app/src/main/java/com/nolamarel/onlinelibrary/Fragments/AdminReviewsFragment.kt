package com.nolamarel.onlinelibrary.Fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.databinding.FragmentAdminReviewsBinding
import com.nolamarel.onlinelibrary.network.AdminUpdateReviewRequest
import com.nolamarel.onlinelibrary.network.ReviewResponse
import kotlinx.coroutines.launch

class AdminReviewsFragment : Fragment() {

    private var _binding: FragmentAdminReviewsBinding? = null
    private val binding get() = _binding!!

    private val reviews = mutableListOf<ReviewResponse>()
    private lateinit var adapter: AdminReviewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminReviewsBinding.inflate(inflater, container, false)

        binding.arrowBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        adapter = AdminReviewsAdapter(
            items = reviews,
            onEditClick = { review -> showEditDialog(review) },
            onDeleteClick = { review -> showDeleteDialog(review) }
        )

        binding.reviewsRv.layoutManager = LinearLayoutManager(requireContext())
        binding.reviewsRv.adapter = adapter

        loadReviews()

        return binding.root
    }

    private fun getBearerToken(): String? {
        val token = SessionManager(requireContext()).getToken()
        return token?.let { if (it.startsWith("Bearer ")) it else "Bearer $it" }
    }

    private fun loadReviews() {
        binding.progressBar.visibility = View.VISIBLE

        val bearer = getBearerToken()
        if (bearer.isNullOrBlank()) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.getAdminReviews(bearer)

                if (!isAdded || _binding == null) return@launch

                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    reviews.clear()
                    reviews.addAll(response.body().orEmpty())
                    adapter.notifyDataSetChanged()
                    binding.emptyText.visibility =
                        if (reviews.isEmpty()) View.VISIBLE else View.GONE
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

    private fun showEditDialog(review: ReviewResponse) {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 24, 40, 8)
        }

        val ratingEt = EditText(requireContext()).apply {
            hint = "Оценка от 1 до 5"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(review.rating.toString())
        }

        val commentEt = EditText(requireContext()).apply {
            hint = "Комментарий"
            minLines = 3
            maxLines = 5
            setText(review.comment.orEmpty())
        }

        val statusGroup = RadioGroup(requireContext()).apply {
            orientation = RadioGroup.VERTICAL
        }

        val pendingRb = RadioButton(requireContext()).apply {
            text = "PENDING"
            id = View.generateViewId()
        }

        val approvedRb = RadioButton(requireContext()).apply {
            text = "APPROVED"
            id = View.generateViewId()
        }

        val rejectedRb = RadioButton(requireContext()).apply {
            text = "REJECTED"
            id = View.generateViewId()
        }

        statusGroup.addView(pendingRb)
        statusGroup.addView(approvedRb)
        statusGroup.addView(rejectedRb)

        when (review.status.uppercase()) {
            "APPROVED" -> statusGroup.check(approvedRb.id)
            "REJECTED" -> statusGroup.check(rejectedRb.id)
            else -> statusGroup.check(pendingRb.id)
        }

        container.addView(ratingEt)
        container.addView(commentEt)
        container.addView(statusGroup)

        AlertDialog.Builder(requireContext())
            .setTitle("Редактировать отзыв")
            .setView(container)
            .setPositiveButton("Сохранить") { _, _ ->
                val rating = ratingEt.text.toString().trim().toIntOrNull()
                if (rating == null || rating !in 1..5) {
                    Toast.makeText(
                        requireContext(),
                        "Оценка должна быть от 1 до 5",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setPositiveButton
                }

                val selectedStatus = when (statusGroup.checkedRadioButtonId) {
                    approvedRb.id -> "APPROVED"
                    rejectedRb.id -> "REJECTED"
                    else -> "PENDING"
                }

                val request = AdminUpdateReviewRequest(
                    rating = rating,
                    comment = commentEt.text.toString().trim().ifBlank { null },
                    status = selectedStatus
                )

                updateReview(review.reviewId.toString(), request)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateReview(reviewId: String, request: AdminUpdateReviewRequest) {
        val bearer = getBearerToken()
        if (bearer.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.updateAdminReview(
                    token = bearer,
                    reviewId = reviewId,
                    body = request
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Отзыв обновлён",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadReviews()
                } else {
                    val errorText = response.errorBody()?.string().orEmpty()
                    Toast.makeText(
                        requireContext(),
                        if (errorText.isNotBlank()) errorText else "Не удалось обновить отзыв",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
                Toast.makeText(
                    requireContext(),
                    "Ошибка подключения к серверу",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showDeleteDialog(review: ReviewResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить отзыв")
            .setMessage("Удалить отзыв пользователя ${review.userName}?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteReview(review.reviewId.toString())
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteReview(reviewId: String) {
        val bearer = getBearerToken()
        if (bearer.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Сессия не найдена", Toast.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.serverApi.deleteAdminReview(
                    token = bearer,
                    reviewId = reviewId
                )

                if (!isAdded || _binding == null) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Отзыв удалён",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadReviews()
                } else {
                    val errorText = response.errorBody()?.string().orEmpty()
                    Toast.makeText(
                        requireContext(),
                        if (errorText.isNotBlank()) errorText else "Не удалось удалить отзыв",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (_: Exception) {
                if (!isAdded || _binding == null) return@launch
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