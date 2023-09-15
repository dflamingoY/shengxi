package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.nineoldandroids.animation.ValueAnimator
import org.xiaoxingqi.shengxi.utils.AppTools

class RevelView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint()

    private var radius = 30f
    private var animator: ValueAnimator? = null

    init {
        radius = AppTools.dp2px(context, 30).toFloat()
        paint.isDither = true
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#33ffffff")
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawCircle(width / 2f, height / 2f, radius, paint)
    }

    fun startView() {
        if (null != animator) {
            animator?.end()
            animator?.cancel()
            animator = null
        }
        animator = ValueAnimator.ofInt(0, 255).setDuration(1000)
        animator?.addUpdateListener {
            paint.alpha = 255 - it.animatedValue as Int
            radius = (it.animatedValue as Int) / 255f * AppTools.dp2px(context, 13).toFloat() + AppTools.dp2px(context, 30).toFloat()
            invalidate()
        }
        animator?.repeatCount = ValueAnimator.INFINITE
        animator?.start()
    }

    fun stopView() {
        if (null != animator) {
            animator?.end()
            animator?.cancel()
            animator = null
        }
    }
}