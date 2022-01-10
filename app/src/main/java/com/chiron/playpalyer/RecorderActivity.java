package com.chiron.playpalyer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.chiron.playpalyer.recorder.MediaRecorderHandler;

public class RecorderActivity extends AppCompatActivity implements View.OnClickListener{
    private MediaRecorderHandler mediaRecorderHandler = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
        initUI();
        initRecorder();
    }

    private void initRecorder(){
        mediaRecorderHandler = new MediaRecorderHandler();
    }

    private void initUI(){
        findViewById(R.id.btn_media_recorder_start_record).setOnClickListener(this);
        findViewById(R.id.btn_media_recorder_stop_record).setOnClickListener(this);
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
        }
    }
}