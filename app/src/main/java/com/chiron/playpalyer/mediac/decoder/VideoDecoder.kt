package com.chiron.playpalyer.mediac.decoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.chiron.playpalyer.mediac.BaseDecoder
import com.chiron.playpalyer.mediac.IExtractor
import com.chiron.playpalyer.mediac.extractor.VideoExtractor
import java.nio.ByteBuffer

class VideoDecoder(path:String,sfv:SurfaceView?,surface:Surface?) :BaseDecoder(path){
    private val TAG = "VideoDecoder"

    /**
     * 视频解码支持两种类型渲染表面，一个是SurfaceView，一个Surface。
     * 其实最后都是传递Surface给MediaCodec
     *
     * SurfaceView应该是大家比较熟悉的View了，最常使用的就是用来做MediaPlayer的显示。当然也可以绘制图片、动画等。
     * Surface应该不是很常用了，这里为了支持后续使用OpenGL来渲染视频，所以预先做了支持。
     */
    private val mSurfaceView = sfv
    private var mSurface = surface

    override fun render(outputBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {

    }

    override fun doneDecode() {

    }

    override fun check(): Boolean {
        if(mSurfaceView == null && mSurface == null){
            Log.w(TAG,"surfaceview和surface都为空，至少一个要不为空")
            mStateListener?.decodeError(this,"显示器为空")
            return false
        }
        return true
    }

    override fun initExtractor(path: String): IExtractor {
        return VideoExtractor(path)
    }

    override fun initSpaceParams(format: MediaFormat) {

    }

    override fun initRender(): Boolean {
        return true
    }

    override fun configDecodec(decodec: MediaCodec, format: MediaFormat): Boolean {
        if(mSurface!=null){
            decodec.configure(format,mSurface,null,0)
            notifyDecode()
        }else{
            mSurfaceView?.holder?.addCallback(object:SurfaceHolder.Callback2{
                override fun surfaceRedrawNeeded(holder: SurfaceHolder) {

                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {

                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {

                }

                override fun surfaceCreated(holder: SurfaceHolder) {
                    mSurface=holder?.surface
                    configDecodec(decodec,format)
                }
            })
            return false
        }
        return true
    }


}