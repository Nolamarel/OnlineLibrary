package com.nolamarel.onlinelibrary.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.nolamarel.onlinelibrary.R;
import com.nolamarel.onlinelibrary.databinding.ActivityReadingBinding;

import java.io.File;

public class ReadingActivity extends AppCompatActivity {
    private ActivityReadingBinding binding;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReadingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activity = this;

        String filePath = RecentBookContent.getVariable();
        //Сделать проверку
        if(filePath != ""){
            binding.toolbarReadingTitle.setText(RecentBookContent.getName());
        }

        binding.pdfView.fromFile(new File(filePath))
                .enableSwipe(true) // Разрешить свайп по страницам
                .swipeHorizontal(false) // Горизонтальный свайп
                .enableAntialiasing(true)
                .fitEachPage(true)
                .pageSnap(true)
                .enableDoubletap(true)
                .defaultPage(0)
                .password(null)
                .spacing(0)
                .autoSpacing(false)
                .pageFitPolicy(FitPolicy.BOTH)
                .pageSnap(false)
                .pageFling(false)
                .nightMode(false)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();

        binding.arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });


    }



}