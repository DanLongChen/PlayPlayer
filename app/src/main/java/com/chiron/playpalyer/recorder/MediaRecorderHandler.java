package com.chiron.playpalyer.recorder;

import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MediaRecorderHandler {
    private MediaRecorder mMediaRecorder = null;
    private File mAudioPath = null;
    private File mAudioFile = null;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startRecord(String path) {
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        //设置输入源为麦克风(这里需要先申请麦克风权限)
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //设置比特率
        mMediaRecorder.setAudioEncodingBitRate(16000);
        //设置采样率
        mMediaRecorder.setAudioSamplingRate(16000);
        //设置文件输出格式
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //设置音频文件编码格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        File audioPath = new File(path);
        if(!audioPath.exists()){
            audioPath.mkdir();
        }
        mAudioPath = audioPath;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String fileName = simpleDateFormat.format(new Date(System.currentTimeMillis()))+".4a";
        Log.e("Daniel","fileName: "+fileName);
        try {
//            mAudioFile = File.createTempFile(fileName, ".4a", mAudioPath);
            mAudioFile = new File(mAudioPath+File.separator+fileName);
            mMediaRecorder.setOutputFile(mAudioFile);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //MediaRecorder使用时需要实例化，所以在不用的时候一定要及时释放掉
    public void stopRecord() {
        try {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mAudioFile = null;
        } catch (RuntimeException e) {
            //Restarts the MediaRecorder to its idle state，需要重新配置，就像新创建一般
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            if (mAudioFile != null && mAudioFile.exists()) {
                mAudioFile.delete();
            }
            mAudioFile = null;
        }
    }
}
