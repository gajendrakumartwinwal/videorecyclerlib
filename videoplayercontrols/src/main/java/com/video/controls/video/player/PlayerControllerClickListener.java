package com.video.controls.video.player;

import android.view.View;

import com.video.controls.R;

import static com.video.controls.video.player.ControllerEventListener.BACK_ARROW;
import static com.video.controls.video.player.ControllerEventListener.FULLSCREEN;
import static com.video.controls.video.player.ControllerEventListener.NEXT_BUTTON;
import static com.video.controls.video.player.ControllerEventListener.PREVIOUS_BUTTON;
import static com.video.controls.video.player.ControllerEventListener.SETTING;
import static com.video.controls.video.player.ControllerEventListener.SHARE;

public class PlayerControllerClickListener implements View.OnClickListener {
    private ControllerEventListener mControllerEventListener;

    public PlayerControllerClickListener() {
    }

    public void setControllerEventListener(ControllerEventListener controllerEventListener) {
        this.mControllerEventListener = controllerEventListener;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.share || v.getId() == R.id.share_land) {
            mControllerEventListener.onEvent(SHARE, null);
        } else if (v.getId() == R.id.settings || v.getId() == R.id.settings_land) {
            mControllerEventListener.onEvent(SETTING, null);
        } else if (v.getId() == R.id.fullscreen || v.getId() == R.id.fullscreen_land) {
            mControllerEventListener.onEvent(FULLSCREEN, null);
        } else if (v.getId() == R.id.crossButton || v.getId() == R.id.crossButton_land) {
            mControllerEventListener.onEvent(BACK_ARROW, null);
        } else if (v.getAlpha() != 0 && (v.getId() == R.id.next1 || v.getId() == R.id.next1_land)) {
            mControllerEventListener.onEvent(NEXT_BUTTON, null);
        } else if (v.getAlpha() != 0 && (v.getId() == R.id.prev1 || v.getId() == R.id.prev1_land)) {
            mControllerEventListener.onEvent(PREVIOUS_BUTTON, null);
        }
    }
}
