package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.Transformation
import org.xiaoxingqi.shengxi.R
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatSupportable

class CircleCountDown @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), SkinCompatSupportable {
    private var strokeWidth = 0f
    private var lowerLayerColor = 0
    private var lowerLayerColorId = 0
    private var topLayerColor = 0
    private var topLayerColorId = 0
    private val paint = Paint()
    private val grayPaint = Paint()
    private var isStop = false

    init {
        val arrays = context.obtainStyledAttributes(attrs, R.styleable.CircleCountDown)
        strokeWidth = arrays.getDimension(R.styleable.CircleCountDown_circleStrokeWidth, 0f)
        if (!arrays.hasValue(R.styleable.CircleCountDown_circleLowerColor) || !arrays.hasValue(R.styleable.CircleCountDown_circleTopLayerColor)) {
            throw IllegalArgumentException("布局文件必须填写背景色和进度条颜色")
        }
        lowerLayerColorId = arrays.getResourceId(R.styleable.CircleCountDown_circleLowerColor, 0)
        topLayerColorId = arrays.getResourceId(R.styleable.CircleCountDown_circleTopLayerColor, 0)
        arrays.recycle()
        applySkin()
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = topLayerColor
        paint.init()
        grayPaint.init()
        grayPaint.color = lowerLayerColor
    }

    fun Paint.init() {
        isDither = true
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = this@CircleCountDown.strokeWidth
    }

    private var currentValue = 0f
    override fun onDraw(canvas: Canvas?) {
        canvas?.drawArc(10f, 10f, width - 10f, height - 10f, 0f, 360f, false, grayPaint)
        canvas?.drawArc(10f, 10f, width - 10f, height - 10f, -90f, currentValue, false, paint)
    }

    fun start() {
        isStop = false
        clearAnimation()
        val innerAnim = InnerAnim()
        innerAnim.interpolator = LinearInterpolator()
        innerAnim.duration = 5000
        innerAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                if (!isStop)
                    animatorEndListener?.end()
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })
        innerAnim.cancel()
        startAnimation(innerAnim)
    }

    fun stop() {
        isStop = true
        clearAnimation()
        currentValue = 0f
        invalidate()
    }

    private inner class InnerAnim : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            animatorEndListener?.timeBeat(((1 - interpolatedTime) * 5000f / 1000 + 1).toInt())
            currentValue = interpolatedTime * 360
            invalidate()
        }
    }

    override fun applySkin() {
        try {
            lowerLayerColor = SkinCompatResources.getInstance().getColorStateList(lowerLayerColorId).defaultColor
            topLayerColor = SkinCompatResources.getInstance().getColorStateList(topLayerColorId).defaultColor
            grayPaint.color = lowerLayerColor
            paint.color = topLayerColor
            invalidate()
        } catch (e: Exception) {
        }
    }

    private var animatorEndListener: OnAnimatorEndListener? = null
    fun setOnAnimatorEndListener(animatorEndListener: OnAnimatorEndListener) {
        this.animatorEndListener = animatorEndListener
    }

    interface OnAnimatorEndListener {
        fun end()
        fun timeBeat(time: Int)
    }

}