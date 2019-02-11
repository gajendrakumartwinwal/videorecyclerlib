package com.video.controls.video.queue;

import com.video.controls.video.player.UserEventListener;
import com.video.controls.video.videoad.TOIVideoPlayerView;

import java.util.ArrayList;
import java.util.Collections;

public class VideoQueueManager<T extends Object> implements QueueInterface<T> {
    private int currentIndex = 0;
    private ArrayList<T> mVideoItems;
    private T mCurrentVideoItem;
    /**
     * added to provided releated videos support in queue
     */
    private T mOverrideCurrent;

    public VideoQueueManager(T current, ArrayList<T> items) {
        this.mCurrentVideoItem = current;
        this.mVideoItems = items;
        initItmes();
    }

    private void initItmes() {
        //Initialize current pager item base on position
        if (mCurrentVideoItem == null && mVideoItems != null) {
            mCurrentVideoItem = mVideoItems.get(0);
        }

        if (mVideoItems == null && mCurrentVideoItem != null) {
            mVideoItems = new ArrayList<>(Collections.singletonList(mCurrentVideoItem));
        }
    }

    @Override
    public T getCurrent() {
        return mOverrideCurrent != null ? mOverrideCurrent : mVideoItems.get(currentIndex);
    }

    @Override
    public T getNext() {
        if (isNext()) {
            currentIndex++;
            mOverrideCurrent = null;
            return getCurrent();
        }
        return null;
    }

    @Override
    public T getPrevious() {
        if (isPrevious()) {
            currentIndex--;
            mOverrideCurrent = null;
            return getCurrent();
        }
        return null;
    }

    @Override
    public void overrideCurrent(T item) {
        mOverrideCurrent = item;
    }

    @Override
    public void updateButtons(TOIVideoPlayerView toiVideoPlayerView) {
        toiVideoPlayerView.onUserEvent(UserEventListener.NEXT_STATE_CHANGED, isNext());
        toiVideoPlayerView.onUserEvent(UserEventListener.PREVIOUS_STATE_CHANGED, isPrevious());
    }

    @Override
    public boolean isNext() {
        return currentIndex < mVideoItems.size() - 1;
    }

    @Override
    public boolean isPrevious() {
        return currentIndex > 1;
    }
}
