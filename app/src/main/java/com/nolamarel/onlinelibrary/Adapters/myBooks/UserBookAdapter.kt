package com.nolamarel.onlinelibrary.Adapters.myBooks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.databinding.ItemUserBookBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class UserBookAdapter(
    private val items: List<UserBookDto>,
    private val onBookClick: (UserBookDto) -> Unit
) : RecyclerView.Adapter<UserBookAdapter.UserBookViewHolder>() {

    inner class UserBookViewHolder(
        private val binding: ItemUserBookBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: UserBookDto) {
            binding.tvBookTitle.text = book.title
//            binding.tvBookAuthor.text = book.author
            binding.tvBookProgress.text = formatProgress(book.progress)

            if (!book.coverUrl.isNullOrBlank()) {
                Glide.with(binding.ivBookCover.context)
                    .load(book.coverUrl)
                    .placeholder(R.drawable.books) // пока грузится
                    .error(R.drawable.books)       // если ошибка
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivBookCover)
            } else {
                binding.ivBookCover.setImageResource(R.drawable.books)
            }

            binding.root.setOnClickListener {
                onBookClick(book)
            }

            binding.ivBookCover.setOnClickListener {
                onBookClick(book)
            }
        }

        private fun formatProgress(progress: String): String {
            return try {
                val value = progress.toDouble()
                "${value.toInt()}%"
            } catch (_: Exception) {
                "$progress%"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserBookViewHolder {
        val binding = ItemUserBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserBookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserBookViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}