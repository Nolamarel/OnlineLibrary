package com.nolamarel.onlinelibrary.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.nolamarel.onlinelibrary.R;
import com.nolamarel.onlinelibrary.databinding.ActivitySignUpBinding;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    Activity activity;
    private String email, password, repPassword, userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        activity = this;
        binding.arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });

        binding.signUpBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = binding.email.getText().toString();
                password = binding.password.getText().toString();
                repPassword = binding.passwordRep.getText().toString();
                userName = binding.userName.getText().toString();
                if(checkPassword(password, repPassword, email, userName) == true ){
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                HashMap<String, String > userInfo = new HashMap<>();
                                userInfo.put("email", email);
                                userInfo.put("profileImage", "");
                                userInfo.put("books", "");
                                userInfo.put("username", userName);
                                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> databaseTask) {
                                                if (databaseTask.isSuccessful()){
                                                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                                } else{
                                                    Toast.makeText(SignUpActivity.this, "Saving user error", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                HashMap<String, String > userRole = new HashMap<>();
                                userRole.put("role", "user");
                                FirebaseDatabase.getInstance().getReference().child("Roles").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(userRole);
                            }
                        }
                    });
                };

            }
        });

    }

    public boolean checkPassword(String pas1, String pas2, String email, String userName){
        if(email.isEmpty() || userName.isEmpty() || pas1.isEmpty() || pas2.isEmpty()){
            Toast.makeText(this, "Fields can't be empty", Toast.LENGTH_SHORT).show();
        } else {
            if(pas1.length() < 8){
                Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show();
            } else{
                if (!pas1.equals(pas2)){
                    Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                } else {
                    return true;
                }
            }
        }
        return false;
    }
}