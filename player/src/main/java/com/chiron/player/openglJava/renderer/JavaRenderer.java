package com.chiron.player.openglJava.renderer;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import com.chiron.player.openglJava.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class JavaRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "JavaRenderer";
    private Context context;
    private int mProgram;
    private int mPositionHandler;
    private final String vertexSource =
                    "#version 300 es\n" +
                    "\n" +
                    "layout(location = 0) in vec4 vPosition;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    gl_Position = vPosition;\n" +
                    "}\n";
    private final String fragmentSource =
                    "#version 300 es\n" +
                    "\n" +
                    "precision mediump float;\n" +
                    "out vec4 fragColor;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    fragColor = vec4 ( 1.0, 0.0, 0.0, 1.0 );\n" +
                    "}\n";

    public JavaRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mProgram = ShaderUtil.createProgram(vertexSource, fragmentSource);
        mPositionHandler = GLES30.glGetAttribLocation(mProgram, "vPosition");
        GLES30.glClearColor(0, 0, 0, 1);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //视距区域使用GLSurfaceView的宽高
        GLES30.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        int vertexCount = 3;
        float[] vertexes = new float[]{
                0.0f, 0.5f, 0.0f,//代表第一个点(x,y,z)
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f
        };
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexes.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        FloatBuffer vfb = vbb.asFloatBuffer();
        vfb.put(vertexes);
        vfb.position(0);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);//clear color buffer
        //1、选择使用的程序
        GLES30.glUseProgram(mProgram);
        //2、加载顶点数据
        GLES30.glVertexAttribPointer(mPositionHandler, vertexCount, GLES30.GL_FLOAT, false, 3 * 4, vfb);
        GLES30.glEnableVertexAttribArray(mPositionHandler);
        //3、绘制
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount);
    }
}
