package com.nolamarel.onlinelibrary.Fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nolamarel.onlinelibrary.Fragments.BooksFragment;
import com.nolamarel.onlinelibrary.databinding.FragmentAboutAppBinding;

public class AboutAppFragment extends Fragment {
    private FragmentAboutAppBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAboutAppBinding.inflate(inflater, container, false);

        binding.arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        return binding.getRoot();
    }
}