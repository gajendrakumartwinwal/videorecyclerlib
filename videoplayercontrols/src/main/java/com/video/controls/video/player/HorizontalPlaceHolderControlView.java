package com.video.controls.video.player;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.video.controls.R;


public class HorizontalPlaceHolderControlView extends PlaceHolderControlView {

    private TextView mTitle;

    public HorizontalPlaceHolderControlView(Context context) {
        super(context);
    }

    public HorizontalPlaceHolderControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTitle = (TextView) findViewById(R.id.video_title_land);
    }

    public void setTitle(String title) {
        if (mTitle != null) {
            mTitle.setVisibility(View.VISIBLE);
            mTitle.setText(title);
//            FontUtil.setFonts(mContext, mTitle, FontUtil.FontFamily.ROBOTO_MEDIUM);
        }
    }

    @Override
    protected void initViews() {
        topControl = findViewById(R.id.top_control_land);
        bottomControl = findViewById(R.id.bottom_control_land);
        centerControl = findViewById(R.id.centerControl_land);
        time = (TextView) findViewById(R.id.time_land);
        timeCurrent = (TextView) findViewById(R.id.time_current_land);
        progressBar = (SeekBar) findViewById(R.id.mediacontroller_progress_land);
        playButton = (ImageButton) findViewById(R.id.play_land);
        initPlayerControllerView();
    }

    @Override
    protected int getNextButtonId() {
        return R.id.next1_land;
    }

    @Override
    protected int getPreviousButtonId() {
        return R.id.prev1_land;
    }

    @Override
    protected int getCrossButtonId() {
        return R.id.crossButton_land;
    }

    @Override
    protected int getShareButtonId() {
        return R.id.share_land;
    }

    @Override
    protected int getSettingButtonId() {
        return R.id.settings_land;
    }

    @Override
    protected int getFullScreenButtonId() {
        return R.id.fullscreen_land;
    }

    @Override
    protected boolean isOrientationMatch() {
        return !super.isOrientationMatch();
    }

    @Override
    protected int getLayoutID() {
        return R.layout.video_player_horizontal_control;
    }
}
