package com.scrollaware.lib.interfaces

import android.graphics.Rect
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.scrollaware.lib.R
import kotlin.math.absoluteValue

/**
 * This class is responsible for giving callback to viewholder about their active and deactive call back based on below observations
 * 1. View which is very near to the center of recyclerview we will make active
 * 2. View which is active earlier will make it deactive as active position is taken by other view
 * 3. Specify delay in video play if required: This delay will wait untill some time and if viewholder is recycled then it will discard else
 * it will play the video
 */
class PercentageCalculator {

    private var DELAY_VALUE: Long = 1000;

    private var mLayoutManager: GridLayoutManager? = null
    /**
     * Currently active viewholder position
     * -1 if no viewholder is active currently else positive
     */
    private var activeViewHolderPosition: Int = -1
    /**
     * Last updated viewholder position from center
     */
    private var activeViewHolderDistanceFromCenter: Int = Int.MAX_VALUE

    private var mRecyclerRect: Rect? = null
    /**
     * This rect is used to get viewholder coordinates
     */
    private var viewHolderRect: Rect = Rect()


    private var scrollState: Int = -1

    /**
     * Notify scroll value in x and y direction to the viewholder using  NestedScrollListener.onScrolled
     */
    internal fun notifyOnScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

        if (mRecyclerRect == null) {
            mRecyclerRect = Rect(0, recyclerView.top, 0, recyclerView.bottom)
        }

        if (mLayoutManager == null) mLayoutManager = recyclerView.layoutManager as GridLayoutManager?
        activeViewHolder(recyclerView)
    }


    /**
     * Activate and deactivate viewholder based on how much far they are from the center of recyclerview
     */
    private fun activeViewHolder(recyclerView: RecyclerView) {
        var firstVisiblePosition = mLayoutManager!!.findFirstCompletelyVisibleItemPosition()
        var lastVisiblePosition = mLayoutManager!!.findLastCompletelyVisibleItemPosition()

        var tempactive: Int = -1
        for (i in firstVisiblePosition..lastVisiblePosition) {
            if (i == firstVisiblePosition) {
                activeViewHolderDistanceFromCenter = getDistanceFromCenter(recyclerView, i)
                tempactive = i
                continue
            }

            var distanceFromCenter: Int = getDistanceFromCenter(recyclerView, i)
            if (activeViewHolderDistanceFromCenter > distanceFromCenter) {
                activeViewHolderDistanceFromCenter = distanceFromCenter
                tempactive = i
            }
        }

        if (tempactive == -1 || tempactive == activeViewHolderPosition) {//Active view is not changed yet no notify event is required

        } else {
            //Notify deactive
            if (activeViewHolderPosition > -1)
                notifyDeactiveView(
                    recyclerView.findViewHolderForAdapterPosition(activeViewHolderPosition!!),
                    activeViewHolderPosition
                )

            //Notify active viewholder
            activeViewHolderPosition = tempactive
            notifyActiveView(
                (recyclerView.findViewHolderForAdapterPosition(activeViewHolderPosition!!)),
                activeViewHolderPosition
            )
        }
    }

    /**
     * Delayed notify handled here if DELAY_VALUE is greater than 0 else notify imidiately
     */
    private fun notifyDeactiveView(viewHolder: RecyclerView.ViewHolder?, position: Int) {
        if (DELAY_VALUE > 0) viewHolder?.itemView?.setTag(R.string.recycler_viewholder_key, "D-" + position)
        (viewHolder as NestedScrollListener).onDeative()
    }


    /**
     * Delayed notify handled here if DELAY_VALUE is greater than 0 else notify imidiately
     */
    private fun notifyActiveView(viewHolder: RecyclerView.ViewHolder?, position: Int) {
        (viewHolder as NestedScrollListener).onPreActive()
        if (DELAY_VALUE > 0) {
            viewHolder!!.itemView.setTag(R.string.recycler_viewholder_key, "A-" + position)
            viewHolder.itemView.postDelayed({
                //If viewholder is not deactivated or time is past {@link DELAY_VALUE}
                if (viewHolder.itemView.getTag(R.string.recycler_viewholder_key).equals("A-" + position)) {
                    (viewHolder as NestedScrollListener).onActive()
                }
            }, DELAY_VALUE)
        } else {
            (viewHolder as NestedScrollListener).onActive()
        }
    }

    private fun getDistanceFromCenter(recyclerView: RecyclerView, position: Int): Int {
        var viewholder: RecyclerView.ViewHolder? = recyclerView.findViewHolderForAdapterPosition(position)
        if (viewholder == null) return Int.MAX_VALUE;

        //Get viewholder rect
        viewHolderRect.top = viewholder.itemView.top
        viewHolderRect.bottom = viewholder.itemView.bottom
        return (mRecyclerRect!!.centerY() - viewHolderRect.centerY()).absoluteValue
    }

    /**
     * This method will be called whenever scroll state changed
     * and it is used to detect scroll state and if user is in drag state call active imidiatly else wait for {@link DELAY_VALUE}
     */
    fun notifyOnScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        scrollState = newState
        Log.d("SCROLL_STATES", "STATE - " + newState);
    }
}