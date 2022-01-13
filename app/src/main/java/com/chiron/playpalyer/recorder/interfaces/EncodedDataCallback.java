package com.chiron.playpalyer.recorder.interfaces;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

public interface EncodedDataCallback {
    void onAudioEncodedCallbacl(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo);
    void onVideoEncodedCallbacl(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo);
}
