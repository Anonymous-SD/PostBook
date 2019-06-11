package com.example.post_app;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText email, password;
    private Button signIn;
    private static final String TAG = "Login";
    private FirebaseUser loggedInUser;
    private ProgressBar loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        TextView register = (TextView) findViewById(R.id.lnkRegister);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        email = findViewById(R.id.txtEmail);
        password = findViewById(R.id.txtPwd);
        signIn = findViewById(R.id.btnLogin);
        loginProgress = findViewById(R.id.login_progress);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SignIn();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        loggedInUser = mAuth.getCurrentUser();

        if (loggedInUser != null) {
            sendToPost();

        }


    }

    private void SignIn() {
        String e = email.getText().toString();
        String p = password.getText().toString();

        if(!e.isEmpty() && !p.isEmpty()) {

            loginProgress.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(e, p)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                sendToPost();

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(Login.this, task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }

                            loginProgress.setVisibility(View.INVISIBLE);
                        }
                    });
        }
        else
        {
            Log.w(TAG, "signInWithEmail:failure");
            Toast.makeText(Login.this, "Please enter email and password.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private void sendToPost()
    {
        Intent intent = new Intent(Login.this, Post.class);
        startActivity(intent);
        finish();
    }
}
