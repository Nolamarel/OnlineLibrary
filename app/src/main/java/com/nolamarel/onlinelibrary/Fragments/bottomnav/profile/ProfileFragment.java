package com.nolamarel.onlinelibrary.Fragments.bottomnav.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.nolamarel.onlinelibrary.Fragments.AboutAppFragment;
import com.nolamarel.onlinelibrary.Activities.LoginActivity;
import com.nolamarel.onlinelibrary.Fragments.ChangePasswordFragment;
import com.nolamarel.onlinelibrary.DatabaseHelper1;
import com.nolamarel.onlinelibrary.LocalizationManager;
import com.nolamarel.onlinelibrary.R;
import com.nolamarel.onlinelibrary.Fragments.SupportServiceFragment;
import com.nolamarel.onlinelibrary.databinding.FragmentProfileBinding;

import java.io.IOException;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private String username;
    private String userImage;
    private Context context;
    private String status;
    private Uri filePath;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container,
                             @NonNull Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        context = getContext();


        loadUserInfo();

        binding.cardViewIn21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        binding.cardViewIn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new SupportServiceFragment();
                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
            }
        });

        binding.cardViewIn23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        binding.cardViewIn22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new ChangePasswordFragment();
                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
            }
        });

        binding.cardViewIn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new AboutAppFragment();
                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
            }
        });

        LocalizationManager localizationManager = new LocalizationManager(getContext());
        binding.languageTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (localizationManager.getCurrentLanguage().equals("ru")) {
                    localizationManager.setLanguage("en");
                } else {
                    localizationManager.setLanguage("ru");
                }
            }
        });


        return binding.getRoot();
    }


    private void loadUserInfo(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        long createdAt = FirebaseAuth.getInstance().getCurrentUser().getMetadata().getCreationTimestamp();

        FirebaseDatabase.getInstance().getReference().child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("username").exists()){
                username = snapshot.child("username").getValue().toString();}

                if (snapshot.child("profileImage").exists()){
                userImage = snapshot.child("profileImage").getValue().toString();
                    if(!userImage.isEmpty()){
                        Glide.with(getContext()).load(userImage).into(binding.profileImage);
                    }}

                binding.profileUsername.setText(username);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseHelper1 dbHelper = new DatabaseHelper1(context, userId);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowCount = dbHelper.getRowCount(db);


        if (rowCount == 0){
            status = getString(R.string.beginner);
        }else if(rowCount > 0 && rowCount < 4){
            status = getString(R.string.reader_);
        } else if(rowCount > 4 && rowCount < 8){
            status = getString(R.string.bookworm);
        } else if (rowCount > 8 && rowCount < 12){
            status = getString(R.string.librarian);
        } else {
            status = getString(R.string.reading_wizard);
        }

        binding.profileStatus.setText(status);

    }

    private void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        pickImageActivityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> pickImageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData()!=null){
                        filePath = result.getData().getData();

                        try {
                            Bitmap bitmap = MediaStore.Images.Media
                                    .getBitmap(
                                            requireContext().getContentResolver(),
                                            filePath
                                    );
                            binding.profileImage.setImageBitmap(bitmap);
                        } catch (IOException e){
                            e.printStackTrace();
                        }

                        uploadImage();
                    }
                }
            });

    private void uploadImage(){
        if (filePath!=null){
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseStorage.getInstance().getReference().child("images/" + uid)
                    .putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getContext(), "Photo upload complete", Toast.LENGTH_SHORT).show();

                            FirebaseStorage.getInstance().getReference().child("images/"+uid).getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .child("profileImage").setValue(uri.toString());
                                        }
                                    });
                        }
                    });
        }
    }

}