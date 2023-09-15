package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.nineoldandroids.animation.ValueAnimator
import org.xiaoxingqi.shengxi.R
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatSupportable

class CheersProgress @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), SkinCompatSupportable {
    private var circleColorId = 0
    private var circleLayerColorId = 0
    private var circleBottomColor = 0
    private var circleLayerColor = 0
    private var cheerWidth = 0f
    private val bottomPaint = Paint()
    private val layerPaint = Paint()
    private var anim: ValueAnimator? = null
    private var currentValue = 0f

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.CheersProgress)
        circleColorId = array.getResourceId(R.styleable.CheersProgress_cheersBackground, 0)
        circleLayerColorId = array.getResourceId(R.styleable.CheersProgress_cheerRotation, 0)
        cheerWidth = array.getDimension(R.styleable.CheersProgress_cheerWidth, 10f)
        array.recycle()
        SkinCompatResources.init(context)
        applySkin()
        bottomPaint.color = circleBottomColor
        bottomPaint.init()
        layerPaint.color = circleLayerColor
        layerPaint.init()
        layerPaint.strokeCap = Paint.Cap.ROUND
        startLoading()
    }

    private fun Paint.init() {
        isDither = true
        isAntiAlias = true
        strokeWidth = cheerWidth
        style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        canvas?.drawArc(10f, 10f, width - 10f, height - 10f, 0f, 360f, false, bottomPaint)
        canvas?.drawArc(10f, 10f, width - 10f, height - 10f, currentValue, 225f, false, layerPaint)
    }

    fun startLoading() {
        anim = ValueAnimator.ofFloat(0f, 360f).setDuration(2500)
        anim?.addUpdateListener {
            currentValue = it.animatedValue as Float
            invalidate()
        }
        anim?.repeatMode = ValueAnimator.RESTART
        anim?.repeatCount = ValueAnimator.INFINITE
        anim?.interpolator = LinearInterpolator()
        anim?.start()
    }

    fun stop() {
        anim?.let {
            it.cancel()
            anim = null
        }
    }

    override fun applySkin() {
        try {
            circleBottomColor = SkinCompatResources.getInstance().getColorStateList(circleColorId).defaultColor
            circleLayerColor = SkinCompatResources.getInstance().getColorStateList(circleLayerColorId).defaultColor
        } catch (e: Exception) {
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }
}