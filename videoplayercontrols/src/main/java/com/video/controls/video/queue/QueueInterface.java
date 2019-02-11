package com.video.controls.video.queue;

import com.video.controls.video.videoad.TOIVideoPlayerView;

public interface QueueInterface<T> {
    T getCurrent();

    T getNext();

    T getPrevious();

    /**
     * Override current with new item temporarly it will be cleared once user click on next or previous button
     */
    void overrideCurrent(T item);

    /**
     * Update next prevous buttons
     */
    void updateButtons(TOIVideoPlayerView toiVideoPlayerView);

    boolean isNext();

    boolean isPrevious();
}
