package com.chiron.playpalyer.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;

public class PlayerTextureView extends TextureView
        implements TextureView.SurfaceTextureListener, AutoRatioViewInterface{
    private double mRequestAspect = -1.0D;
    private Surface mSurface = null;

    public PlayerTextureView(Context context){
        this(context,null,0);
    }

    public PlayerTextureView(Context context,AttributeSet attributeSet){
        this(context,attributeSet,0);
    }

    public PlayerTextureView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);
    }

    //当一个surfaceTexure可用的时候调用这个方法
    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        Log.e("Daniel","onSurfaceTextureAvailable: "+surface+" "+width+" "+height);
        if (mSurface != null) {
            mSurface.release();
        }
        mSurface = new Surface(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
        Log.e("Daniel","onSurfaceTextureChange: "+surface+" "+width+" "+height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        Log.e("Daniel","onSurfaceTextureDestroyed: "+surface);
        if(mSurface!=null){
            mSurface.release();
            mSurface = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
        Log.e("Daniel","onSurfaceTextureUpdated: "+surface);
        if (mSurface != null) {
            mSurface.release();
        }
        mSurface = new Surface(surface);
    }

    /**
     * 设置view宽高比（radio=width/height）
     * @param aspectRadio
     */
    @Override
    public void setAspectRatio(double aspectRadio) {
        if(aspectRadio<0){
            throw new IllegalArgumentException("aspectRatio <0");
        }
        if(mRequestAspect!=aspectRadio){
            mRequestAspect=aspectRadio;
            requestLayout();//请求重新绘制窗口
        }
    }

    /**
     * 以传入的radio来测量视图的大小
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mRequestAspect > 0) {
            //拿到预测的初始长宽
            int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
            int initialHeight = MeasureSpec.getSize(heightMeasureSpec);

            final int horizPadding = getPaddingLeft() + getPaddingRight();
            final int vertPadding = getPaddingTop() + getPaddingBottom();

            //除去内边距
            initialWidth -= horizPadding;
            initialHeight -= vertPadding;

            final double viewAspectRatio = (double) initialWidth / initialHeight;
            //计算两者差别
            final double aspectDiff = mRequestAspect / viewAspectRatio - 1;
            if (Math.abs(aspectDiff) > 0.01) {
                if (aspectDiff > 0) {
                    initialHeight = (int) (initialWidth / mRequestAspect);
                } else {
                    initialWidth = (int) (initialHeight * mRequestAspect);
                }
                initialWidth += horizPadding;
                initialHeight += vertPadding;
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY);
            }
        }
        Log.e("Daniel","measureWidth: "+widthMeasureSpec+"  measureHeight: "+heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    public Surface getSurface(){
        return mSurface;
    }
}
