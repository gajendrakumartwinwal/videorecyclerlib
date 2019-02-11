// Copyright 2014 Google Inc. All Rights Reserved.

package com.video.controls.video.videoad;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.video.controls.R;
import com.video.controls.ga.PlayerGAManager;
import com.video.controls.video.SimpleVideoPlayer;
import com.video.controls.video.VideoPlayer;
import com.video.controls.video.player.ControllerEventListener;
import com.video.controls.video.player.HorizontalPlaceHolderControlView;
import com.video.controls.video.player.PlaceHolderControlView;
import com.video.controls.video.player.UserEventListener;

import java.util.ArrayList;

/**
 * Video player that can play content video and ads.
 */
public class VideoPlayerWithAdPlayback extends FrameLayout implements ControllerEventListener {

    private float RATIO = 4f / 3f;
    // The wrapped video player.
    private SimpleVideoPlayer mSampleVideoPlayer;
    // The SDK will render ad playback UI elements into this ViewGroup.
    private ViewGroup mAdUiContainer;
    // The saved position in the ad to resume if app is backgrounded during ad playback.
    private int mSavedAdPosition;
    // The saved position in the content to resume to after ad playback or if app is backgrounded
    // during content playback.
    private int mSavedContentPosition;
    // Called when the content is completed.
    private OnContentCompleteListener mOnContentCompleteListener;
    // VideoAdPlayer interface implementation for the SDK to send ad play/pause type events.
    private VideoAdPlayer mVideoAdPlayer;
    // ContentProgressProvider interface implementation for the SDK to check content progress.
    private ContentProgressProvider mContentProgressProvider;

    private boolean isAdForceDestoryed;


    private PlaceHolderControlView mPlaceHOlderControlView;
    private HorizontalPlaceHolderControlView mHorizontalPlaybackControlView;
    private FrameLayout playerErrorContainer;
    private TextView mErrorMessage;
    private ImageView mPlaceHolder;
    private ProgressBar mPlayerProgress;
    private ImageView mArrowBackError;
    private ImageView mRetryError;
    //Container for video surfaceview
    private FrameLayout flVideoContainer, flAdUIContainer;
    private AttachViewListener mAttachViewListener;
    private ArrayList<ControllerEventListener> eventListeners;
    private boolean isAttachedToWindow;


    /**
     * orientation manager to handle fullscreen mode on orientation
     *
     * @param context
     * @param attrs
     */
    private OrientationManager mOrientationManager;

