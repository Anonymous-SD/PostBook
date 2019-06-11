package com.example.post_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSetup extends AppCompatActivity {

    private Toolbar setupToolbar;
    private CircleImageView setProfileImage;
    private Uri cropImageResultUri = null;
    private EditText profileName;
    private Button profileUpdate;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private ProgressBar updateProfileProgress;
    private StorageReference picturePath;
    private FirebaseFirestore fireStore;
    private FirebaseFirestoreSettings firebaseSettings;
    private String userId;
    private boolean imageChanged=false;
    public static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        setupToolbar = findViewById(R.id.profile_setup_toolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Profile Setup");


        setProfileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.txt_profile_name);
        profileUpdate = findViewById(R.id.btn_setup_profile);
        updateProfileProgress = findViewById(R.id.update_profile_progress);
        profileUpdate.setEnabled(false);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        fireStore = FirebaseFirestore.getInstance();
//        firebaseSettings = new FirebaseFirestoreSettings.Builder()
//                .setTimestampsInSnapshotsEnabled(true)
//                .build();
//        fireStore.setFirestoreSettings(firebaseSettings);
        storageReference = FirebaseStorage.getInstance().getReference();

        updateProfileProgress.setVisibility(View.VISIBLE);

        fireStore.collection("users").document(userId).get()
        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){

                    String name = documentSnapshot.getString("name");
                    String retrievedImageUrl = documentSnapshot.getString("imagePath");
                    cropImageResultUri = Uri.parse(retrievedImageUrl);
                    profileName.setText(name);


                    RequestOptions requestOptions= new RequestOptions();
                   requestOptions.placeholder(R.drawable.default_profile);
                   Glide.with(ProfileSetup.this).setDefaultRequestOptions(requestOptions).load(retrievedImageUrl).into(setProfileImage);


                }
                else
                {   updateProfileProgress.setVisibility(View.INVISIBLE);
                    Toast.makeText(ProfileSetup.this, "Please set up your profile.", Toast.LENGTH_SHORT).show();
                }
                updateProfileProgress.setVisibility(View.INVISIBLE);
                profileUpdate.setEnabled(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                updateProfileProgress.setVisibility(View.INVISIBLE);
                Toast.makeText(ProfileSetup.this,
                        e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }

        });


        profileUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userProfileName = profileName.getText().toString().trim();


                    if (!TextUtils.isEmpty(userProfileName) && cropImageResultUri != null) {
                        if (imageChanged) {
                        updateProfileProgress.setVisibility(View.VISIBLE);

                        picturePath = storageReference.child("profileImages")
                                .child(userId + ".jpg");

                        UploadTask uploadTask = picturePath.putFile(cropImageResultUri);

                        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return picturePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();


                                    uploadToFirestore(userProfileName, downloadUri);


                                    updateProfileProgress.setVisibility(View.INVISIBLE);
                                } else {

                                    Toast.makeText(ProfileSetup.this, task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    updateProfileProgress.setVisibility(View.INVISIBLE);
                                }
                            }
                        });


                    } else {
                            uploadToFirestore(userProfileName, cropImageResultUri);
                    }
                }else{

                        Toast.makeText(ProfileSetup.this, "Add Profile Picture and Profile Name",
                                Toast.LENGTH_SHORT).show();

                }
            }
        });

        setProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(ProfileSetup.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(ProfileSetup.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_STORAGE);

                    } else {
                        imageCropper();
                    }
                } else {
                    imageCropper();
                }


            }
        });

    }

    private void uploadToFirestore(String userProfileName,Uri downloadUri)
        {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("name", userProfileName);
            dataMap.put("imagePath", downloadUri.toString());

            fireStore.collection("users").
                    document(userId).set(dataMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ProfileSetup.this,
                                    "Profile Updated Successfully",
                                    Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(ProfileSetup.this, Post.class);
                            startActivity(intent);
                            finish();


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileSetup.this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }


            });
        }

    private void imageCropper() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(ProfileSetup.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                cropImageResultUri = result.getUri();

                setProfileImage.setImageURI(cropImageResultUri);
                imageChanged=true;


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted.", Toast.LENGTH_SHORT).show();
                    imageCropper();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

}
