package com.video.controls.video;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

/**
 * Surface with player functionality
 */
public abstract class VideoPlayerSurface extends VideoSurfaceView implements VideoAdPlayer.VideoAdPlayerCallback {
    protected SimpleExoPlayer mSimpleExoPlayer;
    // -1 not set yet, 0  == false, 1 == true
    protected int isAdDisplaying = -1;
    protected ArrayList<ExoPlayer.EventListener> mEventListeners;
    //Added after getting bug in livestreaming after updating exoplayer
    private boolean isLiveStream;
    private DefaultTrackSelector trackSelector;

    //Added to check if ad is played or not after buffering or it is still buffering first time for time expire thresold for ad
    private boolean isAdPlayStarted;


    private Context mContext;

    public VideoPlayerSurface(Context context) {
        super(context);
        this.mContext = context;
        initPlayer();
    }

    public VideoPlayerSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initPlayer();
    }

    public static Activity scanForActivity(Context cont) {
        if (cont == null) {
            return null;
        } else if (cont instanceof Activity) {
            return (Activity) cont;
        } else if (cont instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) cont).getBaseContext());
        }

        return null;
    }

    private void initPlayer() {
        createExoPlayInstance();
        attachPlayerToView();
    }

    private void createExoPlayInstance() {
        // 1. Create a default TrackSelector
//        Handler mainHandler = new Handler();
//        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
//        TrackSelection.Factory videoTrackSelectionFactory =
//                new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
//        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter());
        RenderersFactory factory = new DefaultRenderersFactory(mContext);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        // 3. Create the player
        mSimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(factory, trackSelector);
        mSimpleExoPlayer.setVideoListener(new SimpleExoPlayer.VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
                setVideoWidthHeightRatio(width * 1f / height * 1f);
            }

            @Override
            public void onRenderedFirstFrame() {

            }
        });
//        mSimpleExoPlayer = ExoPlayerFactory.newInstance(new Renderer[]{new ExtractorRendererBuilder(this, ExoplayerUtil.getUserAgent(this), Uri.parse(mp4VideoUri1))}, trackSelector, loadControl);
    }

    public void attachPlayerToView() {
        mSimpleExoPlayer.setVideoSurfaceView(this);
        mSimpleExoPlayer.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onLoadingChanged(boolean isLoading) {
                if (mEventListeners != null && mEventListeners.size() > 0)
                    for (ExoPlayer.EventListener eventListener : mEventListeners) {
                        eventListener.onLoadingChanged(isLoading);
                    }
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (mEventListeners != null && mEventListeners.size() > 0)
                    for (ExoPlayer.EventListener eventListener : mEventListeners) {
                        eventListener.onPlayerStateChanged(playWhenReady, playbackState);
                    }

                switch (playbackState) {
                    case ExoPlayer.STATE_BUFFERING:
                        Log.d(getClass().getName(), "State Buffering");
                        break;
                    case ExoPlayer.STATE_ENDED:
                        Log.d(getClass().getName(), "State Ended");
                        onEnded();
                        break;
                    case ExoPlayer.STATE_IDLE:
                        break;
                    case ExoPlayer.STATE_READY:

                        if (playWhenReady) {
                            onPlay();
                            if (isAdDisplaying()) {
                                isAdPlayStarted = true;
                                Log.d("TimeoutCheck", "Ad play started");
                            }
                            Log.d(getClass().getName(), "State Play");
                        } else {
                            onPause();
                            Log.d(getClass().getName(), "State Pause");
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                if (mEventListeners != null && mEventListeners.size() > 0)
                    for (ExoPlayer.EventListener eventListener : mEventListeners) {
                        eventListener.onTimelineChanged(timeline, manifest);
                    }
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                if (mEventListeners != null && mEventListeners.size() > 0)
                    for (ExoPlayer.EventListener eventListener : mEventListeners) {
                        eventListener.onTracksChanged(trackGroups, trackSelections);
                    }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                onError();
                if (mEventListeners != null && mEventListeners.size() > 0)
                    for (ExoPlayer.EventListener eventListener : mEventListeners) {
                        eventListener.onPlayerError(error);
                    }
            }

            @Override
            public void onPositionDiscontinuity() {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                if (mEventListeners != null && mEventListeners.size() > 0)
                    for (ExoPlayer.EventListener eventListener : mEventListeners) {
                        eventListener.onPlaybackParametersChanged(playbackParameters);
                    }
            }

        });
    }

    public void prepareExoPlayer(String path, VideoPlayer.VIDEO_TYPE video_type) {
        prepareExoPlayer(path, video_type, true);
    }

    public void prepareExoPlayer(String path, VideoPlayer.VIDEO_TYPE video_type, boolean resetPosition) {
        if (TextUtils.isEmpty(path))
            throw new RuntimeException("invalid path to prepaire");
        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext,
                Util.getUserAgent(mContext, "yourApplicationName"), bandwidthMeter);
        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = null;
        if (video_type == VideoPlayer.VIDEO_TYPE.MP4) {
            videoSource = new ExtractorMediaSource(Uri.parse(path), dataSourceFactory, extractorsFactory, null, null);
        } else if (video_type == VideoPlayer.VIDEO_TYPE.HLS) {
            videoSource = new HlsMediaSource(Uri.parse(path), dataSourceFactory, null, null);
        } else if (video_type == VideoPlayer.VIDEO_TYPE.OTHER) {
            videoSource = new ExtractorMediaSource(Uri.parse(path), dataSourceFactory, extractorsFactory, null, null);
        }
        // Prepare the player with the source.
        mSimpleExoPlayer.prepare(videoSource, resetPosition, resetPosition);
        Log.d(getClass().getName(), "Preparing: " + path);
    }

    public SimpleExoPlayer getSimpleExoPlayer() {
        return mSimpleExoPlayer;
    }

    public boolean isAdDisplaying() {
        return isAdDisplaying != 0;
    }

    public void setAdDisplaying(boolean adDisplaying) {
        isAdDisplaying = adDisplaying ? 1 : 0;
    }

    public int getAdDisplayStatus() {
        return isAdDisplaying;
    }

    public boolean isAdPlayStarted() {
        return isAdPlayStarted;
    }

    public void adEventListener(ExoPlayer.EventListener eventListener) {
        if (mEventListeners == null)
            mEventListeners = new ArrayList<>();
        mEventListeners.add(eventListener);
    }

    public boolean isLiveStream() {
        return isLiveStream;
    }

    public void setLiveStream(boolean liveStream) {
        isLiveStream = liveStream;
    }

    public ArrayList<TrackSelectionHelper.TOIFormat> getLiveStreamResolutions() {
        return new TrackSelectionHelper().getResolutions(mSimpleExoPlayer, trackSelector);
    }

    public void changeLiveTvQuality(TrackSelectionHelper.TOIFormat formatSelected) {
        if (formatSelected.format == null) {//Auto resolution
            if (formatSelected.renderIndex == -1)
                trackSelector.clearSelectionOverrides();
            else
                trackSelector.clearSelectionOverrides(formatSelected.renderIndex);
        } else {
            MappingTrackSelector.SelectionOverride override = new MappingTrackSelector.SelectionOverride(new FixedTrackSelection.Factory(), formatSelected.groupIndex, formatSelected.trackIndex);
            trackSelector.setSelectionOverride(formatSelected.renderIndex, trackSelector.getCurrentMappedTrackInfo().getTrackGroups(formatSelected.renderIndex), override);
        }
    }
}