package com.chiron.playpalyer.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.chiron.playpalyer.recorder.interfaces.SourceDataCallback;

import kotlin.jvm.Synchronized;

//PMC数据获取
public class AudioRecorderHandler {
    private volatile static AudioRecorderHandler mInstance = null;
    private final static int[] SAMPLE_RATES = {44100,22050,16000,11025};
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    private int audioSource = MediaRecorder.AudioSource.MIC;
    private AudioRecord mAudioRecorder = null;
    private static final int MAX_BUFFER_SIZE = 8192;
    private int mBufferSize = -1;
    private byte[] mAudioBuffer = null;
    private SourceDataCallback mCallback;
    private boolean isRecording = false;
    private Thread mReadDataThread = null;

    private AudioRecorderHandler(){}

    public static AudioRecorderHandler getInstance(){
        if(mInstance==null){
            synchronized(AudioRecorderHandler.class){
                if(mInstance==null){
                    mInstance = new AudioRecorderHandler();
                }
            }
        }
        return mInstance;
    }

    //初始化
    public AudioRecord init(){
        for (int sampleRate:SAMPLE_RATES){
            //计算最小的bufferSize
            mBufferSize = 2*AudioRecord.getMinBufferSize(sampleRate,channelConfig,audioFormat);
            mAudioRecorder = new AudioRecord(audioSource,sampleRate,channelConfig,audioFormat,mBufferSize);
            if (mAudioRecorder.getState() == AudioRecord.STATE_INITIALIZED
                    && mBufferSize <= MAX_BUFFER_SIZE) {
                mAudioBuffer = new byte[Math.min(4096,mBufferSize)];
                return mAudioRecorder;
            }
        }
        return null;
    }

    //设置数据回调
    public void setDataCallBack(SourceDataCallback callBack){
        mCallback = callBack;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public AudioConfig getAudioConfig(){
        if(mAudioRecorder==null){
            return null;
        }
        if(mBufferSize<0){
            return null;
        }
        AudioConfig audioConfig = new AudioConfig();
        audioConfig.setAudioChannelCount(mAudioRecorder.getChannelCount());
        audioConfig.setAudioFormat(mAudioRecorder.getAudioFormat());
        audioConfig.setAudioSampleRate(mAudioRecorder.getSampleRate());
        audioConfig.setBufferSize(mBufferSize);
        return audioConfig;
    }

    //开始录音
    public void start(){
        if(mAudioRecorder==null){
            return;
        }
        isRecording = true;
        mAudioRecorder.startRecording();
        mReadDataThread = new ReadDataThread();
        mReadDataThread.start();
    }

    //结束录音
    public void stop(){
        isRecording = false;
        if(mAudioRecorder!=null){
            mAudioRecorder.stop();
        }
    }

    //释放资源
    public void release(){
        if(mAudioRecorder!=null){
            mAudioRecorder.release();
        }
        mReadDataThread = null;
        mAudioBuffer = null;
    }

    //开启线程，不断从AudioRecord中读取数据
    private class ReadDataThread extends Thread {
        int index = 0;

        @Override
        public void run() {
            super.run();
            while (isRecording && mAudioRecorder != null) {
                int size = mAudioRecorder.read(mAudioBuffer, 0, mAudioBuffer.length);
                if (size < 0) {
                    continue;
                }
                if (isRecording) {
                    byte[] data = new byte[size];
                    System.arraycopy(mAudioBuffer, 0, data, 0, size);
                    if (mCallback != null) {
                        mCallback.onAudioSourceDataCallback(data, index);
                    }
                }
                index++;
            }
        }
    }
}
