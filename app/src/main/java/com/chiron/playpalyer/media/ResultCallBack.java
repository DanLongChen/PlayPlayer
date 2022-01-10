package com.chiron.playpalyer.media;

import com.chiron.playpalyer.error.MediaError;

public interface ResultCallBack {
    void onSuccess();
    void onFailed(MediaError error);
}
