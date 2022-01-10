package com.chiron.playpalyer.media.decoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.chiron.playpalyer.media.BaseDecoder;
import com.chiron.playpalyer.media.IExtractor;
import com.chiron.playpalyer.media.extractor.VideoExtractor;

import java.nio.ByteBuffer;

public class VideoDecoder extends BaseDecoder {
    private static final String TAG = "VideoDecoder";

    /**
     * 视频解码支持两种类型渲染表面，一个是SurfaceView，一个Surface。
     * 其实最后都是传递Surface给MediaCodec
     *
     * SurfaceView应该是大家比较熟悉的View了，最常使用的就是用来做MediaPlayer的显示。当然也可以绘制图片、动画等。
     * Surface应该不是很常用了，这里为了支持后续使用OpenGL来渲染视频，所以预先做了支持。
     */

    private SurfaceView mSurfaceView = null;
    private Surface mSurface = null;

    public VideoDecoder(String path, SurfaceView surfaceView,Surface surface){
        super(path);
        this.mSurface = surface;
        this.mSurfaceView = surfaceView;
    }

    @Override
    protected boolean check() {
        if(mSurface==null && mSurfaceView==null){//不能两个都为null
            mStateListener.decodeError(this,"Surface is null");
            return false;
        }
        return true;
    }

    @Override
    protected IExtractor initExtractor(String path) {
        return new VideoExtractor(path);
    }

    @Override
    protected boolean initRender() {
        return true;
    }

    @Override
    protected void initSpaceParams(MediaFormat format) {
        mVideoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
        mVideoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
        Log.e("Daniel","mVideoWidth: "+String.valueOf(mVideoWidth)+"  mVideoHeight: "+String.valueOf(mVideoHeight));
    }

    @Override
    protected boolean configDecodec(MediaCodec codec, MediaFormat format) {
        if (mSurface != null) {
            codec.configure(format, mSurface, null, 0);
            notifyDecode();
            Log.e("Daniel", "configDecodec: " + "notifyDecode");
        } else {
            if (mSurfaceView != null && mSurfaceView instanceof SurfaceView) {
                Log.e("configDecodec", "mSurfaceView!=null");
                mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback2() {
                    @Override
                    public void surfaceRedrawNeeded(@NonNull SurfaceHolder holder) {

                    }

                    @Override
                    public void surfaceCreated(@NonNull SurfaceHolder holder) {
                        if (holder != null) {
                            Log.e("configDecodec", "holder!=null");
                            mSurface = holder.getSurface();
                            configDecodec(codec, format);
                        }
                    }

                    @Override
                    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

                    }

                    @Override
                    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

                    }
                });
            }
            return false;
        }
        return true;
    }

    @Override
    protected void render(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo) {

    }

    @Override
    protected void doneDecode() {

    }
}
