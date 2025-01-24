package com.example.muzon;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.Chronometer;

public class Timer extends Chronometer {

    private int curTime = 0;
    private int lastTime = 0;
    private boolean isStarted = false;

    public Timer(Context context) {
        super(context);
        setText("0:00");
    }

    public Timer(Context context,
                 AttributeSet attrs,
                 int defStyleAttr,
                 int defStyleRes){
        super(context, attrs, defStyleAttr, defStyleRes);
        setText("0:00");
    }

    public Timer(Context context,
                 AttributeSet attrs){
        super(context, attrs);
        setText("0:00");
    }

    public void startTimer(){
        long base = SystemClock.elapsedRealtime();
        setBase(base);
        start();
        isStarted = true;
    }

    public void stopTimer(){
        stop();
        isStarted = false;
        lastTime = lastTime + curTime;
        curTime = 0;
    }

    public void reset(){
        stopTimer();
        lastTime = 0;
        setText("0:00");
    }

    public void onTick(){
        long delta = SystemClock.elapsedRealtime() - getBase();
        curTime = (int)delta / 1000;
        int t = lastTime + curTime;
        int min = t / 60;
        int sec = t % 60;
        if(sec <= 9){
            setText(min + ":0" + sec);
            return;
        }
        setText(min + ":" + sec);
    }

    public int getCurTime() {
        return curTime;
    }

    public int getLastTime() {
        return lastTime;
    }

    public boolean isStarted(){
        return isStarted;
    }

    public void setLastTime(int time){
        lastTime = time;
    }
}
