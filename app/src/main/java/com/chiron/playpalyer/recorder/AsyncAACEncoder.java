package com.chiron.playpalyer.recorder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.chiron.playpalyer.recorder.interfaces.EncodedDataCallback;
import com.chiron.playpalyer.utils.ADTSUtil;
import com.chiron.playpalyer.utils.BackGroudLooper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

public class AsyncAACEncoder {
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

    private CodecHandler codecHandler = null;

    private static final int MAX_INPUT_SIZE = 8192;
    private static final int MSG_INPUT_BUFFER_AVALIABLE = 1001;


    private class CodecHandler extends Handler {
        public CodecHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case MSG_INPUT_BUFFER_AVALIABLE:{

                }
            }
        }
    }

    private MediaCodec.Callback codecCallback = new MediaCodec.Callback(){

        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {

        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

        }
    };

    public void config(AudioConfig audioConfig){
        mQueue = new ArrayBlockingQueue<>(20);
        codecHandler = new CodecHandler(BackGroudLooper.getLooper());
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
            mAudioEncoder.setCallback(codecCallback);
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

    //编码PCM数据为AAC并且通过callback传出
    private void encodePCM(){
        int inputIndex;
        ByteBuffer inputBuffer;
        int outputIndex;
        ByteBuffer outputBuffer;
        int outBitSize = -1;
        int outPackSize = -1;
        byte[] chunkAudio;

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
            //添加ADTS头部的操作
            outBitSize = mAudioEncodeBufferInfo.size;//获取到编码后数据大小
            outPackSize = outBitSize+7;
            outputBuffer = encoderOutputBuffers[outputIndex];
            outputBuffer.position(mAudioEncodeBufferInfo.offset);//定位到起始位置
            outputBuffer.limit(mAudioEncodeBufferInfo.offset + outBitSize);
            chunkAudio = new byte[outPackSize];
            ADTSUtil.addADTStoPacket(ADTSUtil.getSampleRateType(mAudioSampleRate), chunkAudio, outPackSize);
            outputBuffer.get(chunkAudio, 7, outBitSize);//将编码得到的AAC数据 取出到byte[]中 偏移量offset=7
            outputBuffer.position(mAudioEncodeBufferInfo.offset);
            if (mEncodedDataCallback != null) {
                mEncodedDataCallback.onAudioEncodedCallback(chunkAudio, mAudioEncodeBufferInfo);
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
