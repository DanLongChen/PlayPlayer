package com.chiron.playpalyer.mediac.muxer

import android.media.MediaCodec
import android.util.Log
import com.chiron.playpalyer.mediac.extractor.AudioExtractor
import com.chiron.playpalyer.mediac.extractor.VideoExtractor
import java.nio.ByteBuffer

class MP4Repack(path:String) {
    private val TAG="MP4Repack"

    //初始化音视频分离器
    private val mAExtractor:AudioExtractor= AudioExtractor(path)
    private val mVExtractor:VideoExtractor= VideoExtractor(path)

    //初始化封装器
    private val mMuxer:MMuxer= MMuxer()

    //启动重封装
    fun start(){
        val audioFormat=mAExtractor.getFormat()
        val videoFormat=mVExtractor.getFormat()

        /**
         * 判断是否有音频数据，没有音频数据则告诉封装器，忽略音频轨道
         */
        if(audioFormat!=null){
            mMuxer.addAudioTrack(audioFormat)
        }else{
            mMuxer.setNoAudio()
        }

        if(videoFormat!=null){
            mMuxer.addVideoTrack(videoFormat)
        }else{
            mMuxer.setNoVideo()
        }

        Thread{
            val buffer=ByteBuffer.allocate(500*1024)
            val bufferInfo=MediaCodec.BufferInfo()

            //音视频分离和写入
            if(audioFormat!=null){
                var size=mAExtractor.readBuffer(buffer)
                while(size>0){
                    bufferInfo.set(0,size,mAExtractor.getCurrentTimeStamp(),mAExtractor.getSampleFlag())

                    mMuxer.writeAudioData(buffer,bufferInfo)
                    size=mAExtractor.readBuffer(buffer)
                }
            }

            //视频数据分离和写入
            if(videoFormat!=null){
                var size=mVExtractor.readBuffer(buffer)
                while(size>0){
                    bufferInfo.set(0,size,mVExtractor.getCurrentTimeStamp(),mVExtractor.getSampleFlag())
                    mMuxer.writeVideoData(buffer,bufferInfo)
                    size=mVExtractor.readBuffer(buffer)
                }
            }

            mAExtractor.stop()
            mVExtractor.stop()
            mMuxer.release()
            Log.i(TAG,"重打包完成！")
        }.start()
    }
}