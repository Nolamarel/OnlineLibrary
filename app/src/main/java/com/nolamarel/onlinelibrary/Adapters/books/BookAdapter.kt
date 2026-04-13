package com.nolamarel.onlinelibrary.Adapters.books

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nolamarel.onlinelibrary.Adapters.books.BookAdapter.BookViewHolder
import com.nolamarel.onlinelibrary.OnItemClickListener.ItemClickListener
import com.nolamarel.onlinelibrary.R

class BookAdapter : RecyclerView.Adapter<BookViewHolder> {
    private var books = ArrayList<Book>()
    private var listener: ItemClickListener? = null

    constructor(books: ArrayList<Book>) {
        this.books = books
    }

    constructor(books: ArrayList<Book>, listener: ItemClickListener?) {
        this.listener = listener
        this.books = books
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.books_item, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]

        holder.book_name.text = book.bookName
        holder.book_author.text = book.bookAuthor

        Glide.with(holder.itemView.context).load(book.bookImage).into(holder.book_image)

        holder.itemView.setOnClickListener {
            val clickedPosition = holder.adapterPosition
            if (clickedPosition != RecyclerView.NO_POSITION) {
                listener!!.onItemClick(clickedPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return books.size
    }

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var book_name: TextView = itemView.findViewById(R.id.book_name_tv)
        var book_author: TextView = itemView.findViewById(R.id.book_author_tv)
        var book_image: ImageView = itemView.findViewById(R.id.book_iv)
    }
}
