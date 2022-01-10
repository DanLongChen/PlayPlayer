package com.chiron.playpalyer.widget;
//自动设置播放器窗口长宽比
public interface AutoRatioViewInterface {
    void setAspectRatio(double aspectRadio);
    void onPause();
    void onResume();
}
