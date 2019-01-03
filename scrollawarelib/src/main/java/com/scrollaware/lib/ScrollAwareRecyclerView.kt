package com.scrollaware.lib

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.scrollaware.lib.interfaces.PercentageCalculator

/**
 * This class gives scroll callback to the viewholder class if viewholder extends {@link NestedScrollListener}
 */
class ScrollAwareRecyclerView : RecyclerView {
    private lateinit var percentageCalculator: PercentageCalculator

    constructor(context: Context) : super(context) {
        registerListener()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        registerListener()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        registerListener()
    }

    var scrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//            percentageCalculator.notifyOnScrollStateChanged(recyclerView, newState)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            percentageCalculator.notifyOnScrolled(recyclerView, dx, dy)
        }
    };

    /**
     * Register listener
     */
    private fun registerListener() {
        addOnScrollListener(scrollListener)
        percentageCalculator = PercentageCalculator()
    }



}