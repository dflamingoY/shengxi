package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import org.xiaoxingqi.shengxi.utils.ColorUtils


class LinearScrollView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    @Volatile
    private var childHeight = 0

    override fun addView(child: View?) {
        super.addView(child)
        if (childHeight == 0 && null != child) {
            getChildHeight(child)
        } else {
            if (childHeight * childCount > measuredHeight) {
                removeViewAt(0)
            }
        }
    }

    /**
     * 获取子View 的高度, 只运行一次
     */
    @Synchronized
    private fun getChildHeight(child: View) {
        child.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                child.viewTreeObserver.removeOnGlobalLayoutListener(this)
                childHeight = child.height
            }
        })
    }

    override fun onViewAdded(child: View?) {
        if (childHeight > 0)
            if (childCount > measuredHeight.toFloat() / childHeight / 2)
                for (childIndex in 0 until childCount) {
                    val alpha = (childIndex / (measuredHeight.toFloat() / childHeight) * 128 + 0.5).toInt() + 127
                    (getChildAt(childIndex) as TextView).let {
                        it.setTextColor(ColorUtils.colorEmbedAlpha(alpha, it.textColors.defaultColor))
                    }
                }
    }

}