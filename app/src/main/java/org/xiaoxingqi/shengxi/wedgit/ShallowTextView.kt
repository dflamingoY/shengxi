package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet

/**
 * TextView 做滚动的阴影
 */
class ShallowTextView(context: Context?, attrs: AttributeSet?) : AppCompatTextView(context, attrs) {

    private val paint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.FILL
        isAntiAlias = true
        isDither = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        /*
         *创建菱形框
         */



    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

    }

}