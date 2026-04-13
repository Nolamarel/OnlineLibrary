package com.nolamarel.onlinelibrary.Fragments.bottomnav.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nolamarel.onlinelibrary.Adapters.sections.Section;
import com.nolamarel.onlinelibrary.Adapters.sections.SectionAdapter;
import com.nolamarel.onlinelibrary.Fragments.BooksFragment;
import com.nolamarel.onlinelibrary.OnItemClickListener;
import com.nolamarel.onlinelibrary.R;
import com.nolamarel.onlinelibrary.databinding.FragmentMainBinding;

import java.util.ArrayList;

public class MainFragment extends Fragment {
    private FragmentMainBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);


        loadSections();


        return binding.getRoot();
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

                binding.booksMainRv.setLayoutManager(new GridLayoutManager(getContext(), 2));
                binding.booksMainRv.setAdapter(new SectionAdapter(sections, new OnItemClickListener.ItemClickListener() {
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