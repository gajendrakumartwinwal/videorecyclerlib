package com.example.kotlin.recyclerview

import android.support.v7.widget.RecyclerView
import android.view.View

abstract class BaseViewHolder<ITEM>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun onBindViewHolder(item: ITEM)

    fun onViewRecycled() {}
}
