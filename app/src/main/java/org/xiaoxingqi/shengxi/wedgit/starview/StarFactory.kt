package org.xiaoxingqi.shengxi.wedgit.starview

import android.graphics.Canvas
import android.graphics.PointF
import android.view.animation.LinearInterpolator
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ValueAnimator
import org.xiaoxingqi.shengxi.utils.StarTypeEvaluator

class StarFactory {

    private var animator: ValueAnimator? = null
    /**
     * 绘制的对象
     */
    private var bean: StarBean

    constructor(bean1: StarBean) {
        this.bean = bean1
        bean.paint.isDither = true
        bean.paint.isAntiAlias = true
        bean.paint.isFilterBitmap = true
        animator = ValueAnimator.ofObject(StarTypeEvaluator(), PointF(bean1!!.startX, bean1.startY), PointF(bean1.endX, bean1.endY))
        animator?.duration = bean1.duration
        animator?.interpolator = LinearInterpolator()
        animator?.start()
        animator?.repeatCount = ValueAnimator.INFINITE
        animator?.repeatMode = ValueAnimator.RESTART
        animator?.addUpdateListener {
            val value = it.animatedValue as PointF
            var alpha = (255 * (1 - it.animatedFraction)).toInt()
            if (alpha <= 127) {
                alpha = 127
            }
            bean.alpha = alpha
            bean.currentX = value.x
            bean.currentY = value.y + bean.offsetY!!
        }
        animator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationRepeat(animation: Animator?) {
                if (bean.random?.nextBoolean()!!) {
                    bean.offsetY = bean.random.nextInt((bean.wHeight - bean.startY).toInt()).toFloat()
                } else {
                    bean.offsetY = -bean.random.nextInt((bean.startX).toInt()).toFloat()
                }
                bean.scale = (bean.random?.nextInt(5)!! + 5) / 10f
            }
        })
    }

    fun draw(canvas: Canvas?) {
        try {
            bean.paint!!.alpha = bean.alpha
            bean.matrix.postScale(bean.scale, bean.scale, bean.bitmap!!.width * 1f / 2, bean.bitmap!!.height * 1f / 2)
            bean.matrix.postTranslate(bean.currentX, bean.currentY)
            canvas?.drawBitmap(bean.bitmap, bean.matrix, bean.paint)
            bean.matrix.reset()
        } catch (e: Exception) {
        }
    }
}