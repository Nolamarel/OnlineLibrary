package com.nolamarel.onlinelibrary.Adapters.myBooks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nolamarel.onlinelibrary.OnItemClickListener;
import com.nolamarel.onlinelibrary.R;

import java.util.ArrayList;

public class MyBookAdapter extends RecyclerView.Adapter<MyBookAdapter.MyBookViewHolder> {
    private ArrayList<MyBook> books = new ArrayList<>();
    private OnItemClickListener.ItemClickListener listener;

    public MyBookAdapter(ArrayList<MyBook> books, OnItemClickListener.ItemClickListener listener) {
        this.books = books;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.library_item, parent, false);
        return new MyBookAdapter.MyBookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyBookViewHolder holder, int position) {
        MyBook myBook = books.get(position);

        holder.myBookPercent.setText(myBook.bookPercent);

        Glide.with(holder.itemView.getContext()).load(myBook.bookImage).into(holder.myBookIv);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = holder.getAdapterPosition();
                if(clickedPosition != RecyclerView.NO_POSITION){
                    listener.onItemClick(clickedPosition);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return books.size();
    }

    public class MyBookViewHolder extends RecyclerView.ViewHolder{

        TextView myBookPercent;
        ImageView myBookIv;

        public MyBookViewHolder(@NonNull View itemView) {
            super(itemView);

            myBookPercent = itemView.findViewById(R.id.my_book_percent);
            myBookIv = itemView.findViewById(R.id.my_book_iv);
        }
    }
}
