package com.example.post_app;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Post extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser loggedInUser;
    private Toolbar topToolbar;
    private int topMenuSelectedItem;
    private FloatingActionButton bottomFloatingAction;
    private FirebaseFirestore fireStore;
    private String userId;
    private BottomNavigationView bottomNavigation;
    private Fragment fragmentHome;
    private Fragment fragmentNotification;
    private Fragment fragmentAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();




        bottomNavigation = findViewById(R.id.bottom_navigation);
        topToolbar = (Toolbar) findViewById(R.id.top_toolbar);
        fragmentAccount = new account_fragment();
        fragmentHome = new home_fragment();
        fragmentNotification = new notification_fragment();
        switchFragment(fragmentHome);


        setSupportActionBar(topToolbar);

        getSupportActionBar().setTitle("PostBook");
        bottomFloatingAction = findViewById(R.id.floating_action_button);


        bottomFloatingAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Post.this, AddPost.class);
                startActivity(intent);

            }
        });

        bottomNavigation.setOnNavigationItemReselectedListener(new  BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem menuItem) {

                int menuItemId = menuItem.getItemId();
                if(menuItemId==R.id.bottom_account_item)
                {
                    switchFragment(fragmentAccount);
                }
                else if(menuItemId== R.id.bottom_home_item)
                {
                    switchFragment(fragmentHome);

                }else if(menuItemId==R.id.bottom_notification_item)
                {
                    switchFragment(fragmentNotification);
                }

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        loggedInUser = mAuth.getCurrentUser();

        if (loggedInUser == null) {
            sendToLogin();

        } else {

            fireStore.collection("users").document(userId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(!documentSnapshot.exists())
                    {
                        Intent intent = new Intent(Post.this, ProfileSetup.class);
                        startActivity(intent);
                        finish();

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Post.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        topMenuSelectedItem = item.getItemId();

        if(topMenuSelectedItem==R.id.top_menu_search)
        {
            return true;
        }
        else if(topMenuSelectedItem==R.id.top_menu_settings)
        {
            Intent intent = new Intent(Post.this, ProfileSetup.class);
            startActivity(intent);
            return true;
        }
        else if(topMenuSelectedItem==R.id.top_menu_logout)
        {
            mAuth.signOut();
            sendToLogin();
            return true;
        }



        return false;
    }

    private void sendToLogin()
    {
        Intent intent = new Intent(Post.this, Login.class);
        startActivity(intent);
        finish();
    }

    private void  switchFragment(Fragment fragment)
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }
}
