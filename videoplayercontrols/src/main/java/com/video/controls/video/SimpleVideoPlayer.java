package com.video.controls.video;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;

import java.util.ArrayList;

/**
 * Videos playing logic just prepaire the player and start playing video by calling play method
 * SampleVideoPlayer.prepareExoPlayer(String url, VIDEO_TYPE video_type);
 * SampleVideoPlayer.play();
 */
public class SimpleVideoPlayer extends VideoPlayerSurface implements VideoPlayer {
    private ArrayList<VideoAdPlayer.VideoAdPlayerCallback> mPlayerCallbacks;
    private PlaybackState mPlaybackState;
    private boolean isVideoReset;//Added a common field for two UI controllers(Landscape & portrait)

    public SimpleVideoPlayer(Context context) {
        super(context);
    }

    public SimpleVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public long getDuration() {
        return mPlaybackState == PlaybackState.STOPPED ? 0 : mSimpleExoPlayer.getDuration();
    }

    @Override
    public long getBufferDuration() {
        return mPlaybackState == PlaybackState.STOPPED ? 0 : mSimpleExoPlayer.getBufferedPosition();
    }

    // Methods implementing the VideoPlayer interface.
    @Override
    public void play() {
        Log.d("SampleVideoPlayer", "Play");
        if (mSimpleExoPlayer != null) {
            mSimpleExoPlayer.setPlayWhenReady(true);
            mPlaybackState = PlaybackState.PLAYING;
        }
    }

    @Override
    public void toggle() {
        Log.d("SampleVideoPlayer", "toggle");
        if (mSimpleExoPlayer != null) {
            mPlaybackState = mSimpleExoPlayer.getPlayWhenReady() ? PlaybackState.PAUSED : PlaybackState.PLAYING;
            mSimpleExoPlayer.setPlayWhenReady(!mSimpleExoPlayer.getPlayWhenReady());
        }
    }

    /**
     * Pause the currently palying video
     */
    @Override
    public void pause() {
        Log.d("SampleVideoPlayer", "Pause");
        if (mSimpleExoPlayer != null) {
            mSimpleExoPlayer.setPlayWhenReady(false);
            mPlaybackState = PlaybackState.PAUSED;
        }
    }

    @Override
    public int getCurrentPosition() {
        return mSimpleExoPlayer == null ? 0 : (int) mSimpleExoPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int videoPositionMs) {
        if (mSimpleExoPlayer != null && (!isLiveStream() || isAdDisplaying()))
            mSimpleExoPlayer.seekTo(videoPositionMs);
    }

    /**
     * Stop the player
     */
    @Override
    public void stopPlayback() {
        Log.d("SampleVideoPlayer", "stopPlayback");
        if (mPlaybackState == PlaybackState.STOPPED) {
            return;
        }
        if (mSimpleExoPlayer != null) {
            mSimpleExoPlayer.stop();
            mPlaybackState = PlaybackState.STOPPED;
        }
    }

    @Override
    public void addPlayerCallback(VideoAdPlayer.VideoAdPlayerCallback callback) {
        if (mPlayerCallbacks == null)
            mPlayerCallbacks = new ArrayList<>();
        if (mSimpleExoPlayer != null)
            mPlayerCallbacks.add(callback);
    }

    @Override
    public void removePlayerCallback(VideoAdPlayer.VideoAdPlayerCallback callback) {
        if (mPlayerCallbacks != null)
            mPlayerCallbacks.remove(callback);
    }

    @Override
    public void releasePlayer() {
        Log.d("SampleVideoPlayer", "ReleasePlayer");
        if (mPlayerCallbacks != null)
            mPlayerCallbacks.clear();
        if (mEventListeners != null)
            mEventListeners.clear();
        mPlaybackState = PlaybackState.STOPPED;
        if (mSimpleExoPlayer != null)
            mSimpleExoPlayer.release();
    }

    /**
     * Called when player starts the playing video after buffering at this point video will be visible
     */

    @Override
    public void onPlay() {
        if (mPlayerCallbacks != null)
            for (VideoAdPlayer.VideoAdPlayerCallback playerCallback : mPlayerCallbacks)
                playerCallback.onPlay();
    }

    /***************
     * State callback listener only
     ******************/

    @Override
    public void onVolumeChanged(int i) {
        if (mPlayerCallbacks != null)
            for (VideoAdPlayer.VideoAdPlayerCallback playerCallback : mPlayerCallbacks)
                playerCallback.onVolumeChanged(i);
    }

    @Override
    public void onPause() {
        if (mPlayerCallbacks != null)
            for (VideoAdPlayer.VideoAdPlayerCallback playerCallback : mPlayerCallbacks)
                playerCallback.onPause();
    }

    @Override
    public void onLoaded() {

    }

    @Override
    public void onResume() {
        if (mPlayerCallbacks != null)
            for (VideoAdPlayer.VideoAdPlayerCallback playerCallback : mPlayerCallbacks)
                playerCallback.onResume();
    }

    @Override
    public void onEnded() {
        if (mPlayerCallbacks != null)
            for (VideoAdPlayer.VideoAdPlayerCallback playerCallback : mPlayerCallbacks)
                playerCallback.onEnded();
    }

    @Override
    public void onError() {
        if (mPlayerCallbacks != null)
            for (VideoAdPlayer.VideoAdPlayerCallback playerCallback : mPlayerCallbacks)
                playerCallback.onError();
    }

    public boolean isVideoReset() {
        return isVideoReset;
    }

    public void setVideoReset(boolean videoReset) {
        isVideoReset = videoReset;
    }

    private enum PlaybackState {
        STOPPED, PAUSED, PLAYING
    }

    /***************
     * Playing state callbacks
     ******************/

}