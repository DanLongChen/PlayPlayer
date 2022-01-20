package com.chiron.playpalyer.utils;

import android.os.Handler;
import android.os.HandlerThread;

public class BackGroudLooper {

    private HandlerThread handlerThread;
    private Handler handler;

    private static class LazyHolder{
        private static BackGroudLooper instance = new BackGroudLooper();
    }

    public static BackGroudLooper getInstance(){
        return LazyHolder.instance;
    }
}
