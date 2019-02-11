package com.video.controls.video.player;


import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.video.controls.R;

import static com.video.controls.video.player.ControllerEventListener.PLACE_HOLDER_VISIBILITY;
import static com.video.controls.video.player.ControllerEventListener.PLAYER_ERROR;
import static com.video.controls.video.player.ControllerEventListener.RESET_CLICK;
import static com.video.controls.video.player.ControllerEventListener.SHOW_PROGRESS;

public class PlaceHolderControlView extends CustomPlaybackControlView {

    protected boolean isLayoutVisible;
    private ControllerEventListener mControllerEventListener;
    private PlayerControllerClickListener playerControllerClickListener;
    private ImageButton nextButton;
    private ImageButton previsousButton;

    public PlaceHolderControlView(Context context) {
        super(context);
    }

    public PlaceHolderControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    protected void initViews() {
        super.initViews();
        initPlayerControllerView();
    }

    public void setControllerEventListener(ControllerEventListener controllerEventListener) {
        this.mControllerEventListener = controllerEventListener;
        playerControllerClickListener.setControllerEventListener(mControllerEventListener);
    }

    protected void initPlayerControllerView() {
        playerControllerClickListener = new PlayerControllerClickListener();
        findViewById(getCrossButtonId()).setOnClickListener(playerControllerClickListener);
        findViewById(getShareButtonId()).setOnClickListener(playerControllerClickListener);
        findViewById(getSettingButtonId()).setOnClickListener(playerControllerClickListener);
        findViewById(getFullScreenButtonId()).setOnClickListener(playerControllerClickListener);


        nextButton = findViewById(getNextButtonId());
        previsousButton = findViewById(getPreviousButtonId());
        nextButton.setOnClickListener(playerControllerClickListener);
        previsousButton.setOnClickListener(playerControllerClickListener);
        setPreviousState(false);
        setNextState(false);
    }

    protected int getNextButtonId() {
        return R.id.next1;
    }

    protected int getPreviousButtonId() {
        return R.id.prev1;
    }

    protected int getCrossButtonId() {
        return R.id.crossButton;
    }

    protected int getShareButtonId() {
        return R.id.share;
    }

    protected int getSettingButtonId() {
        return R.id.settings;
    }

    protected int getFullScreenButtonId() {
        return R.id.fullscreen;
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                setProgressVisibility(View.VISIBLE);
                break;
            case ExoPlayer.STATE_ENDED:
                setProgressVisibility(View.GONE);
                setPlaceHolderVisibility(View.VISIBLE);
                if (mSampleVideoPlayer != null && !mSampleVideoPlayer.isAdDisplaying())
                    setResetButton();
                break;
            case ExoPlayer.STATE_IDLE:
                setProgressVisibility(View.GONE);
                break;
            case ExoPlayer.STATE_READY:
                setProgressVisibility(View.GONE);
                mSampleVideoPlayer.setVisibility(View.VISIBLE);
                if (playWhenReady)
                    setPlaceHolderVisibility(View.GONE);
                if (mSampleVideoPlayer != null && !mSampleVideoPlayer.isAdDisplaying()) {
                    if (isOrientationMatch()) setVisibility(View.VISIBLE);
                    isLayoutVisible = true;
                    show();
                }
                break;
            default:
                Log.d("CUSTOM_PLAYER", "default:" + playbackState);
                break;
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        mControllerEventListener.onEvent(SHOW_PROGRESS, View.GONE);
        if (mSampleVideoPlayer != null && !mSampleVideoPlayer.isAdDisplaying()) {
            mControllerEventListener.onEvent(PLAYER_ERROR, error);
        }
        setVisibility(View.GONE);
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    public void setProgressVisibility(int isVisible) {
        mControllerEventListener.onEvent(SHOW_PROGRESS, isVisible);
        playButton.setVisibility(isVisible == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    public void setPlaceHolderVisibility(int isVisible) {
        mControllerEventListener.onEvent(PLACE_HOLDER_VISIBILITY, isVisible);
    }


    @Override
    protected void onResetClick() {
        super.onResetClick();
        mControllerEventListener.onEvent(RESET_CLICK, null);
    }

    protected boolean isOrientationMatch() {
        return isLiveSteaming || getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public void setControlerVisibility(int visibility) {
        if (View.VISIBLE == visibility && isLayoutVisible)
            setVisibility(visibility);
        else if (View.GONE == visibility)
            setVisibility(visibility);
    }

    public void setPreviousState(boolean isEnable) {
        previsousButton.setAlpha(isEnable ? 1 : 0f);
    }

    public void setNextState(boolean isEnable) {
        nextButton.setAlpha(isEnable ? 1 : 0f);
    }
}
