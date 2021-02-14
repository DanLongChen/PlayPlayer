package com.chiron.playpalyer.mediac

enum class DecodeState {
    /**開始狀態*/
    START,
    /**解碼中*/
    DECODING,
    /**暫停解碼*/
    PAUSE,
    /**正在快進*/
    SEEKING,
    /**解碼完成*/
    FINISH,
    /**解碼器釋放*/
    STOP

}