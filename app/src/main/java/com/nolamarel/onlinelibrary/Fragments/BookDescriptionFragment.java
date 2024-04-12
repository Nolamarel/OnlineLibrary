package com.nolamarel.onlinelibrary.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nolamarel.onlinelibrary.R;
import com.nolamarel.onlinelibrary.databinding.FragmentBookDescriptionBinding;


public class BookDescriptionFragment extends Fragment {

    private FragmentBookDescriptionBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookDescriptionBinding.inflate(inflater, container, false);


        String bookId = getArguments().getString("bookId");


        loadBook(bookId);

        binding.bookDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBook(bookId);
                Toast.makeText(getContext(), "Книга успешно добавлена", Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    private void addBook(String bookId){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("books")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String currentBooksValue = (String) snapshot.getValue();
                            if (currentBooksValue != null && !currentBooksValue.isEmpty()){
                                currentBooksValue += "," + bookId;
                            } else {
                                currentBooksValue = bookId;
                            }
                            FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("books").setValue(currentBooksValue);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBook(String bookId){
        FirebaseDatabase.getInstance().getReference().child("Books").child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    binding.bookName.setText(snapshot.child("name").getValue().toString());
                    binding.bookAuthor.setText(snapshot.child("author").getValue().toString());
                    binding.bookDesc.setText(snapshot.child("description").getValue().toString());
//                    Glide.with(holder.itemView.getContext()).load(book.bookImage).into(holder.book_image);
                    Glide.with(getContext()).load(snapshot.child("image").getValue().toString()).into(binding.bookIv);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //binding.bookName.setText(FirebaseDatabase.getInstance().getReference().child("Books").child(bookId).child("name").getValue());
    }
}