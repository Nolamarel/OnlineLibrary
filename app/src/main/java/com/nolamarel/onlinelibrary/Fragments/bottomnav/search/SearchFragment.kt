package com.nolamarel.onlinelibrary.Fragments.bottomnav.search;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nolamarel.onlinelibrary.Adapters.books.Book;
import com.nolamarel.onlinelibrary.Adapters.books.BookAdapter;
import com.nolamarel.onlinelibrary.Adapters.sections.Section;
import com.nolamarel.onlinelibrary.Adapters.sections.SectionAdapter;
import com.nolamarel.onlinelibrary.Fragments.BookDescriptionFragment;
import com.nolamarel.onlinelibrary.Fragments.BooksFragment;
import com.nolamarel.onlinelibrary.OnItemClickListener;
import com.nolamarel.onlinelibrary.R;
import com.nolamarel.onlinelibrary.databinding.FragmentSearchBinding;

import java.util.ArrayList;

public class SearchFragment extends Fragment {
private FragmentSearchBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);

        //loadSections();
        binding.serchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchBook(newText);
                return false;
            }
        });

        binding.serchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //loadSections();
                return false;
            }
        });


        return binding.getRoot();
    }

    private void searchBook(String newText){
        ArrayList<Book> books = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child("Books").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot bookSnapshot : snapshot.getChildren()){
                    if(bookSnapshot.child("name").toString().toLowerCase().contains(newText.toLowerCase()) || bookSnapshot.child("author").toString().toLowerCase().contains(newText.toLowerCase())){
                        String name = bookSnapshot.child("name").getValue().toString();
                        String author = bookSnapshot.child("author").getValue().toString();
                        String image = bookSnapshot.child("image").getValue().toString();
                        String id = bookSnapshot.getKey().toString();

                        books.add(new Book(id, author, name, image));
                    }
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadSections(){
        ArrayList<Section> sections = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("genres").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot sectionSnapshot: snapshot.getChildren()){
                    String name = sectionSnapshot.child("name").getValue().toString();
                    String id = sectionSnapshot.getKey().toString();
                    String sectionImage = sectionSnapshot.child("image").getValue().toString();

                    sections.add(new Section(name, sectionImage, id));
                }

                binding.booksRv.setLayoutManager(new GridLayoutManager(getContext(), 2));
                binding.booksRv.setAdapter(new SectionAdapter(sections, new OnItemClickListener.ItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        String selectedSection = sections.get(position).getSectionId();
                        Log.d("sectionName", selectedSection);
                        Fragment fragment = new BooksFragment();
                        Bundle args = new Bundle();
                        args.putString("sectionId", selectedSection);
                        fragment.setArguments(args);
                        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
                    }
                }));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}