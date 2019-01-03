package com.example.kotlin.viewholders

import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.example.kotlin.R
import com.example.kotlin.recyclerview.BaseViewHolder
import com.example.kotlin.recyclerview.ItemList
import com.example.kotlin.recyclerview.VideoItemList
import com.example.kotlin.utils.URLs
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView
import com.scrollaware.lib.interfaces.NestedScrollListener

/**
 * R.layout.recycler_video_item
 */
class VideoPlayerViewHolder(itemView: View) : BaseViewHolder<ItemList>(itemView), NestedScrollListener,
    YouTubePlayer.OnInitializedListener {
    var youTubePlayer: YouTubePlayer? = null

    override fun onInitializationSuccess(p0: YouTubePlayer.Provider?, player: YouTubePlayer?, wasRestored: Boolean) {
        youTubePlayer = player
        if (!wasRestored) {
            if (player != null) {
                // loadVideo() will auto play video
                // Use cueVideo() method, if you don't want to play it automatically
                player.loadVideo(videoItemList?.getYoutbeId())
                setViewsVisibility(1, View.VISIBLE)
//                 Hiding player controls
//                player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS)
            }
        }
    }

    override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
    }


    var youtubeplayerView: YouTubePlayerView? = null
    var videoItemList: VideoItemList? = null

    override fun onActive() {
        Log.d("DDDDD", "Active For position - " + (videoItemList?.id ?: ""))
        llVideoContaienr.setBackgroundColor(
            ContextCompat.getColor(
                itemView.context, R.color.primary_dark_material_light
            )
        )
        setViewsVisibility(2, View.VISIBLE)
        playYoutubePlayerView(videoItemList!!.youtubeId!!)
    }

    override fun onDeative() {
//        videoItemList?.deactive(itemView.context)
        Log.d("DDDDD", "Deactive For position - " + (videoItemList?.id ?: ""))
        llVideoContaienr.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
        youTubePlayer?.release()
        llVideoContaienr.removeAllViews()
        setViewsVisibility(0, View.VISIBLE)
    }

    internal var mProgressBar: ProgressBar
    internal var llVideoContaienr: FrameLayout
    internal var mPlayIcon: View? = null

    init {
        mProgressBar = itemView.findViewById(R.id.progress_bar)
        llVideoContaienr = itemView.findViewById(R.id.ll_container)
        mPlayIcon = itemView.findViewById(R.id.play_icon)
    }

    override fun onBindViewHolder(itemList: ItemList) {
        setViewsVisibility(0, View.VISIBLE)
        videoItemList = itemList as VideoItemList
        llVideoContaienr.setTag(itemList.id)
        llVideoContaienr.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
    }

    private fun playYoutubePlayerView(youtubeId: String) {
//        if (youtubeplayerView == null)
        youtubeplayerView = YouTubePlayerView(llVideoContaienr.context)
        llVideoContaienr.removeAllViews()
        llVideoContaienr.addView(youtubeplayerView)
        youtubeplayerView!!.initialize(URLs.YOUTUBE_API_KEY, this)
    }

    fun setViewsVisibility(type: Int, visiblity: Int) {
        if (type == 0) {
            mPlayIcon!!.visibility = visiblity
        } else {
            mPlayIcon!!.visibility = View.GONE
        }
        if (type == 1) {
            llVideoContaienr.visibility = visiblity
        } else {
            llVideoContaienr.visibility = View.GONE
        }
        if (type == 2) {
            mProgressBar.visibility = visiblity
        } else {
            mProgressBar.visibility = View.GONE
        }
    }
}