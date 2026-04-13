package com.nolamarel.onlinelibrary.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nolamarel.onlinelibrary.databinding.FragmentChangePasswordBinding;

public class ChangePasswordFragment extends Fragment {
    private FragmentChangePasswordBinding binding;
    private FirebaseAuth mAuth;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);

        mAuth = FirebaseAuth.getInstance();
        binding.arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.myEmail.getText().toString();
                String oldPassword = binding.oldPassword.getText().toString();
                String newPassword = binding.newPassword.getText().toString();
                String newPasswordRep = binding.newPasswordRep.getText().toString();


                boolean allFieldsValid = true;

                if (email.isEmpty()){
                    binding.myEmail.setError("Поле не может быть пустым");
                    allFieldsValid = false;
                }
                if (oldPassword.isEmpty()){
                    binding.oldPassword.setError("Поле не может быть пустым");
                    allFieldsValid = false;
                }
                if (newPassword.length() < 8){
                    binding.newPassword.setError("Минимум 8 символов");
                    allFieldsValid = false;

                }
                if (oldPassword.equals(newPassword)){
                    binding.newPassword.setError("Пароли не могут совпадать");
                    allFieldsValid = false;
                }
                if (!newPassword.equals(newPasswordRep)){
                    binding.newPassword.setError("Пароли не совпадают");
                    allFieldsValid = false;
                }
                if (allFieldsValid) {
                    // Вызываем функцию
                    changePassword(email, oldPassword, newPassword);
                }
            }
        });


        return binding.getRoot();
    }

    private void changePassword(String email, String oldPassword, String newPassword){
        FirebaseUser user = mAuth.getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);
        if (oldPassword == newPassword){
            Toast.makeText(getContext(), "Введите пароль, отличный от старого", Toast.LENGTH_SHORT).show();
        } else {

            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Пароль успешно изменен", Toast.LENGTH_SHORT).show();
                                    getParentFragmentManager().popBackStack();
                                } else {
                                    Toast.makeText(getContext(), "Некорректный пароль", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Некорректна почта и/или пароль", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }

}