package com.nolamarel.onlinelibrary.Adapters.books

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.nolamarel.onlinelibrary.OnItemClickListener.ItemClickListener
import com.nolamarel.onlinelibrary.R

class BookAdapter : RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    var books = ArrayList<Book>()
    private var listener: ItemClickListener? = null

    constructor(books: ArrayList<Book>) {
        this.books = books
    }

    constructor(books: ArrayList<Book>, listener: ItemClickListener?) {
        this.books = books
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.books_item, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]

        holder.book_name.text = book.bookName
        holder.book_author.text = book.bookAuthor.ifBlank { "Автор неизвестен" }

        android.util.Log.d("BookAdapter", "bookImage=${book.bookImage}")

        Glide.with(holder.itemView.context)
            .load(book.bookImage)
            .placeholder(R.drawable.books)
            .error(R.drawable.books)
            .into(holder.book_image)

        holder.itemView.setOnClickListener {
            val clickedPosition = holder.adapterPosition
            if (clickedPosition != RecyclerView.NO_POSITION) {
                listener?.onItemClick(clickedPosition)
            }
        }
    }

    override fun getItemCount(): Int = books.size

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val book_name: TextView = itemView.findViewById(R.id.book_name_tv)
        val book_author: TextView = itemView.findViewById(R.id.book_author_tv)
        val book_image: ImageView = itemView.findViewById(R.id.book_iv)
    }
}