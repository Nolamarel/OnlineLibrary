package com.nolamarel.onlinelibrary.Fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nolamarel.onlinelibrary.BookResponse
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.databinding.ItemAdminBookBinding

class AdminBooksAdapter(
    private val items: List<BookResponse>,
    private val onEditClick: (BookResponse) -> Unit,
    private val onDeleteClick: (BookResponse) -> Unit
) : RecyclerView.Adapter<AdminBooksAdapter.AdminBookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminBookViewHolder {
        val binding = ItemAdminBookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminBookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminBookViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class AdminBookViewHolder(
        private val binding: ItemAdminBookBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BookResponse) {
            binding.bookTitleTv.text = item.title
            binding.bookAuthorTv.text = item.author

            Glide.with(binding.root.context)
                .load(item.coverUrl)
                .placeholder(R.drawable.books)
                .error(R.drawable.books)
                .into(binding.bookImageIv)

            binding.editBookBtn.setOnClickListener {
                onEditClick(item)
            }

            binding.deleteBookBtn.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }
}