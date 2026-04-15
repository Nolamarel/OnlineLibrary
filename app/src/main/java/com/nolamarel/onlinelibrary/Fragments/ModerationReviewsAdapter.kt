package com.nolamarel.onlinelibrary.Fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nolamarel.onlinelibrary.databinding.ItemModerationReviewBinding
import com.nolamarel.onlinelibrary.network.ReviewResponse

class ModerationReviewsAdapter(
    private val items: List<ReviewResponse>,
    private val onStatusClick: (ReviewResponse) -> Unit
) : RecyclerView.Adapter<ModerationReviewsAdapter.ModerationReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModerationReviewViewHolder {
        val binding = ItemModerationReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ModerationReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModerationReviewViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ModerationReviewViewHolder(
        private val binding: ItemModerationReviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReviewResponse) {
            binding.userNameTv.text = item.userName
            binding.ratingTv.text = "Оценка: ${item.rating}/5"
            binding.commentTv.text = item.comment ?: "Без текста"
            binding.statusTv.text = "Статус: ${item.status}"

            binding.changeStatusBtn.setOnClickListener {
                onStatusClick(item)
            }
        }
    }
}