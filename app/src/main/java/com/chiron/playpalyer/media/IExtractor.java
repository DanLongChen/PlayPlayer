package com.chiron.playpalyer.media;

import android.media.MediaFormat;

import java.nio.ByteBuffer;

/**
 * 音视频分离器定义(音视频数据读取器)
 * 用于将数据喂给MediaC的输入缓冲
 */
public interface IExtractor {

    MediaFormat getFormat();

    void getFormat(MediaFormatCallback callback);
    /**
     * 读取视频数据
     */
    int readBuffer(ByteBuffer byteBuffer);


    void setTrack(int track);


    /**
     * seek到指定的位置并且返回实际帧的时间戳
     */
    long seek(long pos);

    /**
     * 停止读取数据
     */
    void stop();

    long getCurrentTimeStamp();
}
