package com.video.controls.video;


import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.android.exoplayer2.ExoPlaybackException;

public interface VideoPlayer {

    /**
     * Prepaire video to play
     */
    void prepareExoPlayer(String path, VIDEO_TYPE video_type);

    /**
     * Play the currently loaded video from its current position.
     */
    void play();

    /**
     * Play or pause the currently loaded video from its current position.
     */
    void toggle();

    /**
     * Pause the currently loaded video.
     */
    void pause();

    /**
     * Get the playback progress state (milliseconds) of the current video.
     */
    int getCurrentPosition();

    /**
     * Progress the currently loaded video to the given position (milliseconds).
     */
    void seekTo(int videoPosition);

    /**
     * Get the total length of the currently loaded video in milliseconds.
     */
    long getDuration();

    /**
     * Get the buffer length of the currently loaded video in milliseconds.
     */
    long getBufferDuration();

    /**
     * Stop playing the currently loaded video.
     */
    void stopPlayback();

    /**
     * Provide the player with a callback for major video events (pause, complete, resume, etc).
     */
    void addPlayerCallback(VideoAdPlayer.VideoAdPlayerCallback callback);

    /**
     * Remove a player callback from getting notified on video events.
     */
    void removePlayerCallback(VideoAdPlayer.VideoAdPlayerCallback callback);

    void releasePlayer();

    enum VIDEO_TYPE {
        MP4, HLS, OTHER
    }

    /**
     * Interface for alerting caller of major video events.
     */
    interface PlayerCallback {

        /**
         * Called when the current video starts playing from the beginning.
         */
        void onPlayerPlay(boolean isAd);

        /**
         * Called when the current video pauses playback.
         */
        void onPlayerPause(boolean isAd);

        /**
         * Called when the current video resumes playing from a paused state.
         */
        void onPlayerResume(boolean isAd);

        /**
         * Called when the current video has completed playback to the end of the video.
         */
        void onPlayerCompleted(boolean isAd);

        /**
         * Called when an error occurs during video playback.
         */
        void onPlayError(ExoPlaybackException error, boolean isAd);
    }
}