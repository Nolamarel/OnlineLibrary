package com.nolamarel.onlinelibrary.Adapters.books;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ValueEventListener;
import com.nolamarel.onlinelibrary.OnItemClickListener;
import com.nolamarel.onlinelibrary.R;

import java.util.ArrayList;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private ArrayList<Book> books = new ArrayList<>();
    private OnItemClickListener.ItemClickListener listener;

    public BookAdapter(ArrayList<Book> books){
        this.books = books;
    }

    public BookAdapter(ArrayList<Book> books, OnItemClickListener.ItemClickListener listener) {
        this.listener = (OnItemClickListener.ItemClickListener) listener;
        this.books = books;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.books_item, parent, false);
        return new BookAdapter.BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);

        holder.book_name.setText(book.bookName);
        holder.book_author.setText(book.bookAuthor);

        Glide.with(holder.itemView.getContext()).load(book.bookImage).into(holder.book_image);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    listener.onItemClick(clickedPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public class BookViewHolder extends RecyclerView.ViewHolder{

        TextView book_name, book_author;
        ImageView book_image;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);

            book_name = itemView.findViewById(R.id.book_name_tv);
            book_author = itemView.findViewById(R.id.book_author_tv);
            book_image = itemView.findViewById(R.id.book_iv);

        }
    }
}
