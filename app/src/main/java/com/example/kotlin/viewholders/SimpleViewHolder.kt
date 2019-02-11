package com.example.kotlin.viewholders

import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.kotlin.R
import com.example.kotlin.recyclerview.BaseViewHolder
import com.example.kotlin.recyclerview.ItemList
import com.scrollaware.lib.interfaces.NestedScrollListener

class SimpleViewHolder(itemView: View) : BaseViewHolder<ItemList>(itemView),
    NestedScrollListener {
    override fun onPreActive() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onActive() {
        Log.d("ACCCCC", "Active position - " + mHeadline.getTag())
        mGearIcon.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorAccent))
    }

    override fun onDeative() {
        mGearIcon.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
        Log.d("ACCCCC", "Deactive position - " + mHeadline.getTag())
    }

    internal var mHeadline: TextView
    internal var mGearIcon: ImageView

    init {
        mHeadline = itemView.findViewById(R.id.list_item_headline)
        mGearIcon = itemView.findViewById(R.id.iv_gear)
    }

    override fun onBindViewHolder(itemList: ItemList) {
        mHeadline.text = "Position: " + itemList.parent() + "th Item value is: " + itemList.id() + " TYPE - " +
                itemList.parent()
        mHeadline.setTag(itemList.id)
        mGearIcon.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
    }

}