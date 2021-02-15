package com.chiron.playpalyer.mediac

import android.media.MediaFormat

/**
 * 这里使用同步解码，需要不断压入和拉取数据是个比较耗时的操作，
 * 最后会被放入到线程池执行
 */
interface IDecoder:Runnable {
    /**
     * 暂停解码
     */
    fun pause()

    /**
     * 继续解码
     */
    fun resume()

    /**
     * 停止解码
     */
    fun stop()

    /**
     * 跳转到指定的位置，返回实际帧的时间（毫秒）
     */
    fun seetTo(pos:Long):Long

    /**
     * 跳转到指定位置并且播放，返回实际帧的时间（毫秒）
     */
    fun seekAndPlay(pos:Long):Long

    /**
     * 是否正在解码
     */
    fun isDecoding():Boolean

    /**
     * 是否正在快进
     */
    fun isSeeking():Boolean

    /**
     * 是否停止解码
     */
    fun isStop():Boolean

    /**
     * 设置尺寸监听
     */
    fun setSizeListener(l:IDecoderProgress)

    /**
     * 设置状态监听
     */
    fun setStateListener(l:IDecoderStateListener?)

    /**
     * 获取视频宽度
     */
    fun getWidth():Int

    /**
     * 获取视频高度
     */
    fun getHeight():Int

    /**
     * 获取视频长度（播放时长）
     */
    fun getDuration():Long

    /**
     * 获取当前视频播放的时间戳
     */
    fun getCurTimeStamp():Long

    /**
     * 获取视频的旋转角度
     */
    fun getRotationAngle():Int

    /**
     * 获取视频格式参数
     */
    fun getMediaFormat():MediaFormat?

    /**
     * 获取音视频对应的媒体轨道
     */
    fun getTrack():Int

    /**
     * 获取解码的文件路径
     */
    fun getFilePath():String

    /**
     * 无需音视频同步
     */
    fun withoutStnc():IDecoder
}