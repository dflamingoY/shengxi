package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ValueAnimator
import org.xiaoxingqi.shengxi.model.UserDateListData
import org.xiaoxingqi.shengxi.wedgit.calendar.CalendarMonthView

/**
 * 覆盖在上层指定位置做水波纹动画
 */
class CoverWaveView(context: Context?, attrs: AttributeSet?) : View(context, attrs), CalendarMonthView.OnTouchPosition {
    //全局一个动画
    companion object {
        private var column = 0f
        private var row = 0f
        private var basicRaduis = 0f

        private val list = ArrayList<CoverWaveView>()
        private var animator: ValueAnimator? = null
        private fun add2List(view: CoverWaveView) {
            if (!list.contains(view))
                list.add(view)
        }

        private fun start() {
            if (null != animator) {
                animator?.end()
                animator?.cancel()
                animator = null
            }
            animator = ValueAnimator.ofInt(0, 255).setDuration(1200)
            animator?.addUpdateListener { anim ->
                list.forEach {
                    if (it.isAttachedToWindow)
                        it.setAnimatorUpdate(anim.animatedValue as Int)
                }
            }
            animator?.interpolator = LinearInterpolator()
            animator?.repeatCount = ValueAnimator.INFINITE
            animator?.start()
        }

        private fun end() {
            if (null != animator) {
                animator?.let {
                    it.end()
                    it.cancel()
                }
                animator = null
                list.forEach {
                    if (it.isAttachedToWindow)
                        it.invalidate()
                }
            }
        }
    }

    private var currentX = 0
    private var currentY = 0
    private var radius: Float = 0f
    private val paint = Paint().apply {
        color = Color.parseColor("#46cdcf")
        style = Paint.Style.FILL
        isDither = true
        isAntiAlias = true
        alpha = 120
    }
    private var data: UserDateListData? = null

    override fun onDraw(canvas: Canvas?) {
        /**
         * 绘制圆形
         */
        data?.let {
            if (it.isPlaying) {
                if (column != 0f && row != 0f) {
                    canvas?.drawCircle(column * (it.currentColumn + 0.5f), row * (it.currentRow + 0.5f), it.radius, paint)
                }
            }
        }
    }

    fun startAnim() {
        start()
    }

    fun endAnim() {
        end()
    }

    private fun addList(view: CoverWaveView) {
        add2List(view)
    }

    fun setAnimatorUpdate(value: Int) {
        data?.let {
            if (it.isPlaying) {
                paint.alpha = 255 - value
                it.radius = value / 255f * basicRaduis + basicRaduis / 2f
                invalidate()
            }
        }
    }

    /*
     * 几行 几列
     */
    override fun position(x: Int, y: Int) {
        //在此点做Anim
        currentX = x
        currentY = y
        data?.let {
            it.currentColumn = x
            it.currentRow = y
        }
        invalidate()
    }

    /*
        初始化列宽和高
     */
    override fun setRect(x: Int, y: Int) {
        column = x.toFloat()
        row = y.toFloat()
        basicRaduis = if (column > row) row / 2f
        else column / 2f
    }

    fun attachData(item: UserDateListData) {
        data = item
        //移除同一个Data 绑定的View
        var view: CoverWaveView? = null
        list.forEach {
            it.data?.let { bean ->
                if (bean.date == item.date) {
                    view = it
                }
            }
        }
        view?.let {
            list.remove(it)
        }
        addList(this)
    }


    /**
     * 停止所有动画
     */
    fun update() {
        end()
    }

}