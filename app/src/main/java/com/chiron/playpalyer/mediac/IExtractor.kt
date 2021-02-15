package com.chiron.playpalyer.mediac

import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * 音视频分离器定义(音视频数据读取器，TODO 内部使用定义的MMExtractor)
 * 用于将数据喂给MediaC的输入缓冲
 */
interface IExtractor {
    fun getFormat():MediaFormat?

    /**
     * 读取音视频数据
     */
    fun readBuffer(byteBuffer:ByteBuffer):Int

    /**
     * 获取当前帧的时间戳
     */
    fun getCurrentTimeStamp():Long
    fun getSampleFlag():Int

    /**
     * seek到指定的位置并且返回实际帧的时间戳
     */
    fun seek(pos:Long):Long
    fun setStartPos(pos:Long)

    /**
     * 停止读取数据
     */
    fun stop()
}