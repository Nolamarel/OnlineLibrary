package com.nolamarel.onlinelibrary.Fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.databinding.ItemAdminGenreBinding
import com.nolamarel.onlinelibrary.network.GenreResponse

class AdminGenresAdapter(
    private val items: List<GenreResponse>,
    private val onEditClick: (GenreResponse) -> Unit,
    private val onDeleteClick: (GenreResponse) -> Unit
) : RecyclerView.Adapter<AdminGenresAdapter.AdminGenreViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminGenreViewHolder {
        val binding = ItemAdminGenreBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminGenreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminGenreViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class AdminGenreViewHolder(
        private val binding: ItemAdminGenreBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GenreResponse) {
            binding.genreNameTv.text = item.name

            Glide.with(binding.root.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.books)
                .error(R.drawable.books)
                .into(binding.genreImageIv)

            binding.editGenreBtn.setOnClickListener {
                onEditClick(item)
            }

            binding.deleteGenreBtn.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }
}