package com.example.kotlin.recyclerview

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import com.example.kotlin.R
import com.example.kotlin.utils.URLs
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment


class VideoItemList(parent: Int, id: Int, @ViewTemplate type: Int) : ItemList(parent, id, type),
    YouTubePlayer.OnInitializedListener {
    override fun onInitializationSuccess(p0: YouTubePlayer.Provider?, p1: YouTubePlayer?, p2: Boolean) {
        youTubePlayer = p1
        youTubePlayer?.loadVideo(youtubeId)
    }

    override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
        //To change body of created functions use File | Settings | File Templates.
    }

    var youtubeId: String? = null
    val ID_PREFIX: String = "PREFIX-"
    var youTubePlayer: YouTubePlayer? = null

    private var youTubePlayerFragment: YouTubePlayerSupportFragment? = null

    fun setYoutbeId(id: String) {
        youtubeId = id
    }

    fun getYoutbeId(): String? {
        return youtubeId
    }

    public fun active(context: Context, viewGroup: ViewGroup) {
        youTubePlayerFragment?.let {
            (context as AppCompatActivity).supportFragmentManager
                .beginTransaction()
                .remove(it)
                .commit()

            (context as AppCompatActivity).supportFragmentManager
                .executePendingTransactions();
        };


        if (youTubePlayerFragment == null)
            youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance()

        youTubePlayerFragment?.let {
            (context as AppCompatActivity).supportFragmentManager
                .beginTransaction()
                .add(viewGroup.id, it)
                .commit()
            it.initialize(URLs.YOUTUBE_API_KEY, this)
        };
    }

    public fun deactive(context: Context) {
        if (youTubePlayer != null) youTubePlayer!!.release()
        youTubePlayerFragment?.let {
            (context as AppCompatActivity).supportFragmentManager
                .beginTransaction()
                .remove(it)
                .commit()
            (context as AppCompatActivity).supportFragmentManager
                .executePendingTransactions();
            youTubePlayerFragment = null;
            youTubePlayer = null
        };

    }


}
