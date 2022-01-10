package com.chiron.playpalyer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.chiron.playpalyer.media.DecodeState;
import com.chiron.playpalyer.media.decoder.AudioDecoder;
import com.chiron.playpalyer.media.decoder.VideoDecoder;
import com.chiron.playpalyer.media.muxer.MP4Repack;
import com.chiron.playpalyer.media.player.MediaPlayer;
import com.chiron.playpalyer.permission.Permission;
import com.chiron.playpalyer.widget.PlayerTextureView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaActivity extends AppCompatActivity implements View.OnClickListener{
    private ExecutorService pool = Executors.newFixedThreadPool(10);
    private VideoDecoder videoDecoder = null;
    private AudioDecoder audioDecoder = null;
    private String fileName = "mvtest.mp4";
    private PlayerTextureView mPlayerTextureView = null;
    private SurfaceView mSurfaceView = null;
    private ImageButton playBtn = null;
    private ImageButton replayBtn = null;
    private MediaPlayer mediaPlayer = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        mPlayerTextureView = (PlayerTextureView)findViewById(R.id.tv_player);
        mPlayerTextureView.setAspectRatio(640/480.f);
        mSurfaceView = findViewById(R.id.sv_player);
        playBtn=findViewById(R.id.play_button);
        replayBtn = findViewById(R.id.replay_btn);
        playBtn.setOnClickListener(this);
        replayBtn.setOnClickListener(this);
        initPlayer(fileName,mSurfaceView,null);
    }

    private void initPlayer(String fileName, SurfaceView surfaceView, Surface surface){
        final File dir = this.getFilesDir();
        if(!dir.exists()){
            dir.mkdir();
        }
        final File path = new File(dir,fileName);
        try {
            prepareSampleMovies(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.prepare(path.getAbsolutePath(),surfaceView,surface);
    }

    private void clickRepack(View view){
        repack();
    }

    private void repack(){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/test.mp4";
        new MP4Repack(path).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play_button:
                DecodeState state = mediaPlayer.getState();
                if (state != DecodeState.DECODING) {
                    playBtn.setSelected(true);
                    if (state == DecodeState.STOP) {
                        initPlayer(fileName,mSurfaceView,null);
                    }
                    mediaPlayer.start();
                } else {
                    playBtn.setSelected(false);
                    mediaPlayer.pause();
                }
                break;
            case R.id.replay_btn:
                mediaPlayer.stop();
                playBtn.setSelected(false);
                break;
            default:
                break;
        }
    }

    //媒体文件拷贝到本地
    private void prepareSampleMovies(File path) throws IOException {
        if(!path.exists()){
            final BufferedInputStream in = new BufferedInputStream(this.getResources().openRawResource(R.raw.mvtest));
            final BufferedOutputStream out = new BufferedOutputStream(this.openFileOutput(path.getName(), Context.MODE_PRIVATE));
            byte[] buf = new byte[8192];
            int size = in.read(buf);
            while(size>0){
                out.write(buf,0,size);
                size = in.read(buf);
            }
            in.close();
            out.flush();
            out.close();
        }
    }
}