package com.example.post_app;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class AddPost extends AppCompatActivity {

    private Toolbar addPostToolbar;
    private ImageView postImage;
    private EditText descriptionText;
    private Button postButton;
    private Uri postDataUri;
    private ProgressBar postProgressBar;
    private StorageReference storageReference, imagePath;
    private FirebaseFirestore fireStore;
    private FirebaseAuth mAuth;
    private String userId;
    private String currentTimeStamp;
    private File compressedImageFile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        addPostToolbar = findViewById(R.id.add_post_toolbar);
        setSupportActionBar(addPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        storageReference = FirebaseStorage.getInstance().getReference();
        fireStore = FirebaseFirestore.getInstance();


        postImage = findViewById(R.id.add_post_image);
        descriptionText = findViewById(R.id.post_description);
        postButton = findViewById(R.id.post_image_button);
        postProgressBar = findViewById(R.id.post_progressbar);


        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(550, 440)
                        .setAspectRatio(5, 4)
                        .start(AddPost.this);
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String description = descriptionText.getText().toString();
                if (!TextUtils.isEmpty(description) && postDataUri != null) {
                    postProgressBar.setVisibility(View.VISIBLE);


                    imagePath = storageReference.child("postImages").child(UUID.randomUUID().toString() + ".jpg");
                    UploadTask uploadTask = imagePath.putFile(postDataUri);

                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return imagePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {

                                Uri downloadUri = task.getResult();


                                Map<String, Object> dataMap = new HashMap<>();
                                dataMap.put("userId", userId);
                                dataMap.put("description", description);
                                dataMap.put("imagePath", downloadUri.toString());
                                dataMap.put("createdAt", FieldValue.serverTimestamp());

                                fireStore.collection("posts").add(dataMap)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {

                                                Toast.makeText(AddPost.this,
                                                        "Post Uploaded Successfully.", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(AddPost.this, Post.class);
                                                startActivity(intent);
                                                finish();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AddPost.this,
                                                e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }

                                });


                                postProgressBar.setVisibility(View.INVISIBLE);
                            } else {

                                Toast.makeText(AddPost.this, task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                postProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });

                } else {
                    Toast.makeText(AddPost.this, "Please add image and Description",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postDataUri = result.getUri();


                File imgFile = new File(postDataUri.getPath());
                try {
                    compressedImageFile = new Compressor(AddPost.this)
                            .setMaxHeight(100)
                            .setMaxWidth(150)
                            .setQuality(75)
                            .compressToFile(imgFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                postImage.setImageURI(Uri.fromFile(compressedImageFile));

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(AddPost.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }


    }
}
