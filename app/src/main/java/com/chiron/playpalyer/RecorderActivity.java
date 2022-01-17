package com.chiron.playpalyer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaCodec;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.chiron.playpalyer.recorder.AACEncoder;
import com.chiron.playpalyer.recorder.AudioPlayBackHandler;
import com.chiron.playpalyer.recorder.AudioRecorderHandler;
import com.chiron.playpalyer.recorder.MediaRecorderHandler;
import com.chiron.playpalyer.recorder.interfaces.EncodedDataCallback;
import com.chiron.playpalyer.recorder.interfaces.SourceDataCallback;
import com.chiron.playpalyer.utils.FileUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class RecorderActivity extends AppCompatActivity implements View.OnClickListener{
    private MediaRecorderHandler mediaRecorderHandler = null;
    private AudioRecorderHandler audioRecorderHandler = null;
    private AACEncoder aacEncoder = null;
    private BufferedOutputStream mAudioBos;
    private File savePath = null;
    private static final String TAG = RecorderActivity.class.getSimpleName();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
        initUI();
        initRecorder();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initRecorder(){
        savePath = new File(FileUtil.getMainDir(this.getExternalFilesDir(""),"RecordFile"), "record.aac");
        try {
            mAudioBos = new BufferedOutputStream(new FileOutputStream(savePath), 200 * 1024);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mediaRecorderHandler = new MediaRecorderHandler();
        audioRecorderHandler = AudioRecorderHandler.getInstance();
        audioRecorderHandler.init();
        aacEncoder = new AACEncoder();
        aacEncoder.config(audioRecorderHandler.getAudioConfig());
        aacEncoder.setEncodedDataCallback(new EncodedDataCallback() {
            @Override
            public void onAudioEncodedCallback(byte[] data, MediaCodec.BufferInfo bufferInfo) {
                Log.e(TAG,"getAudioEncodedData: "+data.length);
                try {
                    mAudioBos.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onVideoEncodedCallback(byte[] data, MediaCodec.BufferInfo bufferInfo) {

            }
        });

        audioRecorderHandler.setDataCallBack(new SourceDataCallback() {
            @Override
            public void onAudioSourceDataCallback(byte[] data, int index) {
                Log.e(TAG,"getAudioData: "+data.length);
                aacEncoder.putData(data);
            }

            @Override
            public void onVideoSourceDataCallback(byte[] data, int index) {

            }
        });
    }

    private void initUI(){
        findViewById(R.id.btn_media_recorder_start_record).setOnClickListener(this);
        findViewById(R.id.btn_media_recorder_stop_record).setOnClickListener(this);
        findViewById(R.id.btn_audio_recorder_start_record).setOnClickListener(this);
        findViewById(R.id.btn_audio_recorder_stop_record).setOnClickListener(this);
        findViewById(R.id.btn_audio_recorder_start_play).setOnClickListener(this);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_media_recorder_start_record:
                mediaRecorderHandler.startRecord(this.getFilesDir().getAbsolutePath());
                break;
            case R.id.btn_media_recorder_stop_record:
                mediaRecorderHandler.stopRecord();
                break;
            case R.id.btn_audio_recorder_start_record:
                startAudioRecord();
                break;
            case R.id.btn_audio_recorder_stop_record:
                stopAudioRecord();
                break;
            case R.id.btn_audio_recorder_start_play:
                startPlay();
                break;
            default:
                break;
        }
    }

    private void startAudioRecord(){
        Log.e(TAG,"startAudioRecord");
        audioRecorderHandler.start();
        aacEncoder.start();
    }

    private void stopAudioRecord(){
        if(mAudioBos!=null){
            try {
                mAudioBos.flush();
                mAudioBos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        audioRecorderHandler.stop();
        aacEncoder.release();
    }

    private void startPlay(){
        AudioPlayBackHandler playBackHandler = new AudioPlayBackHandler();
//        playBackHandler.startPlay(savePath.getAbsolutePath());
        playBackHandler.startPlay("/storage/emulated/0/Android/data/com.chiron.playpalyer/cache/RecordFile/2022-01-17$01:21:20-AudioCache.aac");
    }
}