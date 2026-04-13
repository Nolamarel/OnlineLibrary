package com.nolamarel.onlinelibrary.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.nolamarel.onlinelibrary.databinding.FragmentSupportServiceBinding;

import java.util.HashMap;

public class SupportServiceFragment extends Fragment {
    private FragmentSupportServiceBinding binding;
    private String problemText;
    private String emailText;
    private String messageText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentSupportServiceBinding.inflate(inflater, container, false);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        binding.arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        binding.supSerMessageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int characterCount = s.length();
                binding.supSerSymbolNow.setText(String.valueOf(characterCount) + "/20000");
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditText problem = binding.supSerThemeEt;
        EditText email = binding.supSerEmailEt;
        EditText message = binding.supSerMessageEt;

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean allFieldsValid = true;

                problemText = binding.supSerThemeEt.getText().toString();
                emailText = binding.supSerEmailEt.getText().toString();
                messageText = binding.supSerMessageEt.getText().toString();

                if (problemText.isEmpty()){
                    problem.setError("Введите проблему");
                    allFieldsValid = false;
                }
                if (emailText.isEmpty()){
                    email.setError("Введите адрес электронной почты");
                    allFieldsValid = false;
                }
                if (messageText.length() < 20){
                    message.setError("Слишком короткое описание");
                    allFieldsValid = false;
                }

                if (allFieldsValid) {
                    sendMessage(userId, problemText, emailText, messageText);
                    Toast.makeText(getContext(), "Сообщение успешно отправлено", Toast.LENGTH_SHORT).show();
                }
            }
        });




        return binding.getRoot();
    }

    public void sendMessage(String userId, String problemText, String emailText, String messageText){
        if (userId == null) return;
        HashMap<String, String> messageInfo = new HashMap<>();
        messageInfo.put("userId", userId);
        messageInfo.put("problem", problemText);
        messageInfo.put("email", emailText);
        messageInfo.put("message", messageText);
        FirebaseDatabase.getInstance().getReference().child("Messages").push().setValue(messageInfo);
    }

}