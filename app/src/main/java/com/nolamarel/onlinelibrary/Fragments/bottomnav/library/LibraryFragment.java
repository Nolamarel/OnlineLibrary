package com.nolamarel.onlinelibrary.Fragments.bottomnav.library;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nolamarel.onlinelibrary.Adapters.myBooks.MyBook;
import com.nolamarel.onlinelibrary.Adapters.myBooks.MyBookAdapter;
import com.nolamarel.onlinelibrary.Fragments.BookDescriptionFragment;
import com.nolamarel.onlinelibrary.OnItemClickListener;
import com.nolamarel.onlinelibrary.R;
import com.nolamarel.onlinelibrary.databinding.FragmentLibraryBinding;

import java.lang.reflect.Array;
import java.util.ArrayList;

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
        ArrayList<MyBook> books = new ArrayList<>();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userBooks = snapshot.child("Users").child(userId).child("books").getValue().toString();
                if (!userBooks.isEmpty()){
                    String bookStr = snapshot.child("Users").child(userId).child("books").getValue().toString();
                    String[] bookIds = bookStr.split(",");

                    for (String bookId : bookIds){
                        DataSnapshot bookSnapshot = snapshot.child("Books").child(bookId);
                        String image = bookSnapshot.child("image").getValue().toString();
                    String id = bookSnapshot.getKey().toString();

                    books.add(new MyBook(image, id, "99%"));

                    }
                    binding.libraryRv.setLayoutManager(new GridLayoutManager(getContext(), 2));

                    binding.libraryRv.setAdapter(new MyBookAdapter(books, new OnItemClickListener.ItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        String selectedBook = books.get(position).getBookId();
                        Fragment fragment = new BookDescriptionFragment();
                        Bundle args = new Bundle();
                        args.putString("bookId", selectedBook);
                        fragment.setArguments(args);
                        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
                    }
                }));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        FirebaseDatabase.getInstance().getReference().child("Books").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for(DataSnapshot bookSnapshot : snapshot.getChildren()){
//                    String image = bookSnapshot.child("image").getValue().toString();
//                    String id = bookSnapshot.getKey().toString();
//
//                    books.add(new MyBook(image, id, "99%"));
//                }
//                binding.libraryRv.setLayoutManager(new GridLayoutManager(getContext(), 2));
//                binding.libraryRv.setAdapter(new MyBookAdapter(books, new OnItemClickListener.ItemClickListener() {
//                    @Override
//                    public void onItemClick(int position) {
//                        String selectedBook = books.get(position).getBookId();
//                        Fragment fragment = new BookDescriptionFragment();
//                        Bundle args = new Bundle();
//                        args.putString("bookId", selectedBook);
//                        fragment.setArguments(args);
//                        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
//                    }
//                }));
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

}