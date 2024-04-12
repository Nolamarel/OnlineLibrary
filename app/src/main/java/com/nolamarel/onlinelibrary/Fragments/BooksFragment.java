package com.nolamarel.onlinelibrary.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nolamarel.onlinelibrary.OnItemClickListener;
import com.nolamarel.onlinelibrary.R;
import com.nolamarel.onlinelibrary.Adapters.books.Book;
import com.nolamarel.onlinelibrary.Adapters.books.BookAdapter;
import com.nolamarel.onlinelibrary.databinding.FragmentBooksBinding;

import java.util.ArrayList;

public class BooksFragment extends Fragment{
    private FragmentBooksBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBooksBinding.inflate(inflater, container, false);
        String sectionId = getArguments().getString("sectionId");
        //Log.d("MySection", sectionId);

//        ArrayList<Book> books = new ArrayList<>();
//        books.add(new Book("12345", "Margaret Mitchell", "Gone with the Wind", R.drawable.peace));
//        books.add(new Book("12345", "Margaret Mitchell", "Gone with the Wind", R.drawable.peace));
//        books.add(new Book("12345", "Margaret Mitchell", "Gone with the Wind", R.drawable.peace));
//        books.add(new Book("12345", "Margaret Mitchell", "Gone with the Wind", R.drawable.peace));
//        books.add(new Book("12345", "Margaret Mitchell", "Gone with the Wind", R.drawable.peace));
//        books.add(new Book("12345", "Margaret Mitchell", "Gone with the Wind", R.drawable.peace));
//        books.add(new Book("12345", "Margaret Mitchell", "Gone with the Wind", R.drawable.peace));
//        books.add(new Book("12345", "Margaret Mitchell", "Gone with the Wind", R.drawable.peace));
//
//
//        binding.booksRv.setLayoutManager(new LinearLayoutManager(getContext()));
//        binding.booksRv.setAdapter(new BookAdapter(books, this));

        loadBooks(sectionId);



        return binding.getRoot();
    }

    private void loadBooks(String sectionId){
        ArrayList<Book> books = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child("genres").child(sectionId).child("books").getValue() != null){
                    binding.sectionName.setText(snapshot.child("genres").child(sectionId).child("name").getValue().toString());
                    String booksStr = snapshot.child("genres").child(sectionId).child("books").getValue().toString();
                    String[] booksIds = booksStr.split(",");

                    for (String bookId : booksIds){
                        DataSnapshot bookSnapshot = snapshot.child("Books").child(bookId);

                        String bookName = bookSnapshot.child("name").getValue().toString();
                        String bookAuthor = bookSnapshot.child("author").getValue().toString();
                        String myBookId = bookSnapshot.getKey().toString();
                        String bookImage = bookSnapshot.child("image").getValue().toString();


                        books.add(new Book(myBookId, bookAuthor, bookName, bookImage));

                    }

                    binding.booksRv.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.booksRv.setAdapter(new BookAdapter(books, new OnItemClickListener.ItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        String selectedBookId = books.get(position).getBookId();
                        Fragment fragment = new BookDescriptionFragment();
                        Bundle args = new Bundle();
                        args.putString("bookId", selectedBookId);
                        fragment.setArguments(args);
                        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
                    }
                }));

                }

//                for(DataSnapshot bookSnapshot : snapshot.getChildren()){
//                    if(bookSnapshot.child("genre").equals(FirebaseDatabase.getInstance().getReference().child("genres").child("name"))){
//                        continue;
//                    }
//                    String bookName = bookSnapshot.child("name").getValue().toString();
//                    String bookAuthor = bookSnapshot.child("author").getValue().toString();
//                    String bookId = bookSnapshot.getKey().toString();
//
//                    books.add(new Book(bookId, bookAuthor, bookName, R.drawable.peace));
//                }
//
//                binding.booksRv.setLayoutManager(new LinearLayoutManager(getContext()));
//                binding.booksRv.setAdapter(new BookAdapter(books, new OnItemClickListener.ItemClickListener() {
//                    @Override
//                    public void onItemClick(int position) {
//                        String selectedBookId = books.get(position).getBookId();
//                        Fragment fragment = new BookDescriptionFragment();
//                        Bundle args = new Bundle();
//                        args.putString("bookId", selectedBookId);
//                        fragment.setArguments(args);
//                        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
//                    }
//                }));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    @Override
//    public void onItemClick(int position) {
//        Fragment fragment = new BookDescriptionFragment();
//        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
//    }
}

