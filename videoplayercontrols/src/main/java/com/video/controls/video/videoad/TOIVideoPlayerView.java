package com.video.controls.video.videoad;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.video.controls.R;
import com.video.controls.video.VideoPlayer;
import com.video.controls.video.player.ControllerEventListener;
import com.video.controls.video.player.ResolutionManager;

public class TOIVideoPlayerView extends TOIYoutubePlayerView implements AttachViewListener, ControllerEventListener {

    private VideoPlayerWithAdPlayback videoPlayerWithAdPlayback;
    private VideoPlayerController.Builder mCurrentControllerBuilder;
    private boolean videoStateOnSetting;


    public TOIVideoPlayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Entry point for video player to play
     */
    private VideoPlayerController mVideoPlayerController;

    public void requestAndPlayAds(VideoPlayerController.Builder builder) {
        setVisibility(View.VISIBLE);
        mCurrentControllerBuilder = builder;
        if (mVideoPlayerController != null) {
            mVideoPlayerController.releasePlayer();
        }

        removeAllViews();
        mInflator.inflate(R.layout.internal_player, this, true);
        videoPlayerWithAdPlayback = findViewById(R.id.videoPlayerWithAdPlayback);

        if (mControllerEventListener != null)
            videoPlayerWithAdPlayback.addControllerEventListener(mControllerEventListener);
        //For replay and other events handling inwhich user action is not required
        videoPlayerWithAdPlayback.addControllerEventListener(this);

        builder.setVideoPlayerWithAdPlayback(videoPlayerWithAdPlayback);
        mVideoPlayerController = builder.build();
        videoPlayerWithAdPlayback.setAttachViewListener(this);

    }

    private void replay() {
        requestAndPlayAds(mCurrentControllerBuilder);
    }

    @Override
    public void onAttachToView() {
        mVideoPlayerController.requestAndPlayAds();
    }

    //TODO: must be some way to remove this dependency out
    public void onConfigurationChanged(Configuration newConfig) {
        if (videoPlayerWithAdPlayback != null)
            videoPlayerWithAdPlayback.onConfigurationChanged(newConfig);
    }

    public void addControllerEventListener(ControllerEventListener controllerEventListener) {
        if (videoPlayerWithAdPlayback != null)
            videoPlayerWithAdPlayback.addControllerEventListener(controllerEventListener);
        else mControllerEventListener = controllerEventListener;
    }

    @Override
    public void onEvent(int eventName, Object value) {
        if (eventName == RESET_CLICK || eventName == RETRY_VIDEO) {
            replay();
        } else if (eventName == SETTING) {
            if (mVideoPlayerController == null || mVideoPlayerController.getVideoResolutions().size() < 1) {
                Toast.makeText(getContext(), "Data not loaded", Toast.LENGTH_SHORT).show();
                return;
            }
            if (mVideoPlayerController != null) {
                videoStateOnSetting = videoPlayerWithAdPlayback != null && (videoPlayerWithAdPlayback.getPlayWhenReady());
                mVideoPlayerController.pause();
            }

            ResolutionManager.showResolutionPopup(getContext(), mVideoPlayerController.getVideoResolutions(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ResolutionManager.saveResolution((Activity) getContext(), mVideoPlayerController.getVideoResolutions().get(which).getRes());

                    mVideoPlayerController.setContentVideo(mVideoPlayerController.getVideoResolutions().get(which).getUrl(), ResolutionManager.VIDEO_QUALITY_AUTO.equalsIgnoreCase(mVideoPlayerController.getVideoResolutions().get(which).getRes()) ? VideoPlayer.VIDEO_TYPE.HLS : VideoPlayer.VIDEO_TYPE.MP4);
                    mVideoPlayerController.changeQuality();
                }
            }, new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mVideoPlayerController != null && videoStateOnSetting) {
                        mVideoPlayerController.resume();
                        videoStateOnSetting = videoPlayerWithAdPlayback != null && videoPlayerWithAdPlayback.getPlayWhenReady();
                    }
                }
            });
        }
    }


    public void releasePlayer() {
        releaseYoutubePlayer();
        if (mVideoPlayerController != null) mVideoPlayerController.releasePlayer();
    }

    public void onUserEvent(int action, Object value) {
        if (mVideoPlayerController != null &&
                mVideoPlayerController.getVideoPlayerWithAdPlayback() != null)
            mVideoPlayerController.getVideoPlayerWithAdPlayback().onUserEvent(action, value);
    }
}
