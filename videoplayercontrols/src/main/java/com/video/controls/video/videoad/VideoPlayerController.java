// Copyright 2014 Google Inc. All Rights Reserved.

package com.video.controls.video.videoad;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.WindowManager;

import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.video.controls.video.VideoPlayer;
import com.video.controls.video.model.VideoResolution;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Ads logic for handling the IMA SDK integration code and events.
 */
public class VideoPlayerController implements LifecycleObserver {
    private static final String AD_REQUEST_EXTRA_PID = "pid";

    private final String mLanguage;
    /**
     * Log interface, so we can output the log commands to the UI or similar.
     */

    // The AdsLoader instance exposes the requestAds method.
    private AdsLoader mAdsLoader;
    // AdsManager exposes methods to control ad playback and listen to ad events.
    private AdsManager mAdsManager;
    // Ad-enabled video player.
    private VideoPlayerWithAdPlayback mVideoPlayerWithAdPlayback;
    // VAST ad tag URL to use when requesting ads during video playback.
    private String mCurrentAdTagUrl;
    //Publisher adId for video Ad Targeting
    private String mPublisherID;
    // URL of content video.
    private String mContentVideoUrl;
    private VideoPlayer.VIDEO_TYPE mVIDEO_type;
    // Tracks if the SDK is playing an ad, since the SDK might not necessarily use the video
    // player provided to play the video ad.
    private boolean mIsAdPlaying;
    private Context mContext;
    //Added event listener
    private AdEvent.AdEventListener adEventListener;

    private EventListener mListener;
    private EventListener.VideoEventType mPlayerState = EventListener.VideoEventType.DEFAULT;
    private boolean videoStateOnPauseActivity;
    private ArrayList<VideoResolution> mVideoResolutions;

    private VideoPlayerController(Builder builder) {
        //Mandatory fields
        this.mVideoPlayerWithAdPlayback = builder.videoPlayerWithAdPlayback;
        this.mContext = builder.context;
        this.mLanguage = builder.language;

        //Optional Parameters
        this.mListener = builder.eventListener;
        this.mVideoResolutions = builder.mVideoResolutions;
        addAdEventListener(builder.adEventListener);
        this.mContentVideoUrl = builder.contentVideoUrl;
        this.mVIDEO_type = builder.video_type;
        this.mCurrentAdTagUrl = builder.adTagUrl;
        this.mPublisherID = builder.publisherAdId;

        ((AppCompatActivity) mContext).getLifecycle().addObserver(this);
    }

