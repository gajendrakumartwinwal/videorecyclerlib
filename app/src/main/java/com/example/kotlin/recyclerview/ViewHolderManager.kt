package com.example.kotlin.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.kotlin.R
import com.example.kotlin.viewholders.SimpleViewHolder
import com.example.kotlin.viewholders.VideoPlayerViewHolder

class ViewHolderManager {

    fun getViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup, @ViewTemplate viewType: Int
    ): BaseViewHolder<ItemList>? {
        when (viewType) {
            ViewTemplate.SIMPLE -> {
                val viewTemp = inflater.inflate(R.layout.recycler_simple_item, parent, false)
                return SimpleViewHolder(viewTemp)
            }
            ViewTemplate.VIDEO -> {
                val viewTemp = inflater.inflate(R.layout.recycler_video_item, parent, false)
                return VideoPlayerViewHolder(viewTemp)
            }
        }
        return null
    }

}