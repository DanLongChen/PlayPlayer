package com.chiron.playpalyer.error;

public enum MediaError {
    EMPTY_MEDIA_FORMAT_ERROR(0x00,"empty media format");

    int code;
    String message;

    private MediaError(int code,String message){
        this.code = code;
        this.message = message;
    }
}
