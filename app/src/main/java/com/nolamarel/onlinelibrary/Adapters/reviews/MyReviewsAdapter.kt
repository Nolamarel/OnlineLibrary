package com.nolamarel.onlinelibrary.Adapters.reviews

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nolamarel.onlinelibrary.databinding.ItemMyReviewBinding
import com.nolamarel.onlinelibrary.network.ReviewResponse

class MyReviewsAdapter(
    private val items: List<ReviewResponse>,
    private val onOpenBookClick: (ReviewResponse) -> Unit
) : RecyclerView.Adapter<MyReviewsAdapter.MyReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyReviewViewHolder {
        val binding = ItemMyReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyReviewViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class MyReviewViewHolder(
        private val binding: ItemMyReviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReviewResponse) {
            binding.reviewRating.text = "Оценка: ${item.rating}/5"
            binding.reviewComment.text = item.comment ?: "Без текста"
            binding.reviewDate.text = item.createdAt
            binding.openBookBtn.setOnClickListener {
                onOpenBookClick(item)
            }
        }
    }
}