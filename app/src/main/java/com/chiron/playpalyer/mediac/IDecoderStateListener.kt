package com.chiron.playpalyer.mediac

interface IDecoderStateListener {
    fun decoderPrepare(decodeJob:BaseDecoder?)
    fun decoderReady(decodeJob:BaseDecoder?)
    fun decoderRunning(decodeJob:BaseDecoder?)
    fun decoderPause(decodeJob:BaseDecoder?)
    fun decodeOneFrame(decodeJob:BaseDecoder?)
    fun decodeFinish(decodeJob:BaseDecoder?)
    fun decodeDestroy(decodeJob:BaseDecoder?)
    fun decodeError(decodeJob:BaseDecoder?,msg:String)
}