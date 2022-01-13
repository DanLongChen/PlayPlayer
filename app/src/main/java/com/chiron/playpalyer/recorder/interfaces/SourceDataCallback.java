package com.chiron.playpalyer.recorder.interfaces;

//数据回调接口定义
public interface SourceDataCallback {
    void onAudioSourceDataCallback(byte[] data,int index);
    void onVideoSourceDataCallback(byte[] data,int index);
}
