package com.example.muzon;

import android.util.Log;

public class PlayerController {

    private boolean isPlaying = false;
    private static volatile PlayerController Instance;

    public static PlayerController getInstance(){
        PlayerController controller = Instance;
        if (controller == null){
            synchronized (PlayerController.class){
                controller = Instance;
                if (controller == null){
                    controller = Instance = new PlayerController();
                }
            }
        }
        return controller;
    }

    public void setPlaying(boolean playing){
        isPlaying = playing;
        Log.i("Controller", "SET");
    }

    public boolean getPlaying(){
        Log.i("Controller", "GET");
        return isPlaying;
    }

    public void changePlaying(){
        Log.i("Controller", "CHANGED");
        isPlaying = !isPlaying;
    }
}
