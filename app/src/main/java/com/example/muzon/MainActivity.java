package com.example.muzon;



import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;

import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.common.MediaItem;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;


public class MainActivity extends AppCompatActivity implements MotionLayout.TransitionListener, NotificationCenter.Delegate{
    static boolean granted = false;
    private static final int READ_MEDIA_AUDIO = 100;
    private static final int TYPE_SPACE = 9;
    private static final int TYPE_TEXT = 10;

    MiniPlayer miniPlayer;
    public static AudioService mService;
    boolean isBound;
    ImageView playerBackground;
    MotionLayout motionLayout;
    FrameLayout playerContainer;
    PlayerFragment playerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_AUDIO}, READ_MEDIA_AUDIO);
            }
        }

       // EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // using toolbar as ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabIndicatorAnimationMode(TabLayout.INDICATOR_ANIMATION_MODE_ELASTIC);
        ViewPager2 pager = findViewById(R.id.pager);
        pager.setOffscreenPageLimit(3);
        FragmentStateAdapter pageAdapter = new FragmentAdapter(this);
        pager.setAdapter(pageAdapter);

        TabLayoutMediator tabLayoutMediator= new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy(){

            @Override
            public void onConfigureTab(TabLayout.Tab tab, int position) {
                tab.setText(position == 0 ? "Избранное" : position == 1 ? "Песни" : "Плейлисты");
            }
        });
        tabLayoutMediator.attach();



        motionLayout = findViewById(R.id.motionLayout1);
        motionLayout.setTransitionListener(this);
        playerContainer = motionLayout.findViewById(R.id.player_container);
        miniPlayer = new MiniPlayer();
        playerFragment = new PlayerFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, miniPlayer).commit();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_container, playerFragment).commit();

        NotificationCenter.getInstance().addObserver(NotificationCenter.clickedSong, this);
        NotificationCenter.getInstance().addObserver(NotificationCenter.arrowBtnClick, this);
    }

    @Override
    protected void onStart(){
        startService();
        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        if (requestCode == READ_MEDIA_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                granted = true;
                Toast.makeText(MainActivity.this, "Access to music granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Access to music denied", Toast.LENGTH_SHORT).show();
            }

        }
    }

        @Override
    protected void onResume(){
        super.onResume();

    }

    @Override
    protected void onStop(){
        super.onStop();
        if(isBound){
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    private void startService(){
        Intent serviceIntent = new Intent(this, AudioService.class);
        startService(serviceIntent);
      bindService();
    }

    private void bindService(){
        Intent intent = new Intent(this, AudioService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.MyBinder binder = (AudioService.MyBinder) service;
            mService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            isBound = false;
        }
    };

    @Override
    protected void onDestroy(){
        super.onDestroy();
        NotificationCenter.getInstance().removeObserver(NotificationCenter.clickedSong, this);
        NotificationCenter.getInstance().removeObserver(NotificationCenter.arrowBtnClick, this);
    }

    @Override
    public void onTransitionChange( MotionLayout motionLayout,
                                    int startId,
                                    int endId,
                                    float progress){

            miniPlayer.getView().setAlpha(1f - 3 * progress);
        }



    @Override
    public void onTransitionStarted(MotionLayout motionLayout, int startId, int endId){
        if(startId == R.id.endPosition){
            miniPlayer.getView().setClickable(true);
        }
    }

    @Override
    public void onTransitionTrigger(MotionLayout motionLayout,
                                    int triggerId,
                                    boolean positive,
                                    float progress){}

    @Override
    public void onTransitionCompleted(MotionLayout motionLayout, int currentId){
            NotificationCenter.getInstance().postNotification(NotificationCenter.sceneChanged, currentId);
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
            if(id == NotificationCenter.arrowBtnClick){
                motionLayout.transitionToStart();
            }
        }

    public int getDominantColor(Bitmap bitmap){
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }
}