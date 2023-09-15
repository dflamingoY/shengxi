package org.xiaoxingqi.shengxi.wedgit.starview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.nineoldandroids.animation.ValueAnimator
import org.xiaoxingqi.shengxi.R
import java.util.*

/**
 * 斜率  0.318
 */
class StarFlowView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val starId = R.mipmap.icon_star_anim
    private var bitmap: Bitmap? = null
    private val slop = 0.318f
    private val random = Random()
    private val list = arrayOfNulls<StarFactory>(4)

    init {
        bitmap = BitmapFactory.decodeResource(resources, starId)
        val animator = ValueAnimator.ofFloat(0f, 1f).setDuration(1000)
        animator.start()
        animator.addUpdateListener {
            invalidate()
        }
        animator.repeatCount = ValueAnimator.INFINITE
        animator.interpolator = LinearInterpolator()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        for (a in 0 until 4) {
            val bean = StarBean()
            bean.bitmap = bitmap
            bean.duration = 2000L + random.nextInt(10) * 120
            bean.startX = w.toFloat() + bitmap?.width!!
            bean.startY = random.nextFloat() * h / 4 * 3//随机高度
            bean.endX = (-bitmap!!.width).toFloat()
            bean.endY = bean.startY + (bean.startX - bean.endX) * slop
            bean.scale = (random.nextInt(5) + 5) / 10f
            bean.wHeight = h
            list[a] = StarFactory(bean)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        for (bean in list) {
            bean?.draw(canvas)
        }
    }
}