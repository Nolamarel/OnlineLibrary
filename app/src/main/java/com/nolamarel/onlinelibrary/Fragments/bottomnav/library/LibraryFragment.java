package com.nolamarel.onlinelibrary.Fragments.bottomnav.library;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.nolamarel.onlinelibrary.Activities.ReadingActivity;
import com.nolamarel.onlinelibrary.Activities.RecentBookContent;
import com.nolamarel.onlinelibrary.Adapters.myBooks.MyBook;
import com.nolamarel.onlinelibrary.Adapters.myBooks.MyBookAdapter;
import com.nolamarel.onlinelibrary.DatabaseHelper1;
import com.nolamarel.onlinelibrary.OnItemClickListener;
import com.nolamarel.onlinelibrary.databinding.FragmentLibraryBinding;

import java.util.List;

public class LibraryFragment extends Fragment {
    private FragmentLibraryBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);


        loadBooks();

        return binding.getRoot();
    }


           private void loadBooks(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        DatabaseHelper1 dbHelper = new DatabaseHelper1(getContext(), userId);
        List<MyBook> books =  dbHelper.getAllMyBooks();
        binding.libraryRv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.libraryRv.setAdapter(new MyBookAdapter(books, new OnItemClickListener.ItemClickListener() {
            @Override
            public void onItemClick(int position) {

                Intent intent = new Intent(requireContext(), ReadingActivity.class);
                String bookContent = books.get(position).getBookContent();
                String bookName = books.get(position).getBookName();
                RecentBookContent.setName(bookName);
                RecentBookContent.setVariable(bookContent);
                startActivity(intent);
            }
        }) {
            @Override
            public void onItemClick(int position) {

            }
        });
           }
    }