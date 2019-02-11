package com.video.controls.ga;

import android.util.Log;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.youtube.player.YouTubePlayer;
import com.video.controls.video.SimpleVideoPlayer;
import com.video.controls.video.player.ControllerEventListener;

import static com.video.controls.video.player.ControllerEventListener.VIDEO_PLAY_COMPLETE;
import static com.video.controls.video.player.ControllerEventListener.VIDEO_PLAY_START;

public class PlayerGAManager implements ExoPlayer.EventListener, YouTubePlayer.PlaybackEventListener {
    private boolean isVIDEO_PLAY_START_sent = false;
    private boolean isVIDEO_PLAY_COMPLETE_sent = false;
    private SimpleVideoPlayer mSampleVideoPlayer;
    private ControllerEventListener mControllerEventListener;

    public PlayerGAManager(ControllerEventListener controllerEventListener, SimpleVideoPlayer simpleVideoPlayer) {
        mControllerEventListener = controllerEventListener;
        mSampleVideoPlayer = simpleVideoPlayer;
    }

    public void startNativeGA() {
        mSampleVideoPlayer.adEventListener(this);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (mSampleVideoPlayer.isAdDisplaying()) {

        } else {
            if (ExoPlayer.STATE_ENDED == playbackState && !isVIDEO_PLAY_COMPLETE_sent) {
                if (mControllerEventListener != null) mControllerEventListener.onEvent(VIDEO_PLAY_COMPLETE, null);
                Log.d(PlayerGAManager.class.getName(), "VIDEO_PLAY_COMPLETE");
                isVIDEO_PLAY_COMPLETE_sent = true;
            } else if (ExoPlayer.STATE_READY == playbackState && playWhenReady && !isVIDEO_PLAY_START_sent) {
                if (mControllerEventListener != null) mControllerEventListener.onEvent(VIDEO_PLAY_START, null);
                Log.d(PlayerGAManager.class.getName(), "VIDEO_PLAY_START");
                isVIDEO_PLAY_START_sent = true;
            } else if (ExoPlayer.STATE_READY == playbackState) {

            }
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }


    /***
     *
     * Youtube Events START:
     */
    @Override
    public void onPlaying() {
        if (mControllerEventListener != null) mControllerEventListener.onEvent(VIDEO_PLAY_START, "youtube");
        Log.d(PlayerGAManager.class.getName(), "VIDEO_PLAY_START - " + "youtube");
    }

    @Override
    public void onPaused() {

    }

    @Override
    public void onStopped() {
        if (mControllerEventListener != null) mControllerEventListener.onEvent(VIDEO_PLAY_COMPLETE, "youtube");
        Log.d(PlayerGAManager.class.getName(), "VIDEO_PLAY_COMPLETE - " + "youtube");
    }

    @Override
    public void onBuffering(boolean b) {

    }

    @Override
    public void onSeekTo(int i) {

    }
}
