package com.chiron.playpalyer.mediac

/**
 * 解码进度
 */
interface IDecoderProgress {
    /**
     * 视频宽高变化时的回调
     */
    fun videoSizeChange(width: Int, height: Int, rotationAngle: Int)

    /**
     * 视频进度变化时的回调
     */
    fun videoProgressChange(pos: Long)
}