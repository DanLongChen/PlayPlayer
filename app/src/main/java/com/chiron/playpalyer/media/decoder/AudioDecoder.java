package com.chiron.playpalyer.media.decoder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;

import com.chiron.playpalyer.media.BaseDecoder;
import com.chiron.playpalyer.media.IExtractor;
import com.chiron.playpalyer.media.extractor.AudioExtractor;

import java.nio.ByteBuffer;

public class AudioDecoder extends BaseDecoder {
    /**采样率 HZ**/
    private int mSampleRate = -1;
    /**声音通道数量**/
    private int mChannels = -1;
    /**PCM采样位数**/
    private int mPCMEncodeBit = AudioFormat.ENCODING_PCM_16BIT;
    /**音频播放器**/
    private AudioTrack mAudioTrack = null;
    /**音频数据缓存**/
    private short[] mAudioTempBuf = null;

    public AudioDecoder(String path){
        super(path);
    }

    @Override
    protected boolean check() {
        return true;
    }

    @Override
    protected IExtractor initExtractor(String path) {
        return new AudioExtractor(path);
    }

    @Override
    protected boolean initRender() {
        //根据channel数来选择是单声道还是立体声
        int channel = mChannels==1?AudioFormat.CHANNEL_OUT_MONO: AudioFormat.CHANNEL_OUT_STEREO;
        //获取最小缓冲区
        int minBufferSize = AudioTrack.getMinBufferSize(mSampleRate,channel,mPCMEncodeBit);
        mAudioTempBuf = new short[minBufferSize/2];
        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,//播放类型
                mSampleRate,//采样率
                channel,//通道数
                mPCMEncodeBit,//采样位数
                minBufferSize,//缓冲区大小
                AudioTrack.MODE_STREAM//播放模式：1、数据流动态写入，2、一次性写入
        );
        mAudioTrack.play();
        return true;
    }

    @Override
    protected void initSpaceParams(MediaFormat format) {
        try {
            mChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            mPCMEncodeBit = format.containsKey(MediaFormat.KEY_PCM_ENCODING) ?
                    format.getInteger(MediaFormat.KEY_PCM_ENCODING) : AudioFormat.ENCODING_PCM_16BIT;
        } catch (Exception e) {

        }
    }

    @Override
    protected boolean configDecodec(MediaCodec codec, MediaFormat format) {
        //音频不需要surface，直接传入null
        codec.configure(format,null,null,0);
        return true;
    }

    @Override
    protected void render(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo) {
        //byteBuffer转到shortBuffer 长度要减半 byte->short
        if(mAudioTempBuf.length<bufferInfo.size/2){
            mAudioTempBuf = new short[bufferInfo.size/2];
        }
        outputBuffer.position(0);
        outputBuffer.asShortBuffer().get(mAudioTempBuf,0,bufferInfo.size/2);
        mAudioTrack.write(mAudioTempBuf,0,bufferInfo.size/2);
    }

    @Override
    protected void doneDecode() {
        if(mAudioTrack!=null){
            mAudioTrack.stop();
            mAudioTrack.release();
        }
    }
}
