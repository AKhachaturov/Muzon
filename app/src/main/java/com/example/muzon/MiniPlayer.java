package com.example.muzon;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class MiniPlayer extends Fragment implements NotificationCenter.Delegate {
    public Song currentSong;
    private TextView name;
    private TextView author;
    private CardView image;
    private FrameLayout Button;
    private ImageView playButton;
    private ImageView pauseButton;
    private boolean isPlaying = false;
    static int n = 0;

    View layout;
    View parent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mini_player, container, false);
        name = view.findViewById(R.id.mini_name);
        author = view.findViewById(R.id.mini_author);
        image = view.findViewById(R.id.mini_card);
        Button = view.findViewById(R.id.mini_btn);
        playButton = view.findViewById(R.id.mini_play);
        pauseButton = view.findViewById(R.id.mini_pause);
        pauseButton.setVisibility(View.INVISIBLE);

        name.setSelected(true);
        author.setSelected(true);


        Button.setOnClickListener((v) -> {
            if(n==0){
                onClick();
                n++;
                NotificationCenter.getInstance().postNotification(NotificationCenter.mini_play);
                PlayerController.getInstance().changePlaying();
            }else{
                n--;
            }
        });

        layout = view;

        NotificationCenter.getInstance().addObserver(NotificationCenter.clickedSong, this);
        NotificationCenter.getInstance().addObserver(NotificationCenter.songChanged, this);
        NotificationCenter.getInstance().addObserver(NotificationCenter.sceneChanged, this);
        NotificationCenter.getInstance().addObserver(NotificationCenter.play, this);
        NotificationCenter.getInstance().addObserver(NotificationCenter.repeatClick, this);

        return view;
    }

    public static MiniPlayer newMiniPlayer() {
        return new MiniPlayer();
    }

    public final void setResources(Song song) {
        if (song != null) {
            name.setText(song.getName());
            name.setText(song.getName());
            author.setText(song.getAuthor());
            Glide.with(MiniPlayer.this).load(song.getImage()).placeholder(R.drawable.photo).into(new CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    image.setForeground(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });


        }
    }

    public final void setSong(Song song) {
        if (song != null) {
            currentSong = song;
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.clickedSong || id == NotificationCenter.songChanged) {
            Song song = (Song) args[0];
            setSong(song);
            setResources(song);
            boolean isPlaying = PlayerController.getInstance().getPlaying();
            if(id == NotificationCenter.clickedSong && !isPlaying){
                onClick();
            }
        } else if(id == NotificationCenter.sceneChanged){
            if((int)args[0] == R.id.endPosition){
                Button.setEnabled(false);
                return;
            }
            Button.setEnabled(true);
        } else if(id == NotificationCenter.play || id == NotificationCenter.repeatClick){
           Log.i("MINI", "PLAY");
           onClick();
        }
    }

    @Override
    public void onDestroy() {
        NotificationCenter.getInstance().removeObserver(NotificationCenter.clickedSong, this);
        NotificationCenter.getInstance().removeObserver(NotificationCenter.songChanged, this);
        NotificationCenter.getInstance().removeObserver(NotificationCenter.sceneChanged, this);
        NotificationCenter.getInstance().removeObserver(NotificationCenter.play, this);
        NotificationCenter.getInstance().removeObserver(NotificationCenter.repeatClick, this);
        super.onDestroy();
    }

    public void onClick(){
        boolean isPlaying = PlayerController.getInstance().getPlaying();
            if (!isPlaying) {
                playButton.setVisibility(View.INVISIBLE);
                pauseButton.setVisibility(View.VISIBLE);
            } else {
                pauseButton.setVisibility(View.INVISIBLE);
                playButton.setVisibility(View.VISIBLE);
            }
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

}
