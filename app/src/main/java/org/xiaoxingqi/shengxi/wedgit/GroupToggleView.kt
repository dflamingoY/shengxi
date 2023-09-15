package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import android.widget.TextView
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatBackgroundHelper
import skin.support.widget.SkinCompatSupportable

/**
 * child 宽度 可滑动  动画过程中不可点击//点击之后做一个加载的动画,切加载过程中不可点击
 */
class GroupToggleView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs), SkinCompatSupportable {
    private var textArray: Array<CharSequence>? = null
    private var slideWidth = 0f
    private var textSize = 12f
    private val paint = Paint()
    private var selectedColor = Color.WHITE
    private var normalColor = Color.GRAY
    private var sliderColor = Color.WHITE
    private var marginX = 0f
    private var clickPosition = 0
    private val anim = SlideAnim()
    private var prePosition = 0
    private var mBackgroundTintHelper: SkinCompatBackgroundHelper? = null

    //颜色值的id
    private var selectedColorId = 0
    private var normalColorId = 0
    private var sliderColorId = 0

    override fun dispatchDraw(canvas: Canvas?) {
        canvas?.drawRoundRect(marginX, 0f, slideWidth + marginX, measuredHeight.toFloat(), AppTools.dp2px(context, 13).toFloat(), AppTools.dp2px(context, 13).toFloat(), paint)
        super.dispatchDraw(canvas)
    }

    private inner class SlideAnim : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            super.applyTransformation(interpolatedTime, t)
            //固定宽度 * 百分比
            marginX += (clickPosition * slideWidth - marginX) * interpolatedTime
            if (prePosition != clickPosition) {
                (getChildAt(clickPosition) as TextView).setTextColor(AppTools.getArgb(interpolatedTime, normalColor, selectedColor))
                (getChildAt(prePosition) as TextView).setTextColor(AppTools.getArgb(interpolatedTime, selectedColor, normalColor))
            }
            postInvalidate()
        }
    }

    private var onChildClickListener: OnChildClickListener? = null

    fun setOnChildClickListener(onChildClickListener: OnChildClickListener) {
        this.onChildClickListener = onChildClickListener
    }

    interface OnChildClickListener {
        fun onClick(position: Int, childView: View)
    }

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.GroupToggleView)
        textArray = array.getTextArray(R.styleable.GroupToggleView_group_title)
        slideWidth = array.getDimension(R.styleable.GroupToggleView_normal_width, 0f)
        textSize = array.getDimension(R.styleable.GroupToggleView_normal_size, 12f)
        selectedColor = array.getColor(R.styleable.GroupToggleView_selector_color, Color.WHITE)
        normalColor = array.getColor(R.styleable.GroupToggleView_normal_color, Color.GRAY)
        sliderColor = array.getColor(R.styleable.GroupToggleView_slider_color, sliderColor)
        if (array.hasValue(R.styleable.GroupToggleView_selector_color))
            selectedColorId = array.getResourceId(R.styleable.GroupToggleView_selector_color, 0)
        if (array.hasValue(R.styleable.GroupToggleView_normal_color))
            normalColorId = array.getResourceId(R.styleable.GroupToggleView_normal_color, 0)
        if (array.hasValue(R.styleable.GroupToggleView_slider_color))
            sliderColorId = array.getResourceId(R.styleable.GroupToggleView_slider_color, 0)
        array.recycle()
        SkinCompatResources.init(context)
        applySkin()
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper?.loadFromAttributes(attrs, 0)
        paint.isAntiAlias = true
        paint.isDither = true
        paint.style = Paint.Style.FILL
        paint.color = sliderColor
        orientation = HORIZONTAL
        removeAllViews()
        anim.duration = 320
        if (textArray != null) {
            val params = LayoutParams(slideWidth.toInt(), LayoutParams.MATCH_PARENT)
            textArray!!.forEach {
                val view = TextView(context)
                view.text = it
                view.setTypeface(view.typeface, Typeface.BOLD)
                view.gravity = Gravity.CENTER
                view.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                view.layoutParams = params
                view.setTextColor(if (childCount == 0)
                    selectedColor
                else
                    normalColor
                )
                addView(view)
                view.setOnClickListener { childView ->
                    //点击之后做一个加载的动画,切加载过程中不可点击
                    if (!isEnabled)
                        return@setOnClickListener
                    val currentPosition = indexOfChild(childView)
                    if (clickPosition == currentPosition)
                        return@setOnClickListener
//                    anim.duration = 320
                    clickPosition = currentPosition
                    view.startAnimation(anim)
                }
            }
        }
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                prePosition = clickPosition
                isEnabled = true
                checkoutSelected()
                onChildClickListener?.onClick(clickPosition, this@GroupToggleView)
            }

            override fun onAnimationStart(animation: Animation?) {
                isEnabled = false
            }
        })
    }

    /*修改选中的条件*/
    private fun checkoutSelected() {
        for (view in 0 until childCount) {
            (getChildAt(view) as TextView).setTextColor(if (clickPosition == view) {
                selectedColor
            } else {
                normalColor
            })
        }
    }

    override fun applySkin() {
        try {
            mBackgroundTintHelper?.applySkin()
            val normalColors = SkinCompatResources.getInstance().getColorStateList(normalColorId)
            val selectedColors = SkinCompatResources.getInstance().getColorStateList(selectedColorId)
            val sliderColors = SkinCompatResources.getInstance().getColorStateList(sliderColorId)
            selectedColor = selectedColors.defaultColor
            normalColor = normalColors.defaultColor
            sliderColor = sliderColors.defaultColor
//            invalidate()
        } catch (e: Exception) {
        }
    }

    override fun setBackgroundResource(resid: Int) {
        super.setBackgroundResource(resid)
        mBackgroundTintHelper?.onSetBackgroundResource(resid)
    }

    fun setCurrentPosition(position: Int) {
        marginX = position * slideWidth
        postInvalidate()
        //修改文字颜色
        (getChildAt(position) as TextView).setTextColor(selectedColor)
        clickPosition = position
        (getChildAt(prePosition) as TextView).setTextColor(normalColor)
        prePosition = clickPosition
    }
}