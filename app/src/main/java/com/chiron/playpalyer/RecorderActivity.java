package com.chiron.playpalyer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.chiron.playpalyer.recorder.AACEncoder;
import com.chiron.playpalyer.recorder.AudioRecorderHandler;
import com.chiron.playpalyer.recorder.MediaRecorderHandler;
import com.chiron.playpalyer.recorder.interfaces.SourceDataCallback;

public class RecorderActivity extends AppCompatActivity implements View.OnClickListener{
    private MediaRecorderHandler mediaRecorderHandler = null;
    private AudioRecorderHandler audioRecorderHandler = null;
    private AACEncoder aacEncoder = null;

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
        mediaRecorderHandler = new MediaRecorderHandler();
        audioRecorderHandler = AudioRecorderHandler.getInstance();
        audioRecorderHandler.init();
        aacEncoder = new AACEncoder();
        aacEncoder.config(audioRecorderHandler.getAudioConfig());

        audioRecorderHandler.setDataCallBack(new SourceDataCallback() {
            @Override
            public void onAudioSourceDataCallback(byte[] data, int index) {
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
        findViewById(R.id.btn_audio_recorder_stop_record).setOnClickListener(this);
        findViewById(R.id.btn_audio_recorder_stop_record).setOnClickListener(this);

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
            default:
                break;
        }
    }

    private void startAudioRecord(){
        audioRecorderHandler.start();
        aacEncoder.start();
    }

    private void stopAudioRecord(){
        audioRecorderHandler.stop();
        aacEncoder.release();
    }
}