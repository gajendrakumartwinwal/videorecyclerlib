package com.video.controls;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.video.controls.video.VideoPlayer;
import com.video.controls.video.model.VideoResolution;
import com.video.controls.video.player.ControllerEventListener;
import com.video.controls.video.queue.QueueInterface;
import com.video.controls.video.queue.VideoQueueManager;
import com.video.controls.video.videoad.TOIVideoPlayerView;
import com.video.controls.video.videoad.VideoPlayerController;

import java.util.ArrayList;

public class TestVideoActivity extends AppCompatActivity implements ControllerEventListener {
    private TOIVideoPlayerView mToiVideoPlayerView;

    private final String VIDEO_QUALITY_AUTO = "Auto";
    private QueueInterface<VideoItem> mVideoQueueManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_test);
        mToiVideoPlayerView = findViewById(R.id.toivideoplayerview);
        mToiVideoPlayerView.requestYoutubeVideoPlay("aEM2kOrrNJI");

        mToiVideoPlayerView.addControllerEventListener(this);
//        initExtras();

//        createImaPlayer(mVideoQueueManager.getCurrent());
    }


    private void initExtras() {
        mVideoQueueManager = new VideoQueueManager<>(getVideoItem(), getVideoItems());
    }


    private void createImaPlayer(VideoItem videoItem) {


        mVideoQueueManager.updateButtons(mToiVideoPlayerView);
        VideoPlayer.VIDEO_TYPE videoType = VIDEO_QUALITY_AUTO.equalsIgnoreCase(videoItem.resolution) ? VideoPlayer.VIDEO_TYPE.HLS : VideoPlayer.VIDEO_TYPE.MP4;
        VideoPlayerController.Builder videoPlayerBuilder = new VideoPlayerController.Builder(this, videoItem.url, videoType)
                .setPublisherAdId("PUBLISHER_ID");

        videoPlayerBuilder.setVideoResolutions(getResolutions());
        videoPlayerBuilder.setAdTagUrl(videoItem.adUrl);

        mToiVideoPlayerView.requestAndPlayAds(videoPlayerBuilder);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mToiVideoPlayerView.onConfigurationChanged(newConfig);
    }

    @Override
    public void onEvent(int eventName, Object value) {
        if (eventName == SHARE) {

        } else if (eventName == NEXT_BUTTON) {
            VideoItem videoItem = mVideoQueueManager.getNext();
            if (videoItem != null)
                createImaPlayer(videoItem);

        } else if (eventName == PREVIOUS_BUTTON) {
            VideoItem videoItem = mVideoQueueManager.getPrevious();
            if (videoItem != null)
                createImaPlayer(videoItem);
        }
    }

    private ArrayList<VideoResolution> getResolutions() {
        ArrayList<VideoResolution> result = new ArrayList<>();
        result.add(new VideoItem("http://slike.akamaized.net/vdo/1x/1j/1x1jt5pgog/hls/master.m3u8", "", "Auto"));
        result.add(new VideoItem("http://slike.akamaized.net/vdo/1x/1j/1x1jt5pgog/87ce2cc17f_F60_144p_100.mp4", "", "144p"));
        result.add(new VideoItem("http://slike.akamaized.net/vdo/1x/1j/1x1jt5pgog/87ce2cc17f_F51_240p_256.mp4", "", "240p"));
        return result;
    }

    class VideoItem implements VideoResolution {
        String url;
        String adUrl;
        String resolution;

        public VideoItem(String url, String adUrl, String resolution) {
            this.url = url;
            this.adUrl = adUrl;
            this.resolution = resolution;
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String getRes() {
            return resolution;
        }
    }

/*

    private VideoItem getVideoItem() {
        return new VideoItem("http://slike.akamaized.net/vdo/1x/1j/1x1jt5pgog/87ce2cc17f_F51_240p_256.mp4",
                "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                        + "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast"
                        + "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct"
                        + "%3Dlinear&correlator=",
                "240p");
    }

    private ArrayList<VideoItem> getVideoItems() {
        ArrayList<VideoItem> videoItems = new ArrayList<>();
        VideoItem videoItem = new VideoItem("http://slike.akamaized.net/vdo/1x/1j/1x1jt5pgog/87ce2cc17f_F51_240p_256.mp4",
                "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                        + "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast"
                        + "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct"
                        + "%3Dlinear&correlator=",
                "240p");
        videoItems.add(videoItem);
        VideoItem videoItem1 = new VideoItem("http://slike.akamaized.net/vdo/1x/1j/1x1jt5pgog/hls/master.m3u8",
                "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                        + "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast"
                        + "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct"
                        + "%3Dlinear&correlator=",
                "240p");
        videoItems.add(videoItem1);
        VideoItem videoItem2 = new VideoItem("http://slike.akamaized.net/vod7/1x/nn/1xnne37gku/hls/master.m3u8",
                "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                        + "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast"
                        + "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct"
                        + "%3Dlinear&correlator=",
                "240p");
        videoItems.add(videoItem2);
        VideoItem videoItem3 = new VideoItem("http://slike.akamaized.net/vod7/1x/nn/1xnnep7gku/hls/master.m3u8",
                "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                        + "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast"
                        + "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct"
                        + "%3Dlinear&correlator=",
                "240p");
        videoItems.add(videoItem3);
        VideoItem videoItem4 = new VideoItem("http://slike.akamaized.net/vod7/1x/nn/1xnndjjgku/hls/master.m3u8",
                "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                        + "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast"
                        + "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct"
                        + "%3Dlinear&correlator=",
                "240p");
        videoItems.add(videoItem4);
        return videoItems;
    }
*/


    private VideoItem getVideoItem() {
        return new VideoItem("http://slike.akamaized.net/vdo/1x/1j/1x1jt5pgog/87ce2cc17f_F51_240p_256.mp4",
                "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                        + "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast"
                        + "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct"
                        + "%3Dlinear&correlator=",
                "240p");
    }

    private ArrayList<VideoItem> getVideoItems() {
        ArrayList<VideoItem> videoItems = new ArrayList<>();
        VideoItem videoItem1 = new VideoItem("http://slike.akamaized.net/vdo/1x/1j/1x1jt5pgog/hls/master.m3u8",
                "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                        + "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast"
                        + "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct"
                        + "%3Dlinear&correlator=",
                "240p");
        videoItems.add(videoItem1);
        VideoItem videoItem = new VideoItem("http://slike.akamaized.net/vdo/1x/1j/1x1jt5pgog/87ce2cc17f_F51_240p_256.mp4",
                "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                        + "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast"
                        + "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct"
                        + "%3Dlinear&correlator=",
                "240p");
        videoItems.add(videoItem);
        VideoItem videoItem2 = new VideoItem("http://slike.akamaized.net/vod7/1x/nn/1xnne37gku/hls/master.m3u8",
                "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                        + "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast"
                        + "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct"
                        + "%3Dlinear&correlator=",
                "240p");
        videoItems.add(videoItem2);
        VideoItem videoItem3 = new VideoItem("http://slike.akamaized.net/vod7/1x/nn/1xnnep7gku/hls/master.m3u8",
                "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                        + "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast"
                        + "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct"
                        + "%3Dlinear&correlator=",
                "240p");
        videoItems.add(videoItem3);
        VideoItem videoItem4 = new VideoItem("http://slike.akamaized.net/vod7/1x/nn/1xnndjjgku/hls/master.m3u8",
                "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/"
                        + "single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast"
                        + "&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct"
                        + "%3Dlinear&correlator=",
                "240p");
        videoItems.add(videoItem4);
        return videoItems;
    }


}
