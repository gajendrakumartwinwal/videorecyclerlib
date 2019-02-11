package com.video.controls.video.player;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.video.controls.R;
import com.video.controls.video.SimpleVideoPlayer;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

public abstract class CustomPlaybackControlView extends FrameLayout implements View.OnTouchListener, ExoPlayer.EventListener {
    public static final int DEFAULT_FAST_FORWARD_MS = 15000;
    public static final int DEFAULT_REWIND_MS = 5000;
    public static final int DEFAULT_SHOW_DURATION_MS = 5000;
    private static final int PROGRESS_BAR_MAX = 1000;
    private static final long MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 3000;
    private final ComponentListener componentListener;
    private final StringBuilder formatBuilder;
    private final Formatter formatter;
    private final Timeline.Window currentWindow;
    protected SimpleVideoPlayer mSampleVideoPlayer;
    protected int layoutID = R.layout.video_player_control;
    protected ImageButton playButton;
    protected TextView time;
    protected TextView timeCurrent;
    protected TextView timeDivider;
    protected SeekBar progressBar;
    protected View fastForwardButton;
    protected View rewindButton;
    protected Context mContext;
    protected boolean isLiveSteaming;
    protected View topControl, centerControl, bottomControl;
    private ExoPlayer player;
    private PlaybackControlView.VisibilityListener visibilityListener;
    private final Runnable hideAction = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private boolean dragging;
    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
    private int rewindMs = DEFAULT_REWIND_MS;
    private int fastForwardMs = DEFAULT_FAST_FORWARD_MS;
    private int showDurationMs = DEFAULT_SHOW_DURATION_MS;
    private ArrayList<SeekChangeListener> seekChangeListenerList;

    public CustomPlaybackControlView(Context context) {
        this(context, null);
    }

    public CustomPlaybackControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomPlaybackControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        currentWindow = new Timeline.Window();
        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
        componentListener = new ComponentListener();

        LayoutInflater.from(context).inflate(getLayoutID(), this);
        setOnTouchListener(this);//Added to hide and show controll on touch event
        initViews();

        progressBar.setPadding(0, 0, 0, 0);
//        progressBar.getThumb().setColorFilter(ResourcesCompat.getColor(context.getApplicationContext().getResources(), R.color.colorAccent, null), PorterDuff.Mode.SRC_ATOP);
        progressBar.setOnSeekBarChangeListener(componentListener);
        playButton.setOnClickListener(componentListener);

