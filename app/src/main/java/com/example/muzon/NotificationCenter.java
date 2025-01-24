package com.example.muzon;

import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;

public class NotificationCenter {

    private static int eventNum = 1;

    public static final int play = eventNum++;
    public static final int clickedSong = eventNum++;
    public static final int imageLoaded = eventNum++;
    public static final int songsLoaded = eventNum++;
    public static final int songChanged = eventNum++;
    public static final int mini_play = eventNum++;
    public static final int seekSong = eventNum++;
    public static final int serviceStarted = eventNum++;
    public static final int setSong = eventNum++;
    public static final int sceneChanged = eventNum++;
    public static final int repeatClick = eventNum++;
    public static final int arrowBtnClick = eventNum++;

    private SparseArray<ArrayList<Delegate>> observers = new SparseArray<>();

    private static volatile NotificationCenter Instance;

    public static NotificationCenter getInstance(){
       NotificationCenter center = Instance;
       if (center == null){
           synchronized (NotificationCenter.class){
               center = Instance;
               if (center == null){
                   center = Instance = new NotificationCenter();
               }
           }
       }
       return center;
    }

    public interface Delegate{
        void didReceivedNotification(int id, Object... args);
    }

    public void postNotification(int id, Object... args){
        ArrayList<Delegate> listeners = observers.get(id);
        if(listeners == null || listeners.isEmpty()){
            return;
        }
        for(int a = 0; a < listeners.size(); a++){
            Delegate observer = listeners.get(a);
            observer.didReceivedNotification(id, args);
        }
    }

    public void addObserver(int id, Delegate observer){
        ArrayList<Delegate> listeners = observers.get(id);
        if(listeners == null){
            observers.put(id, listeners = new ArrayList<>());
        }
        if(listeners.contains(observer)){
            return;
        }
        listeners.add(observer);
    }

    public void removeObserver(int id, Delegate observer){
        ArrayList<Delegate> listeners = observers.get(id);
        if(listeners != null){
            listeners.remove(observer);
        }
    }
}
