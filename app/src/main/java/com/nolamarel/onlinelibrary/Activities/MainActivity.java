package com.nolamarel.onlinelibrary.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.nolamarel.onlinelibrary.Fragments.bottomnav.library.LibraryFragment;
import com.nolamarel.onlinelibrary.Fragments.bottomnav.main.MainFragment;
import com.nolamarel.onlinelibrary.Fragments.bottomnav.search.SearchFragment;
import com.nolamarel.onlinelibrary.Fragments.bottomnav.profile.ProfileFragment;
import com.nolamarel.onlinelibrary.R;
import com.nolamarel.onlinelibrary.databinding.ActivityMainBinding;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new MainFragment());



        mAuth = FirebaseAuth.getInstance();
        context = this;

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user == null){
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        });




        binding.bottonNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.main) {
                replaceFragment(new MainFragment());
            } else if (item.getItemId() == R.id.profile) {
                replaceFragment(new ProfileFragment());
            }
            else if (item.getItemId() == R.id.reader) {
                Intent intent = new Intent(MainActivity.this, ReadingActivity.class);
                startActivity(intent);

            } else if (item.getItemId() == R.id.library) {
                replaceFragment(new LibraryFragment());
            } else if (item.getItemId() == R.id.search) {
                replaceFragment(new SearchFragment());
            }
            return true;

        });
    }


    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }


}