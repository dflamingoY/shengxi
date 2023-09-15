package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import skin.support.widget.SkinCompatTextView

class MarqueeTextView(context: Context?, attrs: AttributeSet?) : SkinCompatTextView(context, attrs) {
    private var dx: Float = 0f//每毫秒的进度
    private var textWidth = 0
    private var currentScrollX = 0
    private var isAutoGravity = false//是否计算文本长度,进行自动设置居中位置

    /* 每毫秒的文本进度
     */
    fun calcDx(time: Int) {
        val rect = Rect()
        paint.getTextBounds(text.toString(), 0, text.toString().length, rect)
        textWidth = rect.width()
        dx = if (textWidth < measuredWidth) {//如果文本内容的宽度不够, 则禁止滚动
            if (isAutoGravity)
                gravity = Gravity.CENTER
            0f
        } else {
            if (isAutoGravity)
                gravity = Gravity.CENTER_VERTICAL
            rect.width().toFloat() / time
        }
    }

    fun setAutoGravity(isAutoGravity: Boolean) {
        this.isAutoGravity = isAutoGravity
    }

    /**
     * 更新
     */
    fun update(time: Int) {
        if (dx == 0f)
            return
        currentScrollX = (dx * time /*+ 0.5f*/).toInt()
        /*if (currentScrollX <= textWidth) {
        } else {
            currentScrollX = 0
        }*/
        scrollTo(currentScrollX, 0)
    }

    fun reset() {
        if (dx != 0f)
            scrollTo(0, 0)
    }
}