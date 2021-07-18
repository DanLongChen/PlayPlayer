package com.chiron.player.openglJava.utils;

import android.opengl.GLES30;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class ShaderUtil {
    private static final String TAG = "ShaderUtil";
    public static int loadShader(int type, String source){
        //1、创建shader
        int shader = GLES30.glCreateShader(type);
        if(shader==0){//create shader failed
            Log.e(TAG,"create shader failed,type = "+type);
            return 0;
        }
        //2、关联shader和代码
        GLES30.glShaderSource(shader,source);
        //3、进行编译
        GLES30.glCompileShader(shader);
        int[] compiledResult = new int[1];
        //4、获取编译状态
        GLES30.glGetShaderiv(shader,GLES30.GL_COMPILE_STATUS,compiledResult,0);
        if(compiledResult[0]==0){
            Log.e(TAG,"Compile err! shader type:"+type+" info:"+GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader);//编译失败则删除
            return 0;
        }
        return shader;
    }

    public static int createProgram(String vertexSource, String fragmentSource){
        //1、创建着色器
        int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER,vertexSource);
        if(vertexShader==0){
            Log.e(TAG,"load vertexShader failed!");
            return 0;
        }
        int fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER,fragmentSource);
        if(fragmentShader==0){
            Log.e(TAG,"load fragmentShader failed!");
            return 0;
        }
        //2、创建program
        int program = GLES30.glCreateProgram();
        if(program==0){
            Log.e(TAG,"create program failed!");
            return 0;
        }
        //3、着色器attach到program
        GLES30.glAttachShader(program,vertexShader);
        GLES30.glAttachShader(program,fragmentShader);
        GLES30.glDeleteShader(vertexShader);
        GLES30.glDeleteShader(fragmentShader);
        //4、链接program
        GLES30.glLinkProgram(program);
        //5、检查链接状态
        int[] linkResult = new int[1];
        GLES30.glGetProgramiv(program,GLES30.GL_LINK_STATUS,linkResult,0);
        if(linkResult[0]==0){
            Log.e(TAG,"link program err, info:"+GLES30.glGetProgramInfoLog(program));
            GLES30.glDeleteProgram(program);
            return 0;
        }
        return program;
    }
}
