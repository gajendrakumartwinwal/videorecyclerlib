package com.example.kotlin

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import com.example.kotlin.recyclerview.ItemList
import com.example.kotlin.recyclerview.MyRecyclerAdapter
import com.example.kotlin.recyclerview.VideoItemList
import com.example.kotlin.recyclerview.ViewTemplate
import com.example.kotlin.utils.URLs
import com.google.android.youtube.player.YouTubeBaseActivity
import java.util.*

class MainActivity : YouTubeBaseActivity() {

    private lateinit var mAdapter: MyRecyclerAdapter
    private lateinit var mRecyclerView: RecyclerView
    private val mLayoutManager = GridLayoutManager(this, 1)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mRecyclerView = findViewById(R.id.recycler_view)
        setRecyclerAdapter()
    }


    private lateinit var items: ArrayList<ItemList>

    private fun setRecyclerAdapter() {
        items = ArrayList()
        for (i in 0..50) {
            var itemList = VideoItemList(i, i, ViewTemplate.VIDEO)
            itemList.setYoutbeId(URLs.YOUTBE_IDS[i % URLs.YOUTBE_IDS.size])
            items.add(itemList)
        }

        mAdapter = MyRecyclerAdapter(this, items)
        mLayoutManager.isItemPrefetchEnabled = true
        mLayoutManager.recycleChildrenOnDetach = true


        mRecyclerView.setLayoutManager(mLayoutManager)
        mRecyclerView.setAdapter(mAdapter)

    }

}
