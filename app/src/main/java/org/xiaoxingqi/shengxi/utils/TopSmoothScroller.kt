package org.xiaoxingqi.shengxi.utils

import android.content.Context
import android.support.v7.widget.LinearSmoothScroller

class TopSmoothScroller(context: Context?) : LinearSmoothScroller(context) {

    override fun getHorizontalSnapPreference(): Int {
        return SNAP_TO_START
    }

    override fun getVerticalSnapPreference(): Int {
        return SNAP_TO_START
    }

}