    public VideoPlayerWithAdPlayback getVideoPlayerWithAdPlayback() {
        return mVideoPlayerWithAdPlayback;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPauseActivity() {
        ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            videoStateOnPauseActivity = mVideoPlayerWithAdPlayback.getPlayWhenReady();
            pause();
        } catch (IllegalStateException ignore) {

        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResumeActivity() {
        ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (videoStateOnPauseActivity) {
            resume();
        }
        videoStateOnPauseActivity = mVideoPlayerWithAdPlayback.getPlayWhenReady();

    }

    public ArrayList<VideoResolution> getVideoResolutions() {
        return mVideoResolutions;
    }

    public EventListener.VideoEventType getPlayerState() {
        return mPlayerState;
    }

    private void pauseContent() {
        mVideoPlayerWithAdPlayback.pauseContentForAdPlayback();
        mIsAdPlaying = true;
        notifyEvent(EventListener.VideoEventType.VIDEO_CONTENT_PAUSED);
    }

    public void resumeContent() {
        notifyEvent(EventListener.VideoEventType.VIDEO_CONTENT_RESUMED);
        mVideoPlayerWithAdPlayback.resumeContentAfterAdPlayback(mContentVideoUrl, mVIDEO_type);
        mIsAdPlaying = false;
    }

    public void changeQuality() {
        mVideoPlayerWithAdPlayback.resumeContentForQuality(mContentVideoUrl, mVIDEO_type);
        mIsAdPlaying = false;
    }

    /**
     * Request and subsequently play video ads from the ad server.
     */
    public void requestAndPlayAds() {
        if (TextUtils.isEmpty(mCurrentAdTagUrl)) {
            resumeContent();
            return;
        }

        // Since we're switching to a new video, tell the SDK the previous video is finished.
        if (mAdsManager != null) {
            mAdsManager.destroy();
        }
        if (mAdsLoader != null)
            mAdsLoader.contentComplete();


        // Create an AdsLoader and optionally set the language.
        notifyEvent(EventListener.VideoEventType.AD_IMA_INIT_REQUESTED);
        ImaSdkSettings imaSdkSettings = new TOIImaSdkSettings();
        imaSdkSettings.setLanguage(mLanguage);
        // Factory class for creating SDK objects.
        ImaSdkFactory mSdkFactory = ImaSdkFactory.getInstance();
        mAdsLoader = mSdkFactory.createAdsLoader(mContext, imaSdkSettings);

        mAdsLoader.addAdErrorListener(new AdErrorEvent.AdErrorListener() {
            /**
             * An event raised when there is an error loading or playing ads.
             */
            @Override
            public void onAdError(AdErrorEvent adErrorEvent) {
                if (mListener != null)
                    mListener.onAdError(adErrorEvent);
                resumeContent();
            }
        });

        mAdsLoader.addAdsLoadedListener(new AdsLoadedListener());

        mVideoPlayerWithAdPlayback.setOnContentCompleteListener(new VideoPlayerWithAdPlayback.OnContentCompleteListener() {
            /**
             * Event raised by VideoPlayerWithAdPlayback when content video is complete.
             */
            @Override
            public void onContentComplete() {
                mAdsLoader.contentComplete();
            }
        });

        // Container with references to video player and ad UI ViewGroup.
        AdDisplayContainer mAdDisplayContainer = mSdkFactory.createAdDisplayContainer();
        mAdDisplayContainer.setPlayer(mVideoPlayerWithAdPlayback.getVideoAdPlayer());
        mAdDisplayContainer.setAdContainer(mVideoPlayerWithAdPlayback.getAdUiContainer());

        // Create the ads request.
        AdsRequest request = mSdkFactory.createAdsRequest();

        prepareAdUrlWithCustomParams();
        request.setAdTagUrl(mCurrentAdTagUrl);

        request.setAdDisplayContainer(mAdDisplayContainer);
        request.setContentProgressProvider(mVideoPlayerWithAdPlayback.getContentProgressProvider());
        notifyEvent(EventListener.VideoEventType.AD_REQUESTED);
        // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
        mAdsLoader.requestAds(request);
    }

    private void prepareAdUrlWithCustomParams() {
        String customParamUrl = "";
        //append publisher id to adTagUrl
        if (!TextUtils.isEmpty(mPublisherID)) {
            customParamUrl = AD_REQUEST_EXTRA_PID + "=" + mPublisherID;
        }
        try {
            customParamUrl = URLEncoder.encode(customParamUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            customParamUrl = "";
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(customParamUrl))
            mCurrentAdTagUrl = mCurrentAdTagUrl + "&cust_params=" + customParamUrl;
        //G: GDPR Changes
        /*if (TOIApplication.getInstance().isEU()) {
            mCurrentAdTagUrl = mCurrentAdTagUrl + "&npa=1";

        }*/
    }

    public String getAdTagUrl() {
        return mCurrentAdTagUrl;
    }

    /**
     * Set metadata about the content video. In more complex implementations, this might
     * more than just a URL and could trigger additional decisions regarding ad tag selection.
     */
    //TODO: Need to verify how to handle in builder pattern
    public void setContentVideo(String videoPath, VideoPlayer.VIDEO_TYPE video_type) {
        mContentVideoUrl = videoPath;
        mVIDEO_type = video_type;
    }

    /**
     * Save position of the video, whether content or ad. Can be called when the app is
     * paused, for example.
     */
    public void pause() {
        pause(false);
    }

    public void pause(boolean resetPosition) {
        mVideoPlayerWithAdPlayback.savePosition(resetPosition);
        if (mAdsManager != null && mVideoPlayerWithAdPlayback.isAdDisplaying()) {
            mAdsManager.pause();
        } else {
            mVideoPlayerWithAdPlayback.pause();
        }
    }

    /**
     * Restore the previously saved progress location of the video. Can be called when
     * the app is resumed.
     */
    public void resume() {
        mVideoPlayerWithAdPlayback.restorePosition();
        if (mAdsManager != null && mVideoPlayerWithAdPlayback.isAdDisplaying()) {
            mAdsManager.resume();
        } else {
            mVideoPlayerWithAdPlayback.play();
        }
    }

    /**
     * Seeks to time in content video in seconds.
     */
    public void seek(double time) {
        mVideoPlayerWithAdPlayback.seek((int) (time * 1000.0));
    }

    /**
     * Returns the current time of the content video in seconds.
     */
    public double getCurrentContentTime() {
        return ((double) mVideoPlayerWithAdPlayback.getCurrentContentTime()) / 1000.0;
    }

    public void releasePlayer() {
        if (mAdsManager != null)
            mAdsManager.destroy();
        if (mVideoPlayerWithAdPlayback != null)
            mVideoPlayerWithAdPlayback.releasePlayer();
    }

    /**
     * Release ad manager if ad time out and play video else skip
     */
    public void releaseAdManagerAndPlayContent() {
        if (mAdsManager != null)
            mAdsManager.destroy();
        if (mVideoPlayerWithAdPlayback != null)
            mVideoPlayerWithAdPlayback.setAdForceDestoryed(true);
        resumeContent();
    }

    private void addAdEventListener(AdEvent.AdEventListener adEventListener) {
        if (mAdsManager != null) {
            mAdsManager.addAdEventListener(adEventListener);
        } else {
            this.adEventListener = adEventListener;
        }
    }

    private void notifyEvent(EventListener.VideoEventType eventType) {
        if (mListener != null && eventType != null) {
            mPlayerState = eventType;
            mListener.onVideoEvent(eventType);
        }
    }

    public interface EventListener {

        void onVideoEvent(VideoEventType eventType);

        void onAdError(AdErrorEvent adErrorEvent);

        enum VideoEventType {
            DEFAULT,// state when player is busy doing init
            AD_REQUESTED,
            AD_LOAD_SKIPPED,
            AD_LOAD_FAILED,
            AD_LOADED,
            AD_PLAYING,
            AD_PLAY_COMPLETED,
            AD_PLAY_SKIPPED,
            AD_IMA_INIT_REQUESTED,
            VIDEO_CONTENT_PAUSED,
            VIDEO_CONTENT_RESUMED,
            VIDEO_CONTENT_COMPLETED;

            private String mValue;

            @Override
            public String toString() {
                return mValue;
            }
        }
    }

    public static class Builder {
        //Mandatory fields
        final Context context;
        VideoPlayerWithAdPlayback videoPlayerWithAdPlayback;
        final String contentVideoUrl;
        final VideoPlayer.VIDEO_TYPE video_type;
        private ArrayList<VideoResolution> mVideoResolutions;

        //Optional Parameters
        String language = "eng";
        VideoPlayerController.EventListener eventListener;
        AdEvent.AdEventListener adEventListener;
        String adTagUrl;
        String publisherAdId;

        public Builder(Context context, String contentVideoUrl, VideoPlayer.VIDEO_TYPE videoType) {
            this.context = context;
            this.contentVideoUrl = contentVideoUrl;
            this.video_type = videoType;
        }

        public Builder setVideoPlayerWithAdPlayback(VideoPlayerWithAdPlayback videoPlayerWithAdPlayback) {
            this.videoPlayerWithAdPlayback = videoPlayerWithAdPlayback;
            return this;
        }


        public Builder setVideoResolutions(ArrayList<VideoResolution> resolutions) {
            this.mVideoResolutions = resolutions;
            return this;
        }

        public Builder setLanguage(String language) {
            this.language = language;
            return this;
        }

        public Builder setEventListener(EventListener eventListener) {
            this.eventListener = eventListener;
            return this;
        }

        //Register AdEvent listener for Ad GA
        public Builder setAdEventListener(AdEvent.AdEventListener adEventListener) {
            this.adEventListener = adEventListener;
            return this;
        }

        /**
         * Set the ad tag URL the player should use to request ads when playing a content video.
         */
        public Builder setAdTagUrl(String adTagUrl) {
            this.adTagUrl = adTagUrl;
            return this;
        }

        public Builder setPublisherAdId(String publisherAdId) {
            this.publisherAdId = publisherAdId;
            return this;
        }

        public VideoPlayerController build() {
            return new VideoPlayerController(this);
        }
    }

    // Inner class implementation of AdsLoader.AdsLoaderListener.
    private class AdsLoadedListener implements AdsLoader.AdsLoadedListener {
        /**
         * An event raised when ads are successfully loaded from the ad server via AdsLoader.
         */
        @Override
        public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
            // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
            // events for ad playback and errors.
            mAdsManager = adsManagerLoadedEvent.getAdsManager();

            // Attach event and error event listeners.
            mAdsManager.addAdErrorListener(new AdErrorEvent.AdErrorListener() {
                /**
                 * An event raised when there is an error loading or playing ads.
                 */
                @Override
                public void onAdError(AdErrorEvent adErrorEvent) {
                    if (mListener != null)
                        mListener.onAdError(adErrorEvent);
                    resumeContent();
                }
            });
            if (adEventListener != null) {
                mAdsManager.addAdEventListener(adEventListener);
                adEventListener = null;
            }
            mAdsManager.addAdEventListener(new AdEvent.AdEventListener() {
                /**
                 * Responds to AdEvents.
                 */
                @Override
                public void onAdEvent(AdEvent adEvent) {

                    // These are the suggested event types to handle. For full list of all ad
                    // event types, see the documentation for AdEvent.AdEventType.
                    switch (adEvent.getType()) {
                        case LOADED:
                            // AdEventType.LOADED will be fired when ads are ready to be
                            // played. AdsManager.start() begins ad playback. This method is
                            // ignored for VMAP or ad rules playlists, as the SDK will
                            // automatically start executing the playlist.
                            mAdsManager.start();
                            notifyEvent(EventListener.VideoEventType.AD_LOADED);
                            break;
                        case CONTENT_PAUSE_REQUESTED:
                            // AdEventType.CONTENT_PAUSE_REQUESTED is fired immediately before
                            // a video ad is played.
                            pauseContent();
                            break;
                        case CONTENT_RESUME_REQUESTED:
                            // AdEventType.CONTENT_RESUME_REQUESTED is fired when the ad is
                            // completed and you should start playing your content.
                            resumeContent();
                            break;
                        case PAUSED:
                            mIsAdPlaying = false;
                            break;
                        case RESUMED:
                            mIsAdPlaying = true;
                            break;
                        case ALL_ADS_COMPLETED:
                            if (mAdsManager != null) {
                                mAdsManager.destroy();
                                mAdsManager = null;
                            }
                            break;
                        /*case SKIPPED:
                            resumeContent();
                            break;*/
                        default:
                            break;
                    }
                }
            });
            mAdsManager.init();
        }
    }

}
