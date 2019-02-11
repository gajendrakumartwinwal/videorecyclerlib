package com.video.controls.video.videoad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.video.controls.R;
import com.video.controls.ga.PlayerGAManager;
import com.video.controls.video.player.ControllerEventListener;
import com.video.controls.youtube.YoutubeConfig;

public class TOIYoutubePlayerView extends LinearLayout {
    private int RECOVERY_DIALOG_REQUEST = 511;
    protected LayoutInflater mInflator;
    protected YouTubePlayerSupportFragment youTubePlayerSupportFragment;
    private String mYoutubeId;
    protected ControllerEventListener mControllerEventListener;
    private YouTubePlayer mYouTubePlayer;
    private boolean isInitialized = false;

    public TOIYoutubePlayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mInflator = LayoutInflater.from(context);

    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void requestYoutubeVideoPlay(String youtubeId) {
        this.mYoutubeId = youtubeId;
        setVisibility(View.VISIBLE);

        isInitialized = true;
        mInflator.inflate(R.layout.view_toi_youtube_player, this, true);
        youTubePlayerSupportFragment = (YouTubePlayerSupportFragment) ((AppCompatActivity) getContext()).getSupportFragmentManager()
                .findFragmentById(R.id.youtubesupportfragment);

        youTubePlayerSupportFragment.initialize(YoutubeConfig.DEVELOPER_KEY, onInitializedListener);
    }

    YouTubePlayer.OnInitializedListener onInitializedListener = new YouTubePlayer.OnInitializedListener() {

        @Override
        public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
            mYouTubePlayer = youTubePlayer;
            if (!wasRestored) {
                youTubePlayer.setPlaybackEventListener(new PlayerGAManager(mControllerEventListener, null));
                // loadVideo() will auto play video
                // Use cueVideo() method, if you don't want to play it automatically
                youTubePlayer.loadVideo(mYoutubeId);

                // Hiding player controls
                youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
            }
        }

        @Override
        public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
            if (errorReason.isUserRecoverableError()) {
                errorReason.getErrorDialog((Activity) getContext(), RECOVERY_DIALOG_REQUEST).show();
            } else {
                Toast.makeText(getContext(), "Error in intitilizing - " + errorReason, Toast.LENGTH_LONG).show();
            }
        }
    };

    /**
     * For youtube error dialog activity
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            youTubePlayerSupportFragment.initialize(YoutubeConfig.DEVELOPER_KEY, onInitializedListener);
        }
    }

    protected void releaseYoutubePlayer() {
        if (youTubePlayerSupportFragment != null) {
            ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction().remove(youTubePlayerSupportFragment).commit();
            youTubePlayerSupportFragment = null;
        }
        if (mYouTubePlayer != null) mYouTubePlayer.release();
    }
}
