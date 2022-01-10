package com.chiron.playpalyer.media.muxer

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.util.Log
import java.lang.Exception
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

class MMuxer {
    private val TAG ="MMuxer"
    private var mPath:String
    private var mMediaMuxer:MediaMuxer?=null

    private var mVideoTrackIndex=-1
    private var mAudioTrackIndex=-1

    private var mIsAudioTrackAdd = false
    private var mIsVideoTrackAdd = false

    private var mIsStart = false

    init{
        //指定视频保存路径和格式
        val fileName="LVideo_"+SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(Date())+".mp4"
        val filePath=Environment.getExternalStorageDirectory().absolutePath.toString()+"/"
        mPath=filePath+fileName
        mMediaMuxer= MediaMuxer(mPath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    }

    /**
     * 在开启封装器前，首先需要设置音视频对应的数据格式，这个格式来源于音视频解封获取的那个MediaFormat，即
    MMExtractor#getVideoFormat()
    MMExtractor#getAudioFormat()
    通过mMediaMuxer!!.addTrack(mediaFormat)后，会返回音视频数据对应的轨道索引，用于封装数据时，将数据写到正确的数据轨道中。
     */
    //添加视频轨道
    fun addVideoTrack(mediaFormat: MediaFormat){
        if(mMediaMuxer!=null){
            mVideoTrackIndex = try{
                mMediaMuxer!!.addTrack(mediaFormat)
            }catch (e:Exception){
                e.printStackTrace()
                return
            }
            mIsVideoTrackAdd=true
            startMuxer()
        }
    }

    //添加音频轨道
    fun addAudioTrack(mediaFormat: MediaFormat){
        if(mMediaMuxer!=null){
            mAudioTrackIndex=try{
                mMediaMuxer!!.addTrack(mediaFormat)
            }catch (e:Exception){
                e.printStackTrace()
                return
            }
            mIsAudioTrackAdd=true
            startMuxer()
        }
    }

    /**
     * 忽略音频轨道
     */
    fun setNoAudio(){
        if(mIsAudioTrackAdd){
            return
        }
        mIsAudioTrackAdd=true
        startMuxer()
    }

    /**
     * 忽略视频轨道
     */
    fun setNoVideo(){
        if(mIsVideoTrackAdd){
            return
        }
        mIsVideoTrackAdd=true
        startMuxer()
    }

    private fun startMuxer(){
        if(mIsVideoTrackAdd && mIsAudioTrackAdd){
            mMediaMuxer?.start()
            mIsStart=true
            Log.i(TAG,"启动混合器，等待数据输入...")
        }
    }

    //TODO 数据写入
    fun writeVideoData(byteBuffer:ByteBuffer,bufferInfo:MediaCodec.BufferInfo){
        if(mIsStart){
            mMediaMuxer?.writeSampleData(mVideoTrackIndex,byteBuffer,bufferInfo)
        }
    }

    fun writeAudioData(byteBuffer:ByteBuffer,bufferInfo: MediaCodec.BufferInfo){
        if(mIsStart){
            mMediaMuxer?.writeSampleData(mAudioTrackIndex,byteBuffer,bufferInfo)
        }
    }

    fun release(){
        mIsVideoTrackAdd=false
        mIsAudioTrackAdd=false
        try {
            mMediaMuxer?.stop()
            mMediaMuxer?.release()
            mMediaMuxer=null
            Log.i(TAG,"混合器退出...")
        }catch(e:Exception){
            e.printStackTrace()
        }
    }
}