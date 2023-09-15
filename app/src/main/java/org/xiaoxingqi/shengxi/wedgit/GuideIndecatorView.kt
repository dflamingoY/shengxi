package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import org.xiaoxingqi.shengxi.utils.AppTools

class GuideIndecatorView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    var paint: Paint = Paint()
    val radius = 6
    private var currentIndex = 0

    init {
        paint.isAntiAlias = true
        paint.isDither = true
        paint.style = Paint.Style.FILL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(AppTools.dp2px(context, 7 * radius), AppTools.dp2px(context, 6))
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        for (count in 0..3) {
            if (currentIndex == count) {
                paint.color = Color.parseColor("#66000000")
            } else {
                paint.color = Color.parseColor("#4d506E49")
            }
            canvas?.drawCircle((count * 2) * AppTools.dp2px(context, radius).toFloat() + AppTools.dp2px(context, radius).toFloat() / 2, measuredHeight.toFloat() / 2, AppTools.dp2px(context, radius).toFloat() / 2, paint)
        }

    }

    fun setCurrent(current: Int) {
        this.currentIndex = current
        invalidate()
    }

}