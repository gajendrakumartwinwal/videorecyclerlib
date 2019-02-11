package com.example.kotlin.viewholders

import android.view.View
import android.widget.ProgressBar
import com.example.kotlin.R
import com.example.kotlin.recyclerview.BaseViewHolder
import com.example.kotlin.recyclerview.ItemList
import com.example.kotlin.recyclerview.VideoItemList
import com.scrollaware.lib.interfaces.NestedScrollListener
import com.video.controls.video.videoad.TOIVideoPlayerView

/**
 * R.layout.recycler_video_item
 */
class VideoPlayerViewHolder(itemView: View) : BaseViewHolder<ItemList>(itemView), NestedScrollListener{
    override fun onPreActive() {
        setViewsVisibility(2, View.VISIBLE)
    }


    var videoItemList: VideoItemList? = null

    override fun onActive() {
        setViewsVisibility(2, View.VISIBLE)
        playYoutubePlayerView(videoItemList!!.youtubeId!!)
    }

    override fun onDeative() {
        mToiVideoPlayerView.releasePlayer()
        setViewsVisibility(0, View.VISIBLE)
    }

    internal var mProgressBar: ProgressBar
    internal var mPlayIcon: View? = null
    var mToiVideoPlayerView: TOIVideoPlayerView

    init {
        mProgressBar = itemView.findViewById(R.id.progress_bar)
        mToiVideoPlayerView = itemView.findViewById(R.id.toivideoplayerview)
        mPlayIcon = itemView.findViewById(R.id.play_icon)
    }

    override fun onBindViewHolder(itemList: ItemList) {
        setViewsVisibility(0, View.VISIBLE)
        videoItemList = itemList as VideoItemList
        mToiVideoPlayerView.setTag(itemList.id)
    }

    private fun playYoutubePlayerView(youtubeId: String) {
//        if (youtubeplayerView == null)
        mToiVideoPlayerView.requestYoutubeVideoPlay("aEM2kOrrNJI")
    }

    fun setViewsVisibility(type: Int, visiblity: Int) {
        if (type == 0) {
            mPlayIcon!!.visibility = visiblity
        } else {
            mPlayIcon!!.visibility = View.GONE
        }
        if (type == 1) {
            mToiVideoPlayerView.visibility = visiblity
        } else {
            mToiVideoPlayerView.visibility = View.GONE
        }
        if (type == 2) {
            mProgressBar.visibility = visiblity
        } else {
            mProgressBar.visibility = View.GONE
        }
    }
}