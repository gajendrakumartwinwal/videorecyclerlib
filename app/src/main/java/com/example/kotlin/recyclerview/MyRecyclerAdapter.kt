package com.example.kotlin.recyclerview

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import java.util.*

class MyRecyclerAdapter(mContext: Context, items: ArrayList<ItemList>) :
    RecyclerView.Adapter<BaseViewHolder<ItemList>>() {

    private val mContext: Context
    private var items: ArrayList<ItemList>? = null
    private val mInflator: LayoutInflater
    private var mViewHolderManager: ViewHolderManager


    init {
        this.mContext = mContext
        this.items = items
        this.mInflator = LayoutInflater.from(mContext)
        mViewHolderManager = ViewHolderManager()
        setHasStableIds(true)
    }

    fun getItems(): ArrayList<ItemList>? {
        return items
    }

    fun setItems(items: ArrayList<ItemList>) {
        this.items = items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<ItemList> {
        return mViewHolderManager.getViewHolder(mInflator, parent, viewType)!!
    }

    override fun getItemViewType(position: Int): Int {
        return items!![position].type
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ItemList>, position: Int) {
        holder.onBindViewHolder(items!![position])
    }

    override fun getItemCount(): Int {
        return items!!.size
    }

    override fun onViewRecycled(holder: BaseViewHolder<ItemList>) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()
    }


    override fun getItemId(position: Int): Long {
        return items!![position].id.toLong()
    }

}