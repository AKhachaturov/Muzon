package com.example.muzon;

import android.graphics.Bitmap;
import android.net.Uri;


public class Song {
    private static final int MAX_LENGTH = 30;
    private String name;
    private String author;
    private String path;
    private Uri image;
    private long duration;
    private long id;

    public Song(){
      //Empty
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthor(String author){
            this.author = author;
        }


    public void setPath(String path){
        this.path = path;
    }

    public void setImage(Uri image){
        //image = Bitmap.createScaledBitmap(image, 100, 100, false);
        this.image = image;
    }

    public void setDuration(long duration){
        this.duration = duration;
    }

    public void setId(long id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public String getAuthor(){
        return author;
    }

    public Uri getImage(){
        return image;
    }

    public String getPath(){
        return path;
    }

    public long getDuration(){
        return duration;
    }

    public long getId(){
        return id;
    }


}
