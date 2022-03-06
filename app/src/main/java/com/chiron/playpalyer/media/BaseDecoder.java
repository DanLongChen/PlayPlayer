package com.chiron.playpalyer.media;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class BaseDecoder implements IDecoder{
    private static final String TAG = "BaseDecoder";
    private final String mFilePath;
    private boolean mIsRunning = false;
    private Object mLock = new Object();//线程锁
    private boolean mReadyForDecode = false;

    private MediaCodec mCodec=null;
    private IExtractor mExtractor=null;

    //解码输入、输出缓冲区
    private ByteBuffer[] mInputBuffers = null;
    private ByteBuffer[] mOutputBuffers = null;

    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private DecodeState mState = DecodeState.STOP;
    protected IDecoderStateListener mStateListener = null;

    private boolean mIsEos = false;//流是否结束
    protected int mVideoWidth = 0;//视频宽高
    protected int mVideoHeight = 0;
    private long mDuration = 0;//视频持续，开始，结束时间
    private long mStartPos = 0;
    private long mEndPos = 0;

    private long mStartTimeForSync = -1L;//开始同步的时间（用于音视频同步）
    private boolean mSyncRender = true;//是否需要音视频同步

    public BaseDecoder(String filePath){
        mFilePath = filePath;
    }

    protected abstract boolean check();//子类自己特殊的检查项
    protected abstract IExtractor initExtractor(String path);
    protected abstract boolean initRender();
    protected abstract void initSpaceParams(MediaFormat format);
    protected abstract boolean configDecodec(MediaCodec codec,MediaFormat format);
    protected abstract void render(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo);
    protected abstract void doneDecode();

    @Override
    public void start() {
        //解码流程定义

        if (mState == DecodeState.STOP) {
            mState = DecodeState.START;
        }

        if (mStateListener != null) {
            mStateListener.decoderPrepare(this);
        }
        Log.e("Daniel","here");
        //初始化，并且启动解码器
        if (!init()) {
            return;
        }
        mIsRunning = true;
        Log.e("Daniel","here1: "+Thread.currentThread().getName());
        while (mIsRunning) {
            Log.e("Daniel","Running: "+Thread.currentThread().getName());
            //判断状态，暂停解码，重新计算同步时间
            if (mState != DecodeState.START &&
                    mState != DecodeState.DECODING &&
                    mState != DecodeState.SEEKING) {
                Log.e("Daniel","mState is not ok: "+mState);
                waitDecode(-1);
                mStartTimeForSync = System.currentTimeMillis() - getCurTimeStamp();
            }

            //解码器没有在运行或者当前解码器状态已经为停止则结束当前循环
            if (!mIsRunning || mState == DecodeState.STOP) {
                mIsRunning = false;
                break;
            }

            if (mStartTimeForSync == -1L) {
                mStartTimeForSync = System.currentTimeMillis();
            }

            Log.e("Daniel","mIsEos: "+mIsEos);
            if (!mIsEos) {
                mIsEos = pushBufferToDecoder();
            }

            int index = pullBufferFromDecoder();
            Log.e("Daniel","index: "+index);
            if (index >= 0) {
                //音视频同步
                if (mState == DecodeState.DECODING) {
                    sleepRender();
                }

                if (mSyncRender) {
                    render(mOutputBuffers[index], mBufferInfo);
                }

                mCodec.releaseOutputBuffer(index, true);

                //刚进来的状态在这里转换
                if (mState == DecodeState.START) {
                    mState = DecodeState.PAUSE;
                }
            }

            if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                mState = DecodeState.FINISH;
                if(mStateListener!=null){
                    mStateListener.decodeFinish(this);
                }
            }
        }

        doneDecode();
        release();
    }

    private boolean init(){
        //验证路径完整性
        if(TextUtils.isEmpty(mFilePath) || !(new File(mFilePath).exists())){
            Log.e(TAG,"文件路径为空");
            if(mStateListener!=null){
                mStateListener.decodeError(this,"文件路径为空");
            }
            return false;
        }
        //检查子类参数是否完整
        Log.e("Daniel","check");
        if(!check()){
            return false;
        }
        //初始化媒体文件提取器
        mExtractor = initExtractor(mFilePath);
        if(mExtractor==null || mExtractor.getFormat()==null){
            return false;
        }
        Log.e("Daniel","mExtractor: "+Thread.currentThread().getName());

        if(!initParams()){
            return false;
        }
        Log.e("Daniel","hehe: "+Thread.currentThread().getName());

        //初始化渲染器
        if(!initRender()){
            return false;
        }
        Log.e("Daniel","initRender: "+Thread.currentThread().getName());

        //初始化解码器
        Log.e("Daniel","initDecodec: "+Thread.currentThread().getName());
        if(!initDecodec()){
            return false;
        }
        return true;
    }

    private boolean initParams(){
        try{
            MediaFormat format = mExtractor.getFormat();
            mDuration = format.getLong(MediaFormat.KEY_DURATION)/1000;
            if(mEndPos==0L){
                mEndPos=mDuration;
            }
            initSpaceParams(format);
        }catch(Exception e){
            Log.e("Daniel","getException: "+e.getMessage());
            return false;
        }
        return true;
    }

    private boolean initDecodec(){
        try {
            //根据音视频编码格式创建codec
            String type = mExtractor.getFormat().getString(MediaFormat.KEY_MIME);
            if ("raven".equals(Build.HARDWARE) && "video/avc".equals(type)) {
                Log.e("Daniel,","type: "+type);
                mCodec = MediaCodec.createByCodecName("c2.exynos.h264.decoder");
            } else {
                mCodec = MediaCodec.createDecoderByType(type);
            }
            //配置解码器，没有配置则一直等待
            if(!configDecodec(mCodec,mExtractor.getFormat())){
                Log.d("Daniel","configDecodec failed!");
                waitDecode(-1);
            }

            mCodec.start();
            Log.d("Daniel","codec start");
            mInputBuffers = mCodec.getInputBuffers();
            mOutputBuffers = mCodec.getOutputBuffers();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private boolean pushBufferToDecoder() {
        int inputBufferIndex = mCodec.dequeueInputBuffer(1000);
        boolean isEndOfStream = false;
        Log.e("Daniel","inputBufferIndex: "+inputBufferIndex);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mInputBuffers[inputBufferIndex];
            int sampleSize = mExtractor.readBuffer(inputBuffer);
            if (sampleSize < 0) {
                Log.e("Daniel","sampleSize < 0");
                mCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                isEndOfStream = true;
            } else {
                mCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, mExtractor.getCurrentTimeStamp(), 0);
            }
        }
        return isEndOfStream;
    }

    private int pullBufferFromDecoder(){
        int index = mCodec.dequeueOutputBuffer(mBufferInfo,1000);
        switch(index){
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:{

            }
            break;
            case MediaCodec.INFO_TRY_AGAIN_LATER:{

            }
            break;
            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:{
                mOutputBuffers=mCodec.getOutputBuffers();
            }
            default:
                return index;
        }
        return index;
    }

    private void sleepRender(){
        long passTime = System.currentTimeMillis()-mStartTimeForSync;
        long curTime = getCurTimeStamp();
        if(curTime>passTime){
            try {
                Thread.sleep(curTime-passTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void release(){
        mState = DecodeState.STOP;
        mIsEos=true;
        mExtractor.stop();
        mCodec.stop();
        mCodec.release();
        if(mStateListener!=null){
            mStateListener.decodeDestroy(this);
        }
    }

    protected void waitDecode(long waitTime) {
        try {
            if (mState == DecodeState.PAUSE) {
                if (mStateListener != null) {
                    mStateListener.decoderPause(this);
                }
            }
            synchronized (mLock) {
                if (waitTime > 0) {
                    mLock.wait(waitTime);
                } else {
                    mLock.wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void notifyDecode(){
        synchronized(mLock){
            mLock.notifyAll();
        }
        if(mState==DecodeState.DECODING){
            if (mStateListener != null) {
                mStateListener.decoderRunning(this);
            }
        }
    }


    @Override
    public void pause() {
        mState = DecodeState.PAUSE;
    }

    @Override
    public void resume() {
        mState = DecodeState.DECODING;
        notifyDecode();
    }

    @Override
    public void stop() {
        mState = DecodeState.STOP;
        mIsRunning = false;
        notifyDecode();//解码器停止了，可能线程还被wait等待，通知其结束
    }

    @Override
    public long seekTo(long pos) {
        return 0;
    }

    @Override
    public long seekAndPlay(long pos) {
        return 0;
    }

    @Override
    public boolean isDecoding() {
        return mState == DecodeState.DECODING;
    }

    @Override
    public boolean isSeeking() {
        return mState == DecodeState.SEEKING;
    }

    @Override
    public boolean isStop() {
        return mState == DecodeState.STOP;
    }

    @Override
    public void setProgressListener(IDecoderProgress listener) {

    }

    @Override
    public void setStateListener(IDecoderStateListener listener) {
        mStateListener = listener;
    }

    @Override
    public int getWidth() {
        return mVideoWidth;
    }

    @Override
    public int getHeight() {
        return mVideoHeight;
    }

    @Override
    public long getDuration() {
        return mDuration;
    }

    @Override
    public long getCurTimeStamp() {
        return mBufferInfo.presentationTimeUs/1000;
    }

    @Override
    public int getRotationAngle() {
        return 0;
    }

    @Override
    public MediaFormat getMediaFormat() {
        if(mExtractor!=null){
            return mExtractor.getFormat();
        }else{
            return null;
        }
    }

    @Override
    public int getTrack() {
        return 0;
    }

    @Override
    public String getFilePath() {
        return mFilePath;
    }

    @Override
    public IDecoder withoutSync() {
        mSyncRender = false;
        return this;
    }
}
