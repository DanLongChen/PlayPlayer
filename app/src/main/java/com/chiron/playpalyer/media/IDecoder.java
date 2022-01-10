package com.chiron.playpalyer.media;

import android.media.MediaFormat;

/**
 * 解码器接口
 */
public interface IDecoder{
    /**
     * 开始解码
     */
    void start();
    /**
     * 暂停解码
     */
    void pause();

    /**
     * 继续解码
     */
    void resume();

    /**
     * 停止解码
     */
    void stop();

    /**
     * 跳到指定位置
     * @param pos 跳到指定位置（帧的时间）
     * @return 返回实际帧的时间
     */
    long seekTo(long pos);

    /**
     * 跳到指定位置并且播放
     * @param pos 跳到指定位置（帧的时间）
     * @return 返回实际帧的时间
     */
    long seekAndPlay(long pos);

    /**
     * 是否正在解码
     * @return
     */
    boolean isDecoding();

    /**
     * 是否正在快进
     * @return
     */
    boolean isSeeking();

    /**
     * 是否已经停止解码
     * @return
     */
    boolean isStop();

    /**
     * 设置进度/状态监听
     * @param listener
     */
    void setProgressListener(IDecoderProgress listener);

    void setStateListener(IDecoderStateListener listener);

    /**
     * 获取宽度
     * @return
     */
    int getWidth();

    /**
     * 获取高度
     * @return
     */
    int getHeight();

    /**
     * 获取播放时长
     * @return
     */
    long getDuration();

    /**
     * 获取当前视频播放的时间戳
     * @return
     */
    long getCurTimeStamp();

    /**
     * 获取当前视频的旋转角度
     * @return
     */
    int getRotationAngle();

    /**
     * 获取视频的格式参数
     * @return
     */
    MediaFormat getMediaFormat();

    /**
     * 获取音视频对应的媒体轨道
     * @return
     */
    int getTrack();

    /**
     * 获取解码的文件路径
     * @return
     */
    String getFilePath();

    /**
     * 设置为无需音视频同步
     * @return
     */
    IDecoder withoutSync();
}
