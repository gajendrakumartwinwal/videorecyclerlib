package com.video.controls.video;


import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class VideoSurfaceView extends SurfaceView {

    /**
     * The surface view will not resize itself if the fractional difference
     * between its default aspect ratio and the aspect ratio of the video falls
     * below this threshold.
     */
    private static final float MAX_ASPECT_RATIO_DEFORMATION_PERCENT = 0.01f;

    /**
     * The ratio of the width and height of the video.
     */
    private float videoAspectRatio;

    /**
     * @param context The context (ex {@link android.app.Activity}) that created this object.
     */
    public VideoSurfaceView(Context context) {
        super(context);
    }

    /**
     * @param context The context (ex {@link android.app.Activity}) that created this object.
     * @param attrs   A set of attributes to configure the {@link SurfaceView}.
     */
    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Resize the view based on the width and height specifications.
     *
     * @param widthMeasureSpec  The specified width.
     * @param heightMeasureSpec The specified height.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (videoAspectRatio != 0) {
            float viewAspectRatio = (float) width / height;
            float aspectDeformation = videoAspectRatio / viewAspectRatio - 1;
            if (aspectDeformation > MAX_ASPECT_RATIO_DEFORMATION_PERCENT) {
                height = (int) (width / videoAspectRatio);
            } else if (aspectDeformation < -MAX_ASPECT_RATIO_DEFORMATION_PERCENT) {
                width = (int) (height * videoAspectRatio);
            }
        }
        setMeasuredDimension(width, height);
    }

    /**
     * Set the aspect ratio that this {@link VideoSurfaceView} should satisfy.
     *
     * @param widthHeightRatio The width to height ratio.
     */
    public void setVideoWidthHeightRatio(float widthHeightRatio) {
        if (this.videoAspectRatio != widthHeightRatio) {
            this.videoAspectRatio = widthHeightRatio;
            requestLayout();
        }
    }

}