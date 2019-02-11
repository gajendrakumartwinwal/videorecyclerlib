package com.video.controls.video.videoad;


public interface onVideoLoaderCallBack {
    void setLoaderVisibility(int visibility);

    void setPlaceHolderVisibility(int visibility);

    void onResetClick();

    void onErrorOccur(String error);
}