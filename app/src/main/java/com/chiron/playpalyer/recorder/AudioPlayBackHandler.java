package com.chiron.playpalyer.recorder;

import android.media.MediaDataSource;
import android.media.MediaPlayer;

import java.io.IOException;

public class AudioPlayBackHandler {
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private boolean isPlaying = false;
    public void startPlay(String filePath){
        try {
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isPlaying = true;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setOnCompletionListener(mp->{
            mp.stop();
            mp.reset();
            mp.release();
            isPlaying = false;
        });
    }

    public void startPlay(MediaDataSource source){
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(source);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setOnCompletionListener(mp->{
            mp.stop();
            mp.reset();
            mp.release();
        });
    }

    public void stopPlay(){
        if(mMediaPlayer!=null && isPlaying){
            mMediaPlayer.stop();
            mMediaPlayer.reset();
//            mMediaPlayer.release();
//            mMediaPlayer = null;
        }
    }
}
