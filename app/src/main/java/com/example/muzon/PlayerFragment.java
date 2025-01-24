package com.example.muzon;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;


import java.util.ArrayList;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class PlayerFragment extends Fragment implements NotificationCenter.Delegate, View.OnTouchListener{

    private ArrayList<Song> songs;
    private ViewPager2 pager;
    private RecyclerView.Adapter pagerAdapter;
    private ImageView image;
    private View curCard;
    private Timer timer;
    private SeekBar seekBar;
    volatile boolean isPlaying = false;
    private TextView durationView;
    RecyclerView recyclerView;
    int curPos = -1;
    private View nextBtn;
    private PlayPauseView playBtn;
    private View prevBtn;
    private boolean isRestart = true;
    private TextView songName;
    private TextView songAuthor;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.player_fragment, container, false);
        songs = new ArrayList<>(0);
        pager = view.findViewById(R.id.imagesPager);
        recyclerView = (RecyclerView) pager.getChildAt(0);
        image = view.findViewById(R.id.player_background);
        durationView = view.findViewById(R.id.duration);
        durationView.setText("0:00");
        nextBtn = view.findViewById(R.id.next_but);
        playBtn = view.findViewById(R.id.playPause);
        prevBtn = view.findViewById(R.id.prev_but);
        songName = view.findViewById(R.id.song_name);
        songAuthor = view.findViewById(R.id.song_author);

        songAuthor.setSelected(true);
        songName.setSelected(true);

        pager.setAdapter(pagerAdapter = new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                FrameLayout frameLayout = new FrameLayout(view.getContext());
                frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                CardView card = new CardView(view.getContext());
                card.setRadius(50f);
                card.setCardElevation(2f);
                card.setLayoutParams(new FrameLayout.LayoutParams(Utils.px(view.getContext(), 290), Utils.px(view.getContext(), 290), Gravity.CENTER));
                frameLayout.addView(card);

                return new ContainerHolder(frameLayout, card);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                FrameLayout frameLayout = (FrameLayout) holder.itemView;
                CardView card = ((ContainerHolder) holder).getCard();

                Song song = songs.get(position);
                Glide.with(PlayerFragment.this).load((song.getImage()))
                        .placeholder(R.drawable.photo).into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                card.setForeground(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                card.setForeground(placeholder);
                            }
                        });

                card.setOnClickListener(v -> {
                    onPlay(v, true);
                    PlayerController.getInstance().changePlaying();
                });
            }

            @Override
            public void onViewAttachedToWindow(RecyclerView.ViewHolder holder){
                CardView card = ((ContainerHolder)holder).getCard();
                if(PlayerController.getInstance().getPlaying()){
                    card.setScaleX(1f);
                    card.setScaleY(1f);
                }else{
                    card.setScaleX(0.6f);
                    card.setScaleY(0.6f);
                }
            }

            @Override
            public int getItemCount() {
                return songs.size();
            }
        });


        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(
                    int position,
                    float positionOffset,
                    @Px int positionOffsetPixels){

                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.i("PLAYER", "pageSELECTED");
                if(image != null){
                    setBackground(songs.get(position), image);
                }
                Song song = songs.get(position);
                curPos = position;
                durationView.setText(Utils.duration(song.getDuration()));
                seekBar.setProgress(0);
                seekBar.setMax((int)song.getDuration()/1000);
                songName.setText(song.getName());
                songAuthor.setText(song.getAuthor());
                timer.reset();
                if(PlayerController.getInstance().getPlaying()){
                    timer.startTimer();
                }
                NotificationCenter.getInstance().postNotification(NotificationCenter.songChanged, song);

            }
        });

        seekBar = view.findViewById(R.id.seekbar);
        timer = view.findViewById(R.id.timer);
        timer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                timer.onTick();
                seekBar.setProgress(timer.getLastTime() + timer.getCurTime(), true);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    timer.setLastTime(progress);
                    timer.setText(Utils.duration((long)progress * 1000));
                }
                if(progress == seekBar.getMax()){
                    timer.reset();
                    seekBar.setProgress(0, false);
                    if(PlayerController.getInstance().getPlaying()){
                        NotificationCenter.getInstance().postNotification(NotificationCenter.play);
                        timer.startTimer();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(timer.isStarted()){
                    timer.stopTimer();
                }
                if(PlayerController.getInstance().getPlaying()){
                    NotificationCenter.getInstance().postNotification(NotificationCenter.play);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                long progress = seekBar.getProgress();
                Song curSong = songs.get(pager.getCurrentItem());
                NotificationCenter.getInstance().postNotification(NotificationCenter.seekSong, curSong, progress * 1000);
                if(PlayerController.getInstance().getPlaying()){
                    PlayerController.getInstance().setPlaying(false);
                    NotificationCenter.getInstance().postNotification(NotificationCenter.play);
                    PlayerController.getInstance().setPlaying(true);
                    timer.startTimer();
                }
            }
        });

        playBtn.setOnClickListener((v)->{
            if(curPos != -1){
                curCard = ((ContainerHolder)recyclerView.findViewHolderForAdapterPosition(curPos)).getCard();
            }
            if(curCard != null){
                onPlay(curCard, true);
                PlayerController.getInstance().changePlaying();
            }
        });


        playBtn.setOnTouchListener(this);
        nextBtn.setOnTouchListener(this);
        prevBtn.setOnTouchListener(this);

        nextBtn.setOnClickListener((v)->{
            int size = songs.size();
            if(size > 0){
                if(curPos < (size-1)){
                    pager.setCurrentItem(curPos + 1);
                } else{
                   pager.setCurrentItem(0);
                }
            }
        });
        prevBtn.setOnClickListener((v)->{
            int size = songs.size();
            if(size > 0){
                if(curPos > 0){
                    pager.setCurrentItem(curPos-1);
                }else{
                    pager.setCurrentItem(size-1);
                }
            }
        });

        View arrowBtn = view.findViewById(R.id.arrowBtn);
        arrowBtn.setOnTouchListener(this);
        arrowBtn.setOnClickListener(v -> NotificationCenter.getInstance().postNotification(NotificationCenter.arrowBtnClick));

        View heartBtn = view.findViewById(R.id.heart);
        View heartFillBtn = view.findViewById(R.id.heart_fill);
        FrameLayout heartContainer = view.findViewById(R.id.heartContainer);
        heartFillBtn.setVisibility(View.INVISIBLE);
        heartContainer.setOnTouchListener(this);
        LottieAnimationView heartLottie = view.findViewById(R.id.heartAnim);
        heartLottie.setVisibility(View.INVISIBLE);
        ValueAnimator animator
                = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(animation -> {
                    heartLottie.setProgress((float)animation.getAnimatedValue());
                });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation){
                super.onAnimationStart(animation);
                heartLottie.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                heartLottie.setProgress(0f);
                heartLottie.setVisibility(View.INVISIBLE);
            }
        });

        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(2000);

        heartContainer.setOnClickListener((v) -> {
            if(heartBtn.getVisibility() == View.VISIBLE){
                heartFillBtn.setVisibility(View.VISIBLE);
                heartBtn.setVisibility(View.INVISIBLE);
                if(heartLottie.getProgress() == 0f){
                    animator.start();
                }
            }else{
                heartBtn.setVisibility(View.VISIBLE);
                heartFillBtn.setVisibility(View.INVISIBLE);
            }
        });




        NotificationCenter.getInstance().addObserver(NotificationCenter.clickedSong, this);
        NotificationCenter.getInstance().addObserver(NotificationCenter.imageLoaded, this);
        NotificationCenter.getInstance().addObserver(NotificationCenter.songsLoaded, this);
        NotificationCenter.getInstance().addObserver(NotificationCenter.mini_play, this);
        NotificationCenter.getInstance().addObserver(NotificationCenter.serviceStarted, this);
        NotificationCenter.getInstance().addObserver(NotificationCenter.repeatClick, this);
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        NotificationCenter.getInstance().removeObserver(NotificationCenter.clickedSong, this);
        NotificationCenter.getInstance().removeObserver(NotificationCenter.imageLoaded, this);
        NotificationCenter.getInstance().removeObserver(NotificationCenter.songsLoaded, this);
        NotificationCenter.getInstance().removeObserver(NotificationCenter.mini_play, this);
        NotificationCenter.getInstance().removeObserver(NotificationCenter.serviceStarted, this);
        NotificationCenter.getInstance().removeObserver(NotificationCenter.repeatClick, this);
    }

    @Override
    public void didReceivedNotification(int id, Object... args){
        if(id == NotificationCenter.clickedSong && args[0] != null){
            View view = getView();
            if(view != null){
                Log.i("PLAYER", "songClicked");
                boolean isPlaying = PlayerController.getInstance().getPlaying();
                if(!isPlaying){
                    playBtn.setState(PlayPauseView.STATE_PLAY);
                }
                pager.setCurrentItem((int)args[1]);
                if(!timer.isStarted()){
                    timer.startTimer();
                }
            }
        }else if(id == NotificationCenter.songsLoaded){
            setSongs((ArrayList<Song>) args[0]);
            pagerAdapter.notifyDataSetChanged();
        }else if(id == NotificationCenter.mini_play || id == NotificationCenter.repeatClick){
            if(curPos != -1){
                curCard = ((ContainerHolder)recyclerView.findViewHolderForAdapterPosition(curPos)).getCard();
                onPlay(curCard, false);
            }
        }else if(id == NotificationCenter.serviceStarted){
        }
    }

    public void setBackground(Song song, ImageView image){
        Glide.with(this).asBitmap().load(song.getImage())
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 10)))
                .into(image);
    }

    class ContainerHolder extends RecyclerView.ViewHolder{
        private FrameLayout frame;
        private CardView card;
        public ContainerHolder(FrameLayout frame, CardView card){
            super(frame);
            this.frame = frame;
            this.card = card;
        }

        public CardView getCard(){
            return card;
        }
    }

    public void setSongs(ArrayList<Song> songs){
       this.songs = songs;
    }

    public void startAnimation(View v, boolean isPlaying){
        if(isPlaying){
            ValueAnimator animator = ValueAnimator.ofFloat(1f, 0.6f);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                    float t = (float)animation.getAnimatedValue();
                    v.setScaleX(t);
                    v.setScaleY(t);
                }
            });
            animator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
            animator.setDuration(200);
            animator.start();
            return;
        }
        ValueAnimator animator = ValueAnimator.ofFloat(0.6f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                float t = (float)animation.getAnimatedValue();
                v.setScaleX(t);
                v.setScaleY(t);
            }
        });
        animator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        animator.setDuration(200);
        animator.start();
    }

    public void setPlaying(boolean playing){
        isPlaying = playing;
    }

    public void onPlay(View v, boolean anim){
        boolean isPlaying = PlayerController.getInstance().getPlaying();
        if(anim){
            if (timer.isStarted()) {
                timer.stopTimer();
            } else {
                timer.startTimer();
            }

            startAnimation(v, isPlaying);
            if(isPlaying){
                playBtn.setState(PlayPauseView.STATE_PAUSE);
            }else{
                playBtn.setState(PlayPauseView.STATE_PLAY);
            }
            if(isRestart){
                isRestart = false;
                Song song = songs.get(pager.getCurrentItem());
                NotificationCenter.getInstance().postNotification(NotificationCenter.songChanged, song);
            }
            NotificationCenter.getInstance().postNotification(NotificationCenter.play);
        }
        else{
            if(isRestart){
                isRestart = false;
                Song song = songs.get(pager.getCurrentItem());
                NotificationCenter.getInstance().postNotification(NotificationCenter.songChanged, song);
            }

            if (isPlaying) {
                timer.stopTimer();
                    v.setScaleX(0.6f);
                    v.setScaleY(0.6f);
                    playBtn.setState(PlayPauseView.STATE_PAUSE);
                    Log.i("Player", "scale0.6");
            } else {
                timer.startTimer();
                v.setScaleX(1f);
                v.setScaleY(1f);
                playBtn.setState(PlayPauseView.STATE_PLAY);
                Log.i("Player", "scale1");
            }
        }
    }

    public void onTouchDown(View v, float from, float to){
           ValueAnimator animator = ValueAnimator.ofFloat(from, to);
           animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
               @Override
               public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                   float t = (float)animation.getAnimatedValue();
                   v.setScaleX(t);
                   v.setScaleY(t);
               }
           });
           animator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
           animator.setDuration(250);
           animator.start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(v, 1f, 0.7f);
                break; // if you want to handle the touch event
            case MotionEvent.ACTION_UP:
                onTouchDown(v, 0.7f, 1f);
                break; // if you want to handle the touch event
        }
        return false;
    }
}
