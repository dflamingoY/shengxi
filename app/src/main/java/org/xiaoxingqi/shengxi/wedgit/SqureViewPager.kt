package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet

class SqureViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}