package com.nolamarel.onlinelibrary.Adapters.myBooks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nolamarel.onlinelibrary.R

class UserBookAdapter(
    private val items: List<UserBookDto>,
    private val onBookClick: (UserBookDto) -> Unit
) : RecyclerView.Adapter<UserBookAdapter.UserBookViewHolder>() {

    inner class UserBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookImage: ImageView = itemView.findViewById(R.id.my_book_iv)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onBookClick(items[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserBookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.library_item, parent, false)
        return UserBookViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserBookViewHolder, position: Int) {
        val book = items[position]

        Glide.with(holder.itemView.context)
            .load(book.coverUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .into(holder.bookImage)
    }

    override fun getItemCount(): Int = items.size
}