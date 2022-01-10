package com.chiron.playpalyer.media.extractor;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.text.TextUtils;

import com.chiron.playpalyer.error.MediaError;
import com.chiron.playpalyer.media.IExtractor;
import com.chiron.playpalyer.media.MediaFormatCallback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * android原生自带一个MediaExtractor，用于音视频数据的分离和提取，这里做一下封装，相当于一个代理
 */
public class MyMediaExtractor implements IExtractor {
    private static final String[] MIME_TYPES = {"video/","audio/"};
    //Android内置的音视频分离器，用于从封装格式中分理和提取音视频数据
    private MediaExtractor mMediaExtractor;

    //记录下当前media中所有的音视频轨道
    private List<Integer> mAudioTracks = new ArrayList<>();
    private List<Integer> mVideoTracks = new ArrayList<>();

    //当前选中的音视频轨道
    private int mAudioTrack = -1;
    private int mVideoTrack = -1;

    //返回当前帧的pts（-1则没有可用帧）
    private long mCurTimeStamp = -1;

    //当前帧标志
    private int mCurSampleFlag = 0;

    //记录开始解码时间
    private long mStartPos = -1;

    private MediaType type = MediaType.UNKNOWN;

    public MyMediaExtractor(String path, MediaType type) {
        if (!TextUtils.isEmpty(path)) {
            mMediaExtractor = new MediaExtractor();
            try {
                //从当前路径下找，也可以是网络路径
                mMediaExtractor.setDataSource(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.type = type;
        }
    }

    /**
     * 获取音/视频多媒体格式(同步获取，获取单个)
     * @return
     */
    @Override
    public MediaFormat getFormat() {
        if (mMediaCheck() && mMediaExtractor.getTrackCount() > 0) {
            int temp = -1;
            for (int i = 0; i < mMediaExtractor.getTrackCount(); i++) {
                MediaFormat mediaFormat = mMediaExtractor.getTrackFormat(i);
                String mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith(type == MediaType.VIDEO ? MIME_TYPES[0] : MIME_TYPES[1])) {
                    if (type == MediaType.VIDEO) {
                        mVideoTrack = i;
                    } else {
                        mAudioTrack = i;
                    }
                    return mediaFormat;
                }
            }
            if (temp >= 0) {
                return mMediaExtractor.getTrackFormat(temp);
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取音/视频多媒体格式(异步获取，获取多个)
     * @param callback
     */
    @Override
    public void getFormat(MediaFormatCallback callback) {
        if (mMediaCheck() && mMediaExtractor.getTrackCount() > 0) {
            List<MediaFormat> result = new ArrayList<>();
            for (int i = 0; i < mMediaExtractor.getTrackCount(); i++) {
                MediaFormat mediaFormat = mMediaExtractor.getTrackFormat(i);
                String mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType.startsWith(type==MediaType.VIDEO?MIME_TYPES[0]:MIME_TYPES[1])) {
                    result.add(mMediaExtractor.getTrackFormat(i));
                }
            }
            if (result.size() > 0) {
                callback.onSuccess(result);
            } else {
                callback.onFailed(MediaError.EMPTY_MEDIA_FORMAT_ERROR);
            }
        }
    }

    /**
     * 读取音/视频帧数据，给解码器喂入数据（调用这个方法之前需要设置一下track）
     * @param byteBuffer
     * @return
     */
    @Override
    public int readBuffer(ByteBuffer byteBuffer) {
        byteBuffer.clear();
        //从offset开始读取视频编码数据到buffer(-1的话就是没有可用的帧数据)
        int readSampleCount = mMediaExtractor.readSampleData(byteBuffer, 0);
        if (readSampleCount < 0) {
            return -1;
        }

        mCurTimeStamp = mMediaExtractor.getSampleTime();
        mCurSampleFlag = mMediaExtractor.getSampleFlags();
        mMediaExtractor.advance();//前进到下一帧
        return readSampleCount;
    }

    /**
     * 设置音/视频轨道（在读取帧数据之前需要设置）
     * @param track
     */
    @Override
    public void setTrack(int track){
        if (mMediaCheck() && track>=0){
            mMediaExtractor.selectTrack(track);
        }
    }

    /**
     seek到指定位置，并且返回实际帧时间戳
     * 说明：seek(pos: Long)方法，主要用于跳播，快速将数据定位到指定的播放位置
     * 但是，由于视频中，除了I帧以外，PB帧都需要依赖其他的帧进行解码，所以，通常只能seek到I帧，
     * 但是I帧通常和指定的播放位置有一定误差，因此需要指定seek靠近哪个关键帧，有以下三种类型：
     SEEK_TO_PREVIOUS_SYNC：跳播位置的上一个关键帧
     SEEK_TO_NEXT_SYNC：跳播位置的下一个关键帧
     SEEK_TO_CLOSEST_SYNC：距离跳播位置的最近的关键帧
     * @return
     */
    @Override
    public long seek(long pos){
        if(mMediaCheck() && pos>=0){
            mMediaExtractor.seekTo(pos,MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            return mMediaExtractor.getSampleTime();
        }
        return -1;
    }

    /**
     * 停止读取数据，释放mediaExtractor
     */
    @Override
    public void stop() {
        if (mMediaCheck()) {
            mMediaExtractor.release();
            mMediaExtractor = null;
        }
    }

    @Override
    public long getCurrentTimeStamp() {
        return mCurTimeStamp;
    }

    public int getmAudioTrack() {
        return mAudioTrack;
    }

    public void setmAudioTrack(int mAudioTrack) {
        this.mAudioTrack = mAudioTrack;
    }

    public int getmVideoTrack() {
        return mVideoTrack;
    }

    public void setmVideoTrack(int mVideoTrack) {
        this.mVideoTrack = mVideoTrack;
    }

    public long getmCurTimeStamp() {
        return mCurTimeStamp;
    }

    public void setmCurTimeStamp(long mCurTimeStamp) {
        this.mCurTimeStamp = mCurTimeStamp;
    }

    public int getmCurSampleFlag() {
        return mCurSampleFlag;
    }

    public void setmCurSampleFlag(int mCurSampleFlag) {
        this.mCurSampleFlag = mCurSampleFlag;
    }

    public long getmStartPos() {
        return mStartPos;
    }

    public void setmStartPos(long mStartPos) {
        this.mStartPos = mStartPos;
    }

    private boolean mMediaCheck(){
        return mMediaExtractor!=null;
    }
}
