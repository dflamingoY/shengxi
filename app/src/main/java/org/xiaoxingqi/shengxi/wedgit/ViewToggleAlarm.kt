package org.xiaoxingqi.shengxi.wedgit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.Transformation
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.ColorUtils
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatBackgroundHelper
import skin.support.widget.SkinCompatSupportable

/**
 * 角标从做到右移动背景颜色做渐变动画
 */
class ViewToggleAlarm(context: Context, attrs: AttributeSet?) : View(context, attrs), View.OnClickListener, SkinCompatSupportable {
    private var onColor: Int = 0
    private var offColor: Int = 0
    private var onIndicatorColor = 0
    private var offIndicatorColor = 0
    private var offTextColor = 0
    private var onTextColor = 0
    private val anim = Anim()
    private var interpolated = 1f
    @Volatile
    private var inRunning = false
    private var isActionOn = false//是否执行开启的动作 存在提前性 , 最后一点时间
    private val mBackgroundTintHelper: SkinCompatBackgroundHelper?
    private var onColorId = 0
    private var offColorId = 0
    private var onIndicatorColorId = 0
    private var offIndicatorColorId = 0
    private var onTextColorId = 0
    private var offTextColorId = 0

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.ViewToggleAlarm)
        onColor = array.getColor(R.styleable.ViewToggleAlarm_toggleOnColor, Color.WHITE)
        offColor = array.getColor(R.styleable.ViewToggleAlarm_toggleOffColor, Color.WHITE)
        onIndicatorColor = array.getColor(R.styleable.ViewToggleAlarm_toggleOnIndicatorColor, Color.WHITE)
        offIndicatorColor = array.getColor(R.styleable.ViewToggleAlarm_toggleOffIndicatorColor, Color.WHITE)
        offTextColor = array.getColor(R.styleable.ViewToggleAlarm_toggleOffTextColor, Color.WHITE)
        onTextColor = array.getColor(R.styleable.ViewToggleAlarm_toggleOnTextColor, Color.WHITE)
        try {
            onColorId = array.getResourceId(R.styleable.ViewToggleAlarm_toggleOnColor, 0)
            offColorId = array.getResourceId(R.styleable.ViewToggleAlarm_toggleOffColor, 0)
            onIndicatorColorId = array.getResourceId(R.styleable.ViewToggleAlarm_toggleOnIndicatorColor, 0)
            offIndicatorColorId = array.getResourceId(R.styleable.ViewToggleAlarm_toggleOffIndicatorColor, 0)
            onTextColorId = array.getResourceId(R.styleable.ViewToggleAlarm_toggleOnTextColor, 0)
            offTextColorId = array.getResourceId(R.styleable.ViewToggleAlarm_toggleOffTextColor, 0)
        } catch (e: Exception) {
        }
        array.recycle()
        anim.duration = 220
        anim.interpolator = LinearInterpolator()
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                inRunning = false
                onChangeListener?.change(isActionOn)
            }

            override fun onAnimationStart(animation: Animation?) {
                inRunning = true
            }
        })
        SkinCompatResources.init(getContext())
        applySkin()
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper.loadFromAttributes(attrs, 0)
        setOnClickListener(this)
    }

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        isDither = true
        isAntiAlias = true
    }

    override fun setBackgroundResource(@DrawableRes resId: Int) {
        super.setBackgroundResource(resId)
        mBackgroundTintHelper?.onSetBackgroundResource(resId)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        //calc 当前差值的颜色进度
        paint.color = ColorUtils.getArgb(if (isActionOn) interpolated else {
            1 - interpolated
        }, offColor, onColor)
        canvas.drawRoundRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), AppTools.dp2px(context, 14).toFloat(), AppTools.dp2px(context, 14).toFloat(), paint)
        paint.textSize = AppTools.dp2px(context, 19).toFloat()
        val text = paint.measureText("O")
        val textRect = Rect()
        paint.getTextBounds("O", 0, 1, textRect)
        paint.color = ColorUtils.getArgb(if (isActionOn) interpolated else {
            1 - interpolated
        }, offTextColor, onColor)
        canvas.drawText("OFF", measuredWidth - (AppTools.dp2px(context, 10) + text * 3), measuredHeight / 2f + textRect.height() / 2f - textRect.bottom, paint)
        paint.color = ColorUtils.getArgb(if (isActionOn) interpolated else {
            1 - interpolated
        }, ColorUtils.and0x(offColor), onTextColor)
        canvas.drawText("ON", AppTools.dp2px(context, 19).toFloat(), measuredHeight / 2f + textRect.height() / 2f - textRect.bottom, paint)
        paint.color = ColorUtils.getArgb(if (isActionOn) interpolated else {
            1 - interpolated
        }, offIndicatorColor, onIndicatorColor)
        /**
         *计算当前indicate的颜色和坐标
         */
        val radius = measuredHeight / 2f
        canvas.drawCircle((measuredWidth - radius * 2) * if (isActionOn) interpolated else {
            1 - interpolated
        } + radius, radius, radius - AppTools.dp2px(context, 4), paint)
    }

    override fun applySkin() {
        try {
            if (onColorId != 0)
                onColor = SkinCompatResources.getInstance().getColorStateList(onColorId).defaultColor
            if (offColorId != 0)
                offColor = SkinCompatResources.getInstance().getColorStateList(offColorId).defaultColor
            if (onIndicatorColorId != 0)
                onIndicatorColor = SkinCompatResources.getInstance().getColorStateList(onIndicatorColorId).defaultColor
            if (offIndicatorColorId != 0)
                offIndicatorColor = SkinCompatResources.getInstance().getColorStateList(offIndicatorColorId).defaultColor
            if (onTextColorId != 0)
                onTextColor = SkinCompatResources.getInstance().getColorStateList(onTextColorId).defaultColor
            if (offTextColorId != 0)
                offTextColor = SkinCompatResources.getInstance().getColorStateList(offTextColorId).defaultColor
        } catch (e: Exception) {
        }
    }

    /**
     * 定时器
     */
    private inner class Anim : Animation() {

        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            if (interpolated != interpolatedTime) {
                interpolated = interpolatedTime
                invalidate()
            }
        }
    }

    fun setAnimStatus(isActed: Boolean) {
        isActionOn = isActed
        invalidate()
    }

    /**
     * 开始执行动画
     */
    private fun setAnimStart() {
        if (!inRunning) {
            isActionOn = !isActionOn
            startAnimation(anim)
        }
    }

    override fun onClick(v: View?) {
        setAnimStart()
    }

    private var onChangeListener: OnToggleChangeListener? = null

    fun setOnChangeListener(onChangeListener: OnToggleChangeListener) {
        this.onChangeListener = onChangeListener
    }

    interface OnToggleChangeListener {
        fun change(isOpen: Boolean)
    }
}