package com.example.post_app;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class PostRecycAdapter extends RecyclerView.Adapter<PostRecycAdapter.ViewHolder> {

    public List<BookPost> postList;
    public Context contx;
    private FirebaseFirestore firestore;


    public PostRecycAdapter(List<BookPost> postList)
    {
        this.postList = postList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_item,viewGroup,false);
        contx=viewGroup.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        String descriptionData = postList.get(i).getDescription();
        viewHolder.setDescriptionText(descriptionData);

        String imagePathUrl= postList.get(i).getImagePath();
        viewHolder.setPostImage(imagePathUrl);

        String userId =  postList.get(i).getUserId();

        getNameImage(userId, viewHolder);

         String date= DateFormat.getDateInstance().format(new Date(postList.get(i).getCreatedAt().getTime())).toString();

         viewHolder.setPostDate(date);

    }

    public void getNameImage(String userId, final ViewHolder viewHolder)
    {
        firestore = FirebaseFirestore.getInstance();
        firestore.collection("users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){

                    String name = documentSnapshot.getString("name");
                    viewHolder.setUsernameText(name);

                    String profileImagePath = documentSnapshot.getString("imagePath");
                    viewHolder.setProfileImage(profileImagePath);



                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder
    {

        private View newView;
        private TextView descriptionView;
        private ImageView postImageView;
        private TextView usernameView;
        private TextView postDateView;
        private ImageView profileImageView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            newView = itemView;
        }

        public void setDescriptionText(String descText)
        {
            descriptionView = newView.findViewById(R.id.post_description_txt);
            descriptionView.setText(descText);
        }

        public  void  setPostImage(String imageLoadUri)
        {
            postImageView = newView.findViewById(R.id.post_main_img);
            Glide.with(contx).load(imageLoadUri).into(postImageView);

        }

        public void setUsernameText(String usernameText)
        {
            usernameView = newView.findViewById(R.id.profile_name_txt);
            usernameView.setText(usernameText);
        }
        public  void  setProfileImage(String imageLoadUri)
        {
            profileImageView = newView.findViewById(R.id.circleImageView);
            Glide.with(contx).load(imageLoadUri).into(profileImageView);

        }
        public void setPostDate(String postDate)
        {
            postDateView = newView.findViewById(R.id.post_date_txt);
            postDateView.setText(postDate);
        }
    }
}