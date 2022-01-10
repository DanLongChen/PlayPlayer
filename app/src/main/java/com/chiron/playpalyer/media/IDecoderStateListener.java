package com.chiron.playpalyer.media;

public interface IDecoderStateListener {
    void decoderPrepare(BaseDecoder decodeJob);
    void decoderReady(BaseDecoder decodeJob);
    void decoderRunning(BaseDecoder decodeJob);
    void decoderPause(BaseDecoder decodeJob);
    void decodeOneFrame(BaseDecoder decodeJob);
    void decodeFinish(BaseDecoder decodeJob);
    void decodeDestroy(BaseDecoder decodeJob);
    void decodeError(BaseDecoder decodeJob,String msg);
}
