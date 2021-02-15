package com.chiron.playpalyer.mediac

import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * android原生自带一个MediaExtractor，用于音视频数据的分离和提取，这里做一下封装
 */
class MMExtractor(path:String?) {
    /**音视频分离器88**/
    private var mExtractor: MediaExtractor?=null

    /**音频通道索引**/
    private var mAudioTrack:Int=-1

    /**视频通道索引**/
    private var mVideoTrack:Int=-1

    /**当前帧时间戳**/
    private var mCurTimeStamp:Long = 0

    /**当前帧标志**/
    private var mCurSampleFlag: Int = 0

    /**开始解码时间点**/
    private var mStartPos:Long = 0

    init {
        //【1、在主构造中进行初始化】
        mExtractor= MediaExtractor()
        mExtractor?.setDataSource(path!!)//将source传入mExtractor
    }

    /**
     * 获取视频格式参数
     */
    fun getVideoFormat():MediaFormat?{
        //【2.1 获取视频多媒体格式】
        for(i in 0 until mExtractor!!.trackCount){//遍历源文件中的通道数
            val mediaFormat = mExtractor!!.getTrackFormat(i)//获取当前通道的格式
            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)//获取格式名
            if(mime!!.startsWith("video/")){
                mVideoTrack=i
                break
            }
        }
        return if(mVideoTrack>=0) {
            mExtractor!!.getTrackFormat(mVideoTrack)
        }
        else null
    }

    /**
     * 获取音频格式参数
     */
    fun getAudioFormat():MediaFormat?{
        //【2.2 获取音频多媒体格式】
        for(i in 0 until mExtractor!!.trackCount){
            val mediaFormat = mExtractor!!.getTrackFormat(i)
            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
            if(mime!!.startsWith("audio/")){
                mAudioTrack=i
                break
            }
        }
        return if(mAudioTrack>=0){
            mExtractor!!.getTrackFormat(mAudioTrack)
        }else null
    }

    /**
     * 读取视频数据（TODO 解码器会传递byteBuffer这个参数进来，作为存放待解码数据的缓冲区）
     */
    fun readBuffer(byteBuffer:ByteBuffer):Int{
        //【3、提取数据】
        byteBuffer.clear()
        selectSourceTrack()
        //TODO 读取数据
        //从当前offset开始检索当前编码样本并且将其存放在buffer中，返回值为读取到的音视频流的大小，<0代表读取完毕
        var readSampleCount = mExtractor!!.readSampleData(byteBuffer,0)
        if(readSampleCount<0){
            return -1
        }
        //TODO 记录当前帧的时间戳
        mCurTimeStamp=mExtractor!!.sampleTime//返回当前样本演示时间
        mCurSampleFlag=mExtractor!!.sampleFlags//返回当前帧标志
        //TODO 进入下一帧
        mExtractor!!.advance()//前进到下一个样本，若没有更多返回false
        return readSampleCount
    }

    /**
     * 选择通道（根据当前选好的音视频通道，将通道切换正确）
     */
    private fun selectSourceTrack(){
        if(mVideoTrack>=0){
            mExtractor!!.selectTrack(mVideoTrack)
        }else if(mAudioTrack>=0){
            mExtractor!!.selectTrack(mAudioTrack)
        }
    }

    /**
     * seek到指定位置，并且返回实际帧时间戳
     * 说明：seek(pos: Long)方法，主要用于跳播，快速将数据定位到指定的播放位置
     * 但是，由于视频中，除了I帧以外，PB帧都需要依赖其他的帧进行解码，所以，通常只能seek到I帧，
     * 但是I帧通常和指定的播放位置有一定误差，因此需要指定seek靠近哪个关键帧，有以下三种类型：
    SEEK_TO_PREVIOUS_SYNC：跳播位置的上一个关键帧
    SEEK_TO_NEXT_SYNC：跳播位置的下一个关键帧
    SEEK_TO_CLOSEST_SYNC：距离跳播位置的最近的关键帧
     */
    fun seek(pos:Long):Long{
        mExtractor!!.seekTo(pos,MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        return mExtractor!!.sampleTime
    }

    /**
     * 停止读取数据
     */
    fun stop(){
        //【4、释放提取器】
        mExtractor?.release()
        mExtractor=null
    }

    fun getVideoTrack():Int{
        return mVideoTrack
    }

    fun getAudioTrack():Int{
        return mAudioTrack
    }

    fun setStartPos(pos:Long){
        mStartPos = pos
    }

    /**
     * 获取当前帧时间
     */
    fun getCurrentTimeStamp():Long{
        return mCurTimeStamp
    }

    fun getSampleFlag():Int{
        return mCurSampleFlag
    }
}