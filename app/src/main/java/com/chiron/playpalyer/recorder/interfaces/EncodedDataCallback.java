package com.chiron.playpalyer.recorder.interfaces;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

public interface EncodedDataCallback {
    void onAudioEncodedCallback(byte[] data, MediaCodec.BufferInfo bufferInfo);
    void onVideoEncodedCallback(byte[] data, MediaCodec.BufferInfo bufferInfo);
}
