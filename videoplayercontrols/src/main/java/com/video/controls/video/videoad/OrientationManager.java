package com.video.controls.video.videoad;


import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.view.OrientationEventListener;

public class OrientationManager extends OrientationEventListener implements LifecycleObserver {
    private int previousAngle;
    private int previousOrientation;
    private Context context;

    private boolean isOrientationUser = true;

    public OrientationManager(Context context) {
        super(context);
        this.context = context;
        ((AppCompatActivity) context).getLifecycle().addObserver(this);
    }

    public int getOrientation() {
        return previousOrientation;
    }

    public void setOrientation(int orientation) {
        this.previousOrientation = orientation;
    }


    @Override
    public void onOrientationChanged(int orientation) {
        if (orientation == -1)
            return;
        if (previousOrientation == 0) {
            previousOrientation = context.getResources().getConfiguration().orientation;
            onOrientationChange(previousOrientation);
        }
        if (previousOrientation == Configuration.ORIENTATION_LANDSCAPE &&
                ((previousAngle > 10 && orientation <= 10) ||
                        (previousAngle < 350 && previousAngle > 270 && orientation >= 350))) {
            onOrientationChange(Configuration.ORIENTATION_PORTRAIT);
            previousOrientation = Configuration.ORIENTATION_PORTRAIT;
        }

        if (previousOrientation == Configuration.ORIENTATION_PORTRAIT &&
                ((previousAngle < 90 && orientation >= 90 && orientation < 270) ||
                        (previousAngle > 280 && orientation <= 280 && orientation > 180))) {
            onOrientationChange(Configuration.ORIENTATION_LANDSCAPE);
            previousOrientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        previousAngle = orientation;
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onPause() {
        disable();
    }

    /**
     * must be called while user click on fullscreen toggle button
     */
    public void setFullScreenToggle() {
        enable();
        isOrientationUser = false;
        if (Util.isFullscreen(context)) {
            ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void onOrientationChange(int orientation) {
        if (!isOrientationUser) {
            isOrientationUser = true;
        } else {
            disable();
            ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }
    }

}