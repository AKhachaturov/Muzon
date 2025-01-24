package com.example.muzon;



import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Objects;

public class AudioService extends Service implements NotificationCenter.Delegate {

    private final IBinder mBinder = new MyBinder();
    public ExoPlayer player;
    final String BROADCAST_ACTION = "com.example.Pause";
    PauseReceiver receiver = new PauseReceiver();


    public AudioService() {
    }

    @Override
    public void onCreate(){
        super.onCreate();
        if(Build.VERSION.SDK_INT >= 26) {
            Intent notificationIntent = new Intent(this, MainActivity.class);

            PendingIntent notIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            String CHANNEL_ID = "channel_1";
            Intent intent = new Intent(BROADCAST_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "AudioChannel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("I like the wa..")
                    .setContentText("Author")
                    .setSmallIcon(R.drawable.ic_music)
                    .setContentIntent(notIntent)
                    .addAction(R.drawable.play_btn, "playButton", pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            startForeground(1, notification);
        }

       IntentFilter filter = new IntentFilter();
       filter.addAction(BROADCAST_ACTION);
       registerReceiver(receiver, filter);
        player = new ExoPlayer.Builder(this).build();

       NotificationCenter.getInstance().addObserver(NotificationCenter.play, this);
       NotificationCenter.getInstance().addObserver(NotificationCenter.songChanged, this);
       NotificationCenter.getInstance().addObserver(NotificationCenter.mini_play, this);
       NotificationCenter.getInstance().addObserver(NotificationCenter.seekSong, this);
       NotificationCenter.getInstance().addObserver(NotificationCenter.setSong, this);
       NotificationCenter.getInstance().addObserver(NotificationCenter.repeatClick, this);
       NotificationCenter.getInstance().addObserver(NotificationCenter.songsLoaded, this);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return START_STICKY;
    }

    public void pause(){
        player.setPlayWhenReady(false);
    }

    public void play(){
        player.prepare();
        player.play();
    }

    public void close(){
        stopSelf();
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

    public final class MyBinder extends Binder {
        AudioService getService(){
            return AudioService.this;
        }
    }

    public class PauseReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            boolean isPlaying = PlayerController.getInstance().getPlaying();
            if (isPlaying) {
                pause();
                PlayerController.getInstance().setPlaying(false);
            }
            else{
               play();
               PlayerController.getInstance().setPlaying(true);
            }
          }
        }

    @Override
    public void onDestroy(){
     super.onDestroy();
     unregisterReceiver(receiver);
     player.release();
     player = null;
     NotificationCenter.getInstance().removeObserver(NotificationCenter.play, this);
     NotificationCenter.getInstance().removeObserver(NotificationCenter.mini_play, this);
     NotificationCenter.getInstance().removeObserver(NotificationCenter.songChanged, this);
     NotificationCenter.getInstance().removeObserver(NotificationCenter.seekSong, this);
     NotificationCenter.getInstance().removeObserver(NotificationCenter.setSong, this);
     NotificationCenter.getInstance().removeObserver(NotificationCenter.repeatClick, this);
     NotificationCenter.getInstance().removeObserver(NotificationCenter.songsLoaded, this);
    }

    public void setCurrentSong(MediaItem song){
        player.setMediaItem(song);
        play();
    }

    @Override
    public void didReceivedNotification(int id, Object... args){
        if(id == NotificationCenter.play || id == NotificationCenter.mini_play || id == NotificationCenter.repeatClick){
            boolean isPlaying = PlayerController.getInstance().getPlaying();
            if(isPlaying){
                pause();
                Log.i("Service", "pause");
            }
            else{
                Log.i("Service", "play");
                play();
            }
        } else if(id == NotificationCenter.songChanged){
            Log.i("Service", "CHANGED");
            Song song = (Song)args[0];
            Uri uri = Uri.parse(song.getPath());
            player.setMediaItem(MediaItem.fromUri(uri));
        } else if(id == NotificationCenter.seekSong){
            Song song = (Song)args[0];
            long seekTo = (long)args[1];
            if(seekTo > song.getDuration()){
                player.seekTo(song.getDuration());
                return;
            }
            player.seekTo(seekTo);
        }
    }

}
