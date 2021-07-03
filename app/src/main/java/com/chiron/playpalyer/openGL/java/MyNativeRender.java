package com.chiron.playpalyer.openGL.java;

/**
 * 在这里定义native render(实现在native层)
 */
public class MyNativeRender {
    static{
        System.loadLibrary("native-render");//加载动态库
    }

    public native void native_OnInit();
    public native void native_OnUnInit();
    public native void native_SetImageDara(int format, int width,int height,byte[] bytes);
    public native void native_OnSurfaceCreated();
    public native void native_OnSurfaceChanged(int width,int height);
    public native void native_OnDrawFrame();
}
