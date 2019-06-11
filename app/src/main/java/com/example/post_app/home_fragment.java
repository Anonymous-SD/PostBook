package com.example.post_app;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class home_fragment extends Fragment {

    private RecyclerView postsView;
    private List<BookPost> postList;
    private FirebaseFirestore firestore;
    private PostRecycAdapter postRecycAdapter;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseAuth mAuth;
    private static final String TAG = "homeFragment";



    public home_fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_fragment,container, false);

        postList = new ArrayList<>();
        postsView = view.findViewById(R.id.main_post_view);
        postRecycAdapter = new PostRecycAdapter(postList);
        mAuth = FirebaseAuth.getInstance();


        linearLayoutManager =new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postsView.setLayoutManager(linearLayoutManager);

        postsView.setHasFixedSize(true);
        postsView.setAdapter(postRecycAdapter);


        if(mAuth.getCurrentUser()!=null)
        {
        firestore = FirebaseFirestore.getInstance();
        firestore.collection("posts").orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e==null)
                {
                for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                    if(doc.getType()== DocumentChange.Type.ADDED)
                    {

                        BookPost bookPost = doc.getDocument().toObject(BookPost.class);
                        postList.add(bookPost);
                        postRecycAdapter.notifyDataSetChanged();
                    }

                }
            }}
        });
        }


       return view;

    }

}
