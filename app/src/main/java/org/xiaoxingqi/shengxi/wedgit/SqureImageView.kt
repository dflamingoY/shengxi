package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet

class SqureImageView(context: Context?, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}