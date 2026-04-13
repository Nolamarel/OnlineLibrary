package com.nolamarel.onlinelibrary.Adapters.myBooks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nolamarel.onlinelibrary.OnItemClickListener
import com.nolamarel.onlinelibrary.R

class UserBookAdapter(
    private val books: List<UserBookDTO>,
    private val listener: OnItemClickListener.ItemClickListener
) : RecyclerView.Adapter<UserBookAdapter.UserBookViewHolder>() {

    inner class UserBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookImage: ImageView = itemView.findViewById(R.id.my_book_iv)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position)
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
        val book = books[position]

        // Загрузка изображения обложки
        Glide.with(holder.itemView.context)
            .load(book.image)
            .into(holder.bookImage)

    }

    override fun getItemCount(): Int = books.size
}