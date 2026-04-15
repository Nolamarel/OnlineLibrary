package com.nolamarel.onlinelibrary.Fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nolamarel.onlinelibrary.databinding.ItemAdminReviewBinding
import com.nolamarel.onlinelibrary.network.ReviewResponse

class AdminReviewsAdapter(
    private val items: List<ReviewResponse>,
    private val onEditClick: (ReviewResponse) -> Unit,
    private val onDeleteClick: (ReviewResponse) -> Unit
) : RecyclerView.Adapter<AdminReviewsAdapter.AdminReviewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminReviewsViewHolder {
        val binding = ItemAdminReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminReviewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminReviewsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class AdminReviewsViewHolder(
        private val binding: ItemAdminReviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReviewResponse) {
            binding.userNameTv.text = item.userName
            binding.bookIdTv.text = "Книга ID: ${item.bookId}"
            binding.ratingTv.text = "Оценка: ${item.rating}/5"
            binding.commentTv.text = item.comment ?: "Без текста"
            binding.statusTv.text = "Статус: ${item.status}"

            binding.editBtn.setOnClickListener {
                onEditClick(item)
            }

            binding.deleteBtn.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }
}