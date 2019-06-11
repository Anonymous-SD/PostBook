package com.example.post_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText email,password,confirmPassword;
    private Button register;
    private static final String TAG = "MainActivity";
    private FirebaseUser loggedInUser;
    private ProgressBar registerProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        TextView login = findViewById(R.id.lnkLogin);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        });



        email = findViewById(R.id.txtEmail);
        password = findViewById(R.id.txtPwd);
        confirmPassword = findViewById(R.id.txtConfirmPwd);
        register = findViewById(R.id.btnregister);
        registerProgress = findViewById(R.id.register_progress);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterAuth();
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        loggedInUser = mAuth.getCurrentUser();

        if(loggedInUser != null)
        {
            Intent intent = new Intent(MainActivity.this, Post.class);
            startActivity(intent);
            finish();
        }

    }

    private void RegisterAuth() {
        String emailFirebase = email.getText().toString().trim();
        String passwordFirebase = password.getText().toString().trim();
        String confirmPasswordFirebase = confirmPassword.getText().toString().trim();

        if (!emailFirebase.isEmpty() && !passwordFirebase.isEmpty()) {
            if (passwordFirebase.equals(confirmPasswordFirebase))
            {
            registerProgress.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(emailFirebase, passwordFirebase)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
//                                Toast.makeText(MainActivity.this, "Register Successful", Toast.LENGTH_LONG).show();
//
//                              Intent intent = new Intent(MainActivity.this, Post.class);
//                              startActivity(intent);
//                              finish();

                                Intent intent = new Intent(MainActivity.this, ProfileSetup.class);
                                startActivity(intent);
                                finish();



                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                            registerProgress.setVisibility(View.INVISIBLE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, e.getMessage());
                }

            });
        }
            else
            {
                Toast.makeText(MainActivity.this, "Password and Confirm Password do not match.",
                        Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Log.w(TAG, "signUpWithEmail:failure");
            Toast.makeText(MainActivity.this, "Please enter email and password.",
                    Toast.LENGTH_SHORT).show();
        }
    }


}