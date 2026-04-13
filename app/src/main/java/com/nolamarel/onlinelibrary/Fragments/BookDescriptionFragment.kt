package com.nolamarel.onlinelibrary.Fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nolamarel.onlinelibrary.Adapters.myBooks.MyBook;
import com.nolamarel.onlinelibrary.DatabaseHelper1;
import com.nolamarel.onlinelibrary.databinding.FragmentBookDescriptionBinding;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


public class BookDescriptionFragment extends Fragment {

    private FragmentBookDescriptionBinding binding;
    private String urlPDF;
    private String urlImage;
    private String bookName;
    private String bookAuthor;
    private String bookContent;
    private String bookImage;
    private Bitmap image;

    private Context context;
    private ContentResolver resolver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookDescriptionBinding.inflate(inflater, container, false);


        String bookId = getArguments().getString("bookId");
        loadBook(bookId);
        context = getContext();
        resolver = context.getContentResolver();

        binding.bookDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBook(bookId);
                Toast.makeText(getContext(), "Книга успешно добавлена", Toast.LENGTH_SHORT).show();
            }
        });

        binding.arrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        return binding.getRoot();
    }


    private void addToSql(String bookId){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        FirebaseDatabase.getInstance().getReference().child("Books").child(bookId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                urlPDF = snapshot.child("url").getValue(String.class);
                urlImage = snapshot.child("image").getValue(String.class);
                bookAuthor = snapshot.child("author").getValue(String.class);
                bookName = snapshot.child("name").getValue(String.class);
                if (urlPDF != null) {
                    if(urlImage == null){
                        Toast.makeText(context, "Изображение не найдено", Toast.LENGTH_SHORT).show();
                    }
                    downloadPdfFromFirebaseStorage(urlPDF, urlImage, bookId);
                } else {
                    Toast.makeText(context, "Невозможно загрузить содержимое книги", Toast.LENGTH_SHORT).show();
                }
                DatabaseHelper1 dbHelper = new DatabaseHelper1(context, userId);
                List<MyBook> books = dbHelper.getAllMyBooks();
                if (dbHelper.addBook(new MyBook(0, bookAuthor, bookName, bookImage, bookContent))){
                    books.add(new MyBook(0, bookAuthor, bookName, bookImage, bookContent));
                    Toast.makeText(context, "Книга успешно добавлена", Toast.LENGTH_SHORT).show();
                }
                dbHelper.close();
                for (MyBook book : books) {
                    Log.d("haha", "Книга: " + bookId);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    void downloadPdfFromFirebaseStorage(String urlPDF, String urlImage, String bookId){
        StorageReference pdfRef = FirebaseStorage.getInstance().getReferenceFromUrl(urlPDF);
        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(urlImage);

        final File localFile;
        final File localImageFile;
        try {
            localFile = File.createTempFile("Book " + bookId, "pdf");
            localImageFile = File.createTempFile("Book " + bookId, "jpg");

            pdfRef.getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        Uri pdfUri = Uri.fromFile(localFile);
                        File downloadsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "myApp");
                        if (!downloadsDir.exists()) {
                            downloadsDir.mkdirs();
                        }
                        ContentResolver resolver = getContext().getContentResolver();
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Downloads.DISPLAY_NAME, "Book " + bookId + ".pdf");
                        values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                        Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), values);
                        try {
                            InputStream inputStream = new FileInputStream(localFile);
                            OutputStream outputStream = resolver.openOutputStream(uri);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                FileUtils.copy(inputStream, outputStream);
                            }
                            localFile.delete();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    })
                    .addOnFailureListener(e -> {
                    });

            imageRef.getFile(localImageFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        // PDF файл успешно загружен
                        Uri imageUri = Uri.fromFile(localImageFile);
                        File downloadsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "myApp");
                        if (!downloadsDir.exists()) {
                            downloadsDir.mkdirs();
                        }
                        ContentResolver resolver = getContext().getContentResolver();
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Downloads.DISPLAY_NAME, "Book " + bookId + ".jpg");
                        values.put(MediaStore.Downloads.MIME_TYPE, "application/jpg");
                        Uri uri1 = resolver.insert(MediaStore.Files.getContentUri("external"), values);
                        try {
                            InputStream inputStream = new FileInputStream(localImageFile);
                            OutputStream outputStream = resolver.openOutputStream(uri1);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                FileUtils.copy(inputStream, outputStream);
                            }
                            localImageFile.delete();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    })
                    .addOnFailureListener(e -> {
                    });

            bookContent = "/storage/emulated/0/Download/Book " + bookId + ".pdf";
            bookImage = "/storage/emulated/0/Download/Book " + bookId + ".jpg";


        } catch (IOException e) {
            e.printStackTrace();
        }





    }

    private void addBook(String bookId){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("books")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String[] booksIds = snapshot.getValue().toString().split(",");
                            boolean bookExists = false;
                            for (String bookIdOld : booksIds) {
                                if (bookIdOld.equals(bookId)) {
                                    bookExists = true;
                                    break;
                                }
                            }
                            if (bookExists) {
                                Toast.makeText(context, "Книга уже добавлена в библиотеку", Toast.LENGTH_SHORT).show();
                            } else {
                                String currentBooksValue = (String) snapshot.getValue();
                                if (currentBooksValue != null && !currentBooksValue.isEmpty()) {
                                    currentBooksValue += "," + bookId;
                                } else {
                                    currentBooksValue = bookId;
                                }
                                FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("books").setValue(currentBooksValue);
                                addToSql(bookId);
                            }
                        } else {
                            FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("books").setValue(bookId);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void loadBook(String bookId){
        FirebaseDatabase.getInstance().getReference().child("Books").child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    binding.bookName.setText(snapshot.child("name").getValue().toString());
                    binding.bookAuthor.setText(snapshot.child("author").getValue().toString());
                    binding.bookDesc.setText(snapshot.child("description").getValue().toString());
                    Glide.with(getContext()).load(snapshot.child("image").getValue().toString()).into(binding.bookIv);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //binding.bookName.setText(FirebaseDatabase.getInstance().getReference().child("Books").child(bookId).child("name").getValue());
    }
}