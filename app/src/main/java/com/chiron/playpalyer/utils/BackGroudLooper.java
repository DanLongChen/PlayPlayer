package com.chiron.playpalyer.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class BackGroudLooper {

    private HandlerThread handlerThread;
    private Handler handler;

    private BackGroudLooper(){
        handlerThread = new HandlerThread("bg_thread");
        handlerThread.start();
        handler=new Handler(handlerThread.getLooper());
    }

    private static class LazyHolder{
        private static BackGroudLooper instance = new BackGroudLooper();
    }

    public static BackGroudLooper getInstance(){
        return LazyHolder.instance;
    }

    public static Looper getLooper(){
        return BackGroudLooper.getInstance().handlerThread.getLooper();
    }

    public static void post(Runnable runnable){
        BackGroudLooper.getInstance().handler.post(runnable);
    }

    public static void postDelay(Runnable runnable,long delayTime){
        BackGroudLooper.getInstance().handler.postDelayed(runnable,delayTime);
    }

    public static void remove(Runnable runnable){
        BackGroudLooper.getInstance().handler.removeCallbacks(runnable);
    }

}
