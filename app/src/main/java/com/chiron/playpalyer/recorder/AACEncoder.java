package com.chiron.playpalyer.recorder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.chiron.playpalyer.recorder.interfaces.EncodedDataCallback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

//将PCM数据通过MediaCodec编码为AAC格式
public class AACEncoder {
    private ArrayBlockingQueue<byte[]> mQueue;
    private int mAudioChannelCount = -1;
    private int mAudioSampleRate = -1;
    private int mAudioBufferSize = -1;


    private MediaCodec mAudioEncoder = null;
    private long presentationTimeUs = -1L;
    private boolean isEncoding = false;
    private ByteBuffer[] encoderInputBuffers;
    private ByteBuffer[] encoderOutputBuffers;
    private MediaCodec.BufferInfo mAudioEncodeBufferInfo;

    private Thread mEncodeThread;
    private EncodedDataCallback mEncodedDataCallback;

    private static final int MAX_INPUT_SIZE = 8192;

    public void config(AudioConfig audioConfig){
        mQueue = new ArrayBlockingQueue<>(20);
        if (audioConfig != null) {
            mAudioChannelCount = audioConfig.getAudioChannelCount();
            mAudioSampleRate = audioConfig.getAudioSampleRate();
            mAudioBufferSize = audioConfig.getBufferSize();
        }else{
            mAudioChannelCount = 2;
            mAudioSampleRate = 16000;
            mAudioBufferSize = 8192;
        }
        initAudioEncoder();
    }

    //在这里进行初始化
    private void initAudioEncoder(){
        try {
            mAudioEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                    mAudioSampleRate,mAudioChannelCount);
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,MAX_INPUT_SIZE);
            format.setInteger(MediaFormat.KEY_BIT_RATE,1000*16);
            format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
            mAudioEncoder.configure(format,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        presentationTimeUs = System.currentTimeMillis();
        isEncoding = true;
        if(mAudioEncoder!=null){
            mAudioEncoder.start();
            encoderInputBuffers = mAudioEncoder.getInputBuffers();
            encoderOutputBuffers=mAudioEncoder.getOutputBuffers();
            mAudioEncodeBufferInfo = new MediaCodec.BufferInfo();
            mEncodeThread = new Thread(new EncodeRunnable());
            mEncodeThread.start();
        }
    }

    public void release(){
        if(mAudioEncoder!=null){
            mAudioEncoder.stop();
            mAudioEncoder.release();
            mAudioEncoder=null;
        }
    }

    public void putData(byte[] pcmData){
        mQueue.offer(pcmData);
    }

    public void setEncodedDataCallback(EncodedDataCallback callback) {
        this.mEncodedDataCallback = callback;
    }

    //在循环中一直
    private class EncodeRunnable implements Runnable{
        @Override
        public void run() {
            while(isEncoding || !mQueue.isEmpty()){
                encodePCM();
            }
            release();
        }
    }

    //编码PCM数据为AAC并且保存
    private void encodePCM(){
        int inputIndex;
        ByteBuffer inputBuffer;
        int outputIndex;
        ByteBuffer outputBuffer;

        byte[] chunkPCM = getPCMData();
        if(chunkPCM==null){
            return;
        }
        inputIndex = mAudioEncoder.dequeueInputBuffer(-1);
        if(inputIndex>=0){
            inputBuffer = encoderInputBuffers[inputIndex];
            inputBuffer.clear();
            inputBuffer.limit(chunkPCM.length);
            inputBuffer.put(chunkPCM);
            long pts = System.currentTimeMillis()-presentationTimeUs;
            mAudioEncoder.queueInputBuffer(inputIndex,0,chunkPCM.length,pts,0);
        }
        outputIndex = mAudioEncoder.dequeueOutputBuffer(mAudioEncodeBufferInfo, 2000);
        while (outputIndex >= 0) {
            outputBuffer = encoderOutputBuffers[outputIndex];
            if (mEncodedDataCallback != null) {
                mEncodedDataCallback.onAudioEncodedCallbacl(outputBuffer, mAudioEncodeBufferInfo);
            }
            mAudioEncoder.releaseOutputBuffer(outputIndex, false);
            outputIndex = mAudioEncoder.dequeueOutputBuffer(mAudioEncodeBufferInfo, 2000);
        }
    }

    private byte[] getPCMData() {
        if (mQueue.isEmpty()) {
            return null;
        }
        return mQueue.poll();
    }
}
