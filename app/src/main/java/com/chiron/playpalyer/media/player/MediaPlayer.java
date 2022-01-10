package com.chiron.playpalyer.media.player;

import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.chiron.playpalyer.media.DecodeState;
import com.chiron.playpalyer.media.decoder.AudioDecoder;
import com.chiron.playpalyer.media.decoder.VideoDecoder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//播放器控制
public class MediaPlayer {
    private VideoDecoder videoDecoder = null;
    private AudioDecoder audioDecoder = null;
    private ExecutorService pool = Executors.newFixedThreadPool(2);
    private Runnable videoRunnable = null;
    private Runnable audioRunnable = null;
    private DecodeState mState = DecodeState.STOP;
    private String mPath = null;
    private SurfaceView mSurfaceView = null;
    private Surface mSurface = null;
    public void prepare(String path, SurfaceView surfaceView, Surface surface){
        if (pool == null) {
            pool = Executors.newFixedThreadPool(2);
        }
        Log.e("Daniel","SurfaceView: "+surfaceView+"  Surface: "+surface);
        mPath = path;
        mSurfaceView = surfaceView;
        mSurface = surface;
        videoDecoder = new VideoDecoder(path,surfaceView,surface);
        audioDecoder = new AudioDecoder(path);
        videoRunnable = new Runnable() {
            @Override
            public void run() {
                videoDecoder.start();
            }
        };
        audioRunnable = new Runnable() {
            @Override
            public void run() {
                audioDecoder.start();
            }
        };
        pool.execute(videoRunnable);
        pool.execute(audioRunnable);
        mState = DecodeState.START;
    }

    public void start(){
        videoDecoder.resume();
        audioDecoder.resume();
        mState = DecodeState.DECODING;
    }

    public void stop(){
        videoDecoder.stop();
        audioDecoder.stop();
        mState = DecodeState.STOP;
        pool.shutdown();
        pool = null;
        videoDecoder = null;
        audioDecoder = null;
    }

    public void seek(int position){

    }

    public void resume(){
        videoDecoder.resume();
        audioDecoder.resume();
        mState = DecodeState.DECODING;
    }

    public void pause(){
        videoDecoder.pause();
        audioDecoder.pause();
        mState = DecodeState.PAUSE;
    }

    public void replay(){
        stop();
        prepare(mPath,mSurfaceView,mSurface);
        start();
        resume();
    }

    public DecodeState getState(){
        return mState;
    }
}
