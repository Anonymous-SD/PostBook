package com.example.post_app;

import java.util.Date;

public class BookPost {
    public String userId;
    public String imagePath;
    public String description;
    public Date createdAt;


    public BookPost(String userId, String imagePath, String description, Date createdAt) {
        this.userId = userId;
        this.imagePath = imagePath;
        this.description = description;
        this.createdAt = createdAt;
    }

    public BookPost()
    {

    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }




}
