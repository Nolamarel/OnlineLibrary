package com.nolamarel.onlinelibrary.Adapters.myBooks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nolamarel.onlinelibrary.Adapters.myBooks.MyBookAdapter.MyBookViewHolder
import com.nolamarel.onlinelibrary.OnItemClickListener.ItemClickListener
import com.nolamarel.onlinelibrary.R

abstract class MyBookAdapter : RecyclerView.Adapter<MyBookViewHolder> {
    private var books: List<MyBook> = ArrayList()
    private var listener: ItemClickListener? = null

    constructor(books: List<MyBook>, listener: ItemClickListener?) {
        this.books = books
        this.listener = listener
    }

    constructor(books: ArrayList<MyBook>) {
        this.books = books
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.library_item, parent, false)
        return MyBookViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyBookViewHolder, position: Int) {
        val myBook = books[position]


        Glide.with(holder.itemView.context).load(myBook.bookImage).into(holder.myBookIv)

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

    abstract fun onItemClick(position: Int)

    inner class MyBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var myBookPercent: TextView? = null
        var myBookIv: ImageView = itemView.findViewById(R.id.my_book_iv)

        fun bind(imagePath: String?) {
            // Загрузите изображение по указанному пути и установите его в ImageView
            Glide.with(itemView.context)
                .load(imagePath)
                .into(myBookIv)
        }
    }
}
