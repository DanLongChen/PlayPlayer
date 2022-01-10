package com.chiron.playpalyer.media;

import android.media.MediaFormat;

import com.chiron.playpalyer.error.MediaError;

import java.util.List;

public interface MediaFormatCallback {
    void onSuccess(List<MediaFormat> mediaFormatList);
    void onFailed(MediaError error);
}