        if (rewindButton != null) {
            rewindButton.setOnClickListener(componentListener);
        }
        if (fastForwardButton != null) {
            fastForwardButton.setOnClickListener(componentListener);
        }
        updateAll();
    }

    public void setSeekChangeListener(SeekChangeListener playerTimeChangeListener) {
        if (seekChangeListenerList == null)
            seekChangeListenerList = new ArrayList<>();
        seekChangeListenerList.add(playerTimeChangeListener);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isVideoReset()) {
            return false;
        }
        if (isVisible()) {
            hide();
        } else {
            show();
        }
        return false;
    }

    protected void initViews() {
        topControl = findViewById(R.id.top_control);
        bottomControl = findViewById(R.id.bottom_control);
        centerControl = findViewById(R.id.centerControl);
        time = (TextView) findViewById(R.id.time);
        timeCurrent = (TextView) findViewById(R.id.time_current);
        timeDivider = (TextView) findViewById(R.id.timeDivider);
        progressBar = (SeekBar) findViewById(R.id.mediacontroller_progress);
        playButton = (ImageButton) findViewById(R.id.play);
    }

    /**
     * Sets the {@link ExoPlayer} to control.
     *
     * @param sampleVideoPlayer the {@code ExoPlayer} to control.
     */
    public void setVideoPlayer(SimpleVideoPlayer sampleVideoPlayer) {
        this.mSampleVideoPlayer = sampleVideoPlayer;
        if (this.player != null) {
            this.player.removeListener(componentListener);
        }
        this.player = sampleVideoPlayer.getSimpleExoPlayer();
        if (mSampleVideoPlayer.isLiveStream())
            setSeekBarVisibility(GONE);
        this.player.addListener(this);
        if (mSampleVideoPlayer != null) {
            mSampleVideoPlayer.adEventListener(componentListener);
        }
        updateAll();
    }

    /**
     * Sets the {@link PlaybackControlView.VisibilityListener}.
     *
     * @param listener The listener to be notified about visibility changes.
     */
    public void setVisibilityListener(PlaybackControlView.VisibilityListener listener) {
        this.visibilityListener = listener;
    }

    /**
     * Sets the rewind increment in milliseconds.
     *
     * @param rewindMs The rewind increment in milliseconds.
     */
    public void setRewindIncrementMs(int rewindMs) {
        this.rewindMs = rewindMs;
    }

    /**
     * Sets the fast forward increment in milliseconds.
     *
     * @param fastForwardMs The fast forward increment in milliseconds.
     */
    public void setFastForwardIncrementMs(int fastForwardMs) {
        this.fastForwardMs = fastForwardMs;
    }

    /**
     * Sets the duration to show the playback control in milliseconds.
     *
     * @param showDurationMs The duration in milliseconds.
     */
    public void setShowDurationMs(int showDurationMs) {
        this.showDurationMs = showDurationMs;
    }

    /**
     * Shows the controller for the duration last passed to {@link #setShowDurationMs(int)}, or for
     * {@link #DEFAULT_SHOW_DURATION_MS} if {@link #setShowDurationMs(int)} has not been called.
     */
    public void show() {
        boolean isStarted = player != null && player.getDuration() > 0;
        if (isStarted || isLiveSteaming) {
            show(showDurationMs);
        }
    }

    /**
     * Shows the controller for the {@code durationMs}. If {@code durationMs} is 0 the controller is
     * shown until {@link #hide()} is called.
     *
     * @param durationMs The duration in milliseconds.
     */
    public void show(int durationMs) {
        setControlVisibility(true);
        if (visibilityListener != null) {
            visibilityListener.onVisibilityChange(getVisibility());
        }
        updateAll();
        showDurationMs = durationMs;
        hideDeferred();
    }

    /**
     * Hides the controller.
     */
    public void hide() {
        setControlVisibility(false);
        if (visibilityListener != null) {
            visibilityListener.onVisibilityChange(getVisibility());
        }
//        removeCallbacks(updateProgressAction);
        removeCallbacks(hideAction);
    }

    /**
     * Returns whether the controller is currently visible.
     */
    public boolean isVisible() {
        return bottomControl.getVisibility() == View.VISIBLE;
    }

    private void hideDeferred() {
        removeCallbacks(hideAction);
        if (showDurationMs > 0) {
            postDelayed(hideAction, showDurationMs);
        }
    }

    private void updateAll() {
        updatePlayPauseButton();
        updateNavigation();
        updateProgress();
    }

    private void updatePlayPauseButton() {
        boolean playing = player != null && player.getPlayWhenReady();
        if (!isVideoReset()) {
            playButton.setImageResource(playing ? R.drawable.ic_action_pause : R.drawable.ic_action_play);
        }
//        playButton.setContentDescription(getResources().getString(playing ? R.string.pause_description : R.string.play_description));
    }

    private void updateNavigation() {
        Timeline currentTimeline = player != null ? player.getCurrentTimeline() : null;
        boolean haveTimeline = currentTimeline != null;
        boolean isSeekable = false;
//        boolean enablePrevious = false;
//        boolean enableNext = false;
        try {
            if (haveTimeline) {
                int currentWindowIndex = player.getCurrentWindowIndex();
                currentTimeline.getWindow(currentWindowIndex, currentWindow);
                isSeekable = currentWindow.isSeekable;
//            enablePrevious = currentWindowIndex > 0 || isSeekable || !currentWindow.isDynamic;
//            enableNext = (currentWindowIndex < currentTimeline.getWindowCount() - 1) || currentWindow.isDynamic;
            }
        } catch (Exception e) {
            // TODO: 04/04/17 index out of bound occures after updating exoplayer
        }
        setButtonEnabled(isSeekable, fastForwardButton);
        setButtonEnabled(isSeekable, rewindButton);
        progressBar.setEnabled(isSeekable);
    }

    private void updateProgress() {
        long duration = player == null ? 0 : player.getDuration();
        long position = player == null ? 0 : player.getCurrentPosition();
        time.setText(stringForTime(duration));
        if (!dragging) {
            timeCurrent.setText(stringForTime(position));
        }
        int playbackState = player == null ? ExoPlayer.STATE_IDLE : player.getPlaybackState();

        if (!dragging && playbackState != ExoPlayer.STATE_BUFFERING) {
            progressBar.setProgress(progressBarValue(position));
        }
        long bufferedPosition = player == null ? 0 : player.getBufferedPosition();
        progressBar.setSecondaryProgress(progressBarValue(bufferedPosition));
        // Remove scheduled updates.
        removeCallbacks(updateProgressAction);
        // Schedule an update if necessary.
        if (playbackState != ExoPlayer.STATE_IDLE && playbackState != ExoPlayer.STATE_ENDED) {
            long delayMs;
            if (player.getPlayWhenReady() && playbackState == ExoPlayer.STATE_READY) {
                delayMs = 1000 - (position % 1000);
                if (delayMs < 200) {
                    delayMs += 1000;
                }
            } else {
                delayMs = 1000;
            }
            postDelayed(updateProgressAction, delayMs);
        }
    }

    private void setButtonEnabled(boolean enabled, View view) {
        if (view == null) {
            return;
        }
        view.setEnabled(enabled);
        view.setAlpha(enabled ? 1f : 0.3f);
        view.setVisibility(VISIBLE);

    }

    private String stringForTime(long timeMs) {
        if (timeMs == C.TIME_UNSET) {
            timeMs = 0;
        }
        long totalSeconds = (timeMs + 500) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        formatBuilder.setLength(0);
        return hours > 0 ? formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : formatter.format("%02d:%02d", minutes, seconds).toString();
    }

    private int progressBarValue(long position) {
        long duration = player == null ? C.TIME_UNSET : player.getDuration();
        return duration == C.TIME_UNSET || duration == 0 ? 0
                : (int) ((position * PROGRESS_BAR_MAX) / duration);
    }

    private long positionValue(int progress) {
        long duration = player == null ? C.TIME_UNSET : player.getDuration();
        return duration == C.TIME_UNSET ? 0 : ((duration * progress) / PROGRESS_BAR_MAX);
    }

    private void rewind() {
        player.seekTo(Math.max(player.getCurrentPosition() - rewindMs, 0));
    }

    private void fastForward() {
        player.seekTo(Math.min(player.getCurrentPosition() + fastForwardMs, player.getDuration()));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (player == null || event.getAction() != KeyEvent.ACTION_DOWN) {
            return super.dispatchKeyEvent(event);
        }
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                fastForward();
                break;
            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                rewind();
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                player.setPlayWhenReady(!player.getPlayWhenReady());
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                player.setPlayWhenReady(true);
                break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                player.setPlayWhenReady(false);
                break;
            default:
                return false;
        }
        show();
        return true;
    }

    public void setResetButton() {
        setVideoReset(true);
        playButton.setImageResource(R.drawable.ic_action_replay);
        removeCallbacks(updateProgressAction);
        removeCallbacks(hideAction);
        setControlVisibility(true);
    }

    protected void onResetClick() {
        setVideoReset(false);
    }

    protected boolean isVideoReset() {
        if (mSampleVideoPlayer != null) {
            return mSampleVideoPlayer.isVideoReset();
        } else {
            return false;
        }
    }

    protected void setVideoReset(boolean isReset) {
        if (mSampleVideoPlayer != null) {
            mSampleVideoPlayer.setVideoReset(isReset);
        }
    }

    /**
     * Disable Visibility for
     *
     * @param v hide seekbar for live streamming
     */
    public void setSeekBarVisibility(int v) {
        progressBar.setVisibility(v);
        time.setVisibility(v);
        timeDivider.setVisibility(v);
        timeCurrent.setVisibility(v);
        isLiveSteaming = v == View.GONE;
    }

    protected void setControlVisibility(boolean isVisible) {
        if (isVisible) {
//            topControl.setVisibility(View.VISIBLE);
            bottomControl.setVisibility(View.VISIBLE);
            centerControl.setVisibility(View.VISIBLE);
        } else {
//            topControl.setVisibility(View.GONE);
            bottomControl.setVisibility(View.GONE);
            centerControl.setVisibility(View.GONE);
        }
    }

    protected int getLayoutID() {
        return layoutID;
    }

    /**
     * Listener to be notified about changes of the visibility of the UI control.
     */
    public interface VisibilityListener {
        /**
         * Called when the visibility changes.
         *
         * @param visibility The new visibility. Either {@link View#VISIBLE} or {@link View#GONE}.
         */
        void onVisibilityChange(int visibility);
    }

    public interface SeekChangeListener {
        void onSeekChanged(long beforeSeekPos, long
                postSeekPos);// currentPos is the previous playing position before user seek
    }

    private final class ComponentListener implements ExoPlayer.EventListener,
            SeekBar.OnSeekBarChangeListener, OnClickListener {

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            removeCallbacks(hideAction);
            dragging = true;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                timeCurrent.setText(stringForTime(positionValue(progress)));
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            dragging = false;
            setVideoReset(false);
            final long beforeSeekPosition = player.getCurrentPosition();
            player.seekTo(positionValue(seekBar.getProgress()));
            if (seekChangeListenerList != null) {
                for (SeekChangeListener listener :
                        seekChangeListenerList) {
                    if (listener != null) {
                        listener.onSeekChanged(beforeSeekPosition, player.getCurrentPosition());
                    }
                }
            }
            hideDeferred();
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            updatePlayPauseButton();
            updateProgress();
        }

        @Override
        public void onPositionDiscontinuity() {
            updateNavigation();
            updateProgress();
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            updateNavigation();
            updateProgress();
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            // Do nothing.
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            // Do nothing.
        }

        @Override
        public void onClick(View view) {
            if (!isVisible()) {
                return;
            }
            Timeline currentTimeline = player.getCurrentTimeline();
            if (fastForwardButton == view) {
                fastForward();
            } else if (rewindButton == view && currentTimeline != null) {
                rewind();
            } else if (playButton == view) {
                if (isVideoReset()) {
                    onResetClick();
                } else {
                    player.setPlayWhenReady(!player.getPlayWhenReady());
                }
            }
            hideDeferred();
        }

    }
}