    public VideoPlayerWithAdPlayback(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoPlayerWithAdPlayback(Context context) {
        super(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
        isAttachedToWindow = true;
        if (mAttachViewListener != null) mAttachViewListener.onAttachToView();
    }

    public void setAttachViewListener(AttachViewListener mAttachViewListener) {
        this.mAttachViewListener = mAttachViewListener;
        if (isAttachedToWindow)
            this.mAttachViewListener.onAttachToView();
    }

    private void init() {
        mOrientationManager = new OrientationManager(getContext());

        mSavedAdPosition = 0;
        mSavedContentPosition = 0;
        mSampleVideoPlayer = (SimpleVideoPlayer) this.getRootView().findViewById(R.id.videoPlayer);
        mAdUiContainer = (ViewGroup) this.getRootView().findViewById(R.id.adUiContainer);
        mPlaceHOlderControlView = (PlaceHolderControlView) findViewById(R.id.playbackControlView);
        mHorizontalPlaybackControlView = (HorizontalPlaceHolderControlView) findViewById(R.id.horizontalPlaybackControlView);

        playerErrorContainer = (FrameLayout) findViewById(R.id.errorContainer);
        mErrorMessage = (TextView) findViewById(R.id.errorMessage);
        mPlaceHolder = (ImageView) findViewById(R.id.placeHolder);
        mPlayerProgress = (ProgressBar) findViewById(R.id.progressPlayer);

        mArrowBackError = (ImageView) findViewById(R.id.backArrow);
        mRetryError = (ImageView) findViewById(R.id.retry);

        mSampleVideoPlayer.setVideoWidthHeightRatio(RATIO);

        new PlayerGAManager(this, mSampleVideoPlayer).startNativeGA();
        mPlaceHOlderControlView.setVideoPlayer(mSampleVideoPlayer);
        mHorizontalPlaybackControlView.setVideoPlayer(mSampleVideoPlayer);
        flVideoContainer = (FrameLayout) findViewById(R.id.flVideoContainer);
        flAdUIContainer = (FrameLayout) findViewById(R.id.adUiContainer);

        adjustViewOnScreen();

        mPlaceHOlderControlView.setControllerEventListener(this);
        mHorizontalPlaybackControlView.setControllerEventListener(this);
        mArrowBackError.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onEvent(BACK_ARROW, null);
            }
        });
        mRetryError.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onEvent(RETRY_VIDEO, null);
            }
        });

        // Define VideoAdPlayer connector.
        mVideoAdPlayer = new VideoAdPlayer() {
            @Override
            public void playAd() {
                mSampleVideoPlayer.play();
            }

            @Override
            public void loadAd(String url) {
                if (isAdForceDestoryed)
                    return;
                mSampleVideoPlayer.prepareExoPlayer(url, VideoPlayer.VIDEO_TYPE.MP4);
                mSampleVideoPlayer.setAdDisplaying(true);
            }

            @Override
            public void stopAd() {
                if (isAdForceDestoryed)
                    return;
                mSampleVideoPlayer.pause();
            }

            @Override
            public void pauseAd() {
                if (isAdForceDestoryed)
                    return;
                mSampleVideoPlayer.pause();
            }

            @Override
            public void resumeAd() {
                if (isAdForceDestoryed)
                    return;
                playAd();
            }

            @Override
            public void addCallback(VideoAdPlayerCallback videoAdPlayerCallback) {
                if (isAdForceDestoryed)
                    return;
                mSampleVideoPlayer.addPlayerCallback(videoAdPlayerCallback);
            }

            @Override
            public void removeCallback(VideoAdPlayerCallback videoAdPlayerCallback) {
                if (isAdForceDestoryed)
                    return;
                mSampleVideoPlayer.removePlayerCallback(videoAdPlayerCallback);
            }

            @Override
            public VideoProgressUpdate getAdProgress() {
                if (!isAdDisplaying() || mSampleVideoPlayer.getDuration() <= 0) {
                    return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
                }
                return new VideoProgressUpdate(mSampleVideoPlayer.getCurrentPosition(),
                        mSampleVideoPlayer.getDuration());
            }
        };

        mContentProgressProvider = new ContentProgressProvider() {
            @Override
            public VideoProgressUpdate getContentProgress() {
                if (isAdDisplaying() || mSampleVideoPlayer.getDuration() <= 0) {
                    return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
                }
                return new VideoProgressUpdate(mSampleVideoPlayer.getCurrentPosition(),
                        mSampleVideoPlayer.getDuration());
            }
        };
    }

    /**
     * Set a listener to be triggered when the content (non-ad) video completes.
     */
    public void setOnContentCompleteListener(OnContentCompleteListener listener) {
        mOnContentCompleteListener = listener;
        mSampleVideoPlayer.adEventListener(new ExoPlayer.EventListener() {
            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (mSampleVideoPlayer.isAdDisplaying()) {

                } else {
                    if (ExoPlayer.STATE_ENDED == playbackState) {
                        mOnContentCompleteListener.onContentComplete();
                    } else if (ExoPlayer.STATE_READY == playbackState) {

                    }
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
                String temp = "";
            }

            @Override
            public void onPositionDiscontinuity() {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }
        });
    }

    /**
     * Save the playback progress state of the currently playing video. This is called when content
     * is paused to prepare for ad playback or when app is backgrounded.
     */
    public void savePosition(boolean resetPosition) {
        if (resetPosition) {
            mSavedAdPosition = 0;
            mSavedContentPosition = 0;
            return;
        }
        if (isAdDisplaying()) {
            mSavedAdPosition = mSampleVideoPlayer.getCurrentPosition();
        } else {
            mSavedContentPosition = mSampleVideoPlayer.getCurrentPosition();
        }
    }

    /**
     * Restore the currently loaded video to its previously saved playback progress state. This is
     * called when content is resumed after ad playback or when focus has returned to the app.
     */
    public void restorePosition() {
        int currentPlayerPos = mSampleVideoPlayer.getCurrentPosition() / 1000;
        if (isAdDisplaying() && currentPlayerPos != mSavedAdPosition / 1000) {
            mSampleVideoPlayer.seekTo(mSavedAdPosition);
        } else if (currentPlayerPos != mSavedContentPosition / 1000) {
            mSampleVideoPlayer.seekTo(mSavedContentPosition);
        }
    }

    public boolean getPlayWhenReady() {
        return mSampleVideoPlayer != null && (mSampleVideoPlayer.getSimpleExoPlayer() != null && mSampleVideoPlayer.getSimpleExoPlayer().getPlayWhenReady());
    }

    /**
     * Pauses the content video.
     */
    public void pause() {
        mSampleVideoPlayer.pause();
    }

    /**
     * Plays the content video.
     */
    public void play() {
        mSampleVideoPlayer.play();
    }

    public void toggle() {
        mSampleVideoPlayer.toggle();
    }

    /**
     * Seeks the content video.
     */
    public void seek(int time) {
        if (isAdDisplaying()) {
            // When ad is playing, set the content video position to seek to when ad finishes.
            mSavedContentPosition = time;
        } else {
            mSampleVideoPlayer.seekTo(time);
        }
    }

    /**
     * Returns current content video play time.
     */
    public int getCurrentContentTime() {
        if (isAdDisplaying()) {
            return mSavedContentPosition;
        } else {
            return mSampleVideoPlayer.getCurrentPosition();
        }
    }

    /**
     * Pause the currently playing content video in preparation for an ad to play, and disables
     * the media controller.
     */
    public void pauseContentForAdPlayback() {
//        mSampleVideoPlayer.disablePlaybackControls();
        savePosition(false);
        mSampleVideoPlayer.pause();
    }

    /**
     * Resume the content video from its previous playback progress position after
     * an ad finishes playing. Re-enables the media controller.
     */
    public void resumeContentAfterAdPlayback(String path, VideoPlayer.VIDEO_TYPE video_type) {
        mSampleVideoPlayer.play();
        mSampleVideoPlayer.prepareExoPlayer(path, video_type);
        mSampleVideoPlayer.setAdDisplaying(false);
        mSampleVideoPlayer.seekTo(mSavedContentPosition);
    }

    public void resumeContentForQuality(String path, VideoPlayer.VIDEO_TYPE video_type) {
        if (TextUtils.isEmpty(path)) {
            Log.w("ImaExample", "No content URL specified.");
            return;
        }
        mSampleVideoPlayer.play();
        mSampleVideoPlayer.seekTo(mSavedContentPosition);
        mSampleVideoPlayer.prepareExoPlayer(path, video_type, false);
        mSampleVideoPlayer.setAdDisplaying(false);
    }

    /**
     * Returns the UI element for rendering video ad elements.
     */
    public ViewGroup getAdUiContainer() {
        return mAdUiContainer;
    }

    /**
     * Returns an implementation of the SDK's VideoAdPlayer interface.
     */
    public VideoAdPlayer getVideoAdPlayer() {
        return mVideoAdPlayer;
    }

    /**
     * Returns if an ad is displayed.
     */
    public boolean isAdDisplaying() {
        return (mSampleVideoPlayer != null && mSampleVideoPlayer.isAdDisplaying());
    }

    public ContentProgressProvider getContentProgressProvider() {
        return mContentProgressProvider;
    }

    public void setAdForceDestoryed(boolean adForceDestoryed) {
        isAdForceDestoryed = adForceDestoryed;
    }

    /**
     * Adjust view controlls while activity orienation changes or user mannually change the orientation
     */
    private void adjustViewOnScreen() {
        if (Util.isFullscreen(getContext())) {
            landscapeUIChange();
        } else {
            portraitUIChange();
        }
    }

    private void landscapeUIChange() {
        Util.changeStatusBarStatus(getContext(), false);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;

        setLayoutParams(layoutParams);

        if (flVideoContainer != null) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) flVideoContainer.getLayoutParams();
            p.setMargins(0, 0, 0, 0);
            flVideoContainer.setLayoutParams(p);
            flAdUIContainer.setLayoutParams(p);

            mPlaceHolder.setLayoutParams(p);
            playerErrorContainer.setLayoutParams(p);

            if (mHorizontalPlaybackControlView != null) {
                mHorizontalPlaybackControlView.setControlerVisibility(View.VISIBLE);
            }
            if (mPlaceHOlderControlView != null) {
                mPlaceHOlderControlView.setControlerVisibility(View.GONE);
            }
        }
    }

    private void portraitUIChange() {
        Util.changeStatusBarStatus(getContext(), true);
        //Root Container Change
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = Util.get9x5Height(getContext()) + Util.convertDPToPixels(9, getContext());//9dp added for seekbar
        setLayoutParams(layoutParams);

        if (flVideoContainer != null) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) flVideoContainer.getLayoutParams();
            p.setMargins(0, 0, 0, Util.convertDPToPixels(9, getContext()));
            flVideoContainer.setLayoutParams(p);
            flAdUIContainer.setLayoutParams(p);

            mPlaceHolder.setLayoutParams(p);
            playerErrorContainer.setLayoutParams(p);

            if (mHorizontalPlaybackControlView != null) {
                mHorizontalPlaybackControlView.setControlerVisibility(View.GONE);
            }
            if (mPlaceHOlderControlView != null) {
                mPlaceHOlderControlView.setControlerVisibility(View.VISIBLE);
            }
        }
    }

    public void showPlaceHolder() {

    }

    //TODO: must be some way to remove this dependency out
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            landscapeUIChange();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            portraitUIChange();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releasePlayer();
    }

    public void releasePlayer() {
        if (mSampleVideoPlayer != null) {
            mSampleVideoPlayer.releasePlayer();
        }
    }

    /**
     * To handle user action effect in player UI and all
     *
     * @param action
     * @param value
     */
    public void onUserEvent(int action, Object value) {
        if (action == UserEventListener.NEXT_STATE_CHANGED) {
            mPlaceHOlderControlView.setNextState((Boolean) value);
            mHorizontalPlaybackControlView.setNextState((Boolean) value);
        } else if (action == UserEventListener.NEXT_STATE_CHANGED) {
            mPlaceHOlderControlView.setNextState((Boolean) value);
            mHorizontalPlaybackControlView.setNextState((Boolean) value);
        }
    }


    @Override
    public void onEvent(int eventName, Object value) {
        //Send evens to observers
        if (eventListeners != null) {
            for (int i = 0; i < eventListeners.size(); i++) {
                eventListeners.get(i).onEvent(eventName, value);
            }
        }


        if (eventName == PLAYER_ERROR) {
            savePosition(false);
            playerErrorContainer.setVisibility(View.VISIBLE);
            if (!Util.hasInternetAccess(getContext())) {
                mErrorMessage.setText(getResources().getString(R.string.network_unavailable));
            } else {
                mErrorMessage.setText(getResources().getString(R.string.video_play_error));
            }
        } else if (eventName == PLACE_HOLDER_VISIBILITY) {
            mPlaceHolder.setVisibility((Integer) value);
        } else if (eventName == SHOW_PROGRESS) {
            mPlayerProgress.setVisibility((Integer) value);
        } else if (eventName == FULLSCREEN) {
            mOrientationManager.setFullScreenToggle();
        } else if (eventName == BACK_ARROW) {
            ((Activity) getContext()).onBackPressed();
        }
    }

    public void addControllerEventListener(ControllerEventListener controllerEventListener) {
        if (eventListeners == null) eventListeners = new ArrayList<>();
        eventListeners.add(controllerEventListener);
    }

    /**
     * Interface for alerting caller of video completion.
     */
    public interface OnContentCompleteListener {
        void onContentComplete();
    }
}
