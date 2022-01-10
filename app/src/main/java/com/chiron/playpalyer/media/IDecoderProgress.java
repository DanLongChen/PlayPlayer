package com.chiron.playpalyer.media;

/**
 * 解码进度
 */
interface IDecoderProgress {
    /**
     * 视频宽高变化时的回调
     */
    void videoSizeChange(int width,int height, int rotationAngle);

    /**
     * 视频进度变化时的回调
     */
    void videoProgressChange(long pos);
}
