package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.wedgit.helper.NavigatorHelper
import org.xiaoxingqi.shengxi.wedgit.helper.NavigatorHelper.OnNavigatorScrollListener
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatBackgroundHelper
import skin.support.widget.SkinCompatSupportable

class PagerSlidingBlock(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs), SkinCompatSupportable, OnNavigatorScrollListener {

    private var viewPager: ViewPager? = null
    private var slideWidth = 0f
    private var textSize = 12f
    private var selectedColor = Color.WHITE
    private var normalColor = Color.GRAY
    private var sliderColor = Color.WHITE

    //颜色值的id
    private var selectedColorId = 0
    private var normalColorId = 0
    private var sliderColorId = 0
    private var mBackgroundTintHelper: SkinCompatBackgroundHelper? = null

    private var marginX = 0f
    private var paint: Paint
    private var navigatorHelper: NavigatorHelper? = null
    private var arrays: Array<CharSequence>? = null
    private var stateSelectedColor = 0
    private var stateNormalColor = 0
    private var stateSelectedColorId = -1
    private var stateNormalColorId = -1

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.GroupToggleView)
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
        if (array.hasValue(R.styleable.GroupToggleView_stateGroupSelectedTextColor)) {
            stateSelectedColor = array.getColor(R.styleable.GroupToggleView_stateGroupSelectedTextColor, 0)
            stateSelectedColorId = array.getResourceId(R.styleable.GroupToggleView_stateGroupSelectedTextColor, -1)
        }
        if (array.hasValue(R.styleable.GroupToggleView_stateGroupNormalTextColor)) {
            stateNormalColor = array.getColor(R.styleable.GroupToggleView_stateGroupNormalTextColor, 0)
            stateNormalColorId = array.getResourceId(R.styleable.GroupToggleView_stateGroupNormalTextColor, -1)
        }
        arrays = array.getTextArray(R.styleable.GroupToggleView_group_title)
        array.recycle()
        paint = Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.FILL
            color = sliderColor
        }
        SkinCompatResources.init(context)
        applySkin()
        arrays?.let {
            val params = LayoutParams(slideWidth.toInt(), LayoutParams.MATCH_PARENT)
            it.forEach { char ->
                val view = TextView(context)
                view.text = char
                view.setTypeface(view.typeface, Typeface.NORMAL)
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
                    viewPager?.currentItem = indexOfChild(childView)
                }
            }
        }
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper?.loadFromAttributes(attrs, 0)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        canvas?.drawRoundRect(marginX, 0f, slideWidth + marginX, measuredHeight.toFloat(), AppTools.dp2px(context, 13).toFloat(), AppTools.dp2px(context, 13).toFloat(), paint)
        super.dispatchDraw(canvas)
    }

    fun setViewPager(viewPager: ViewPager) {
        if (viewPager.adapter == null)
            throw IllegalStateException("ViewPager Adapter required")
        this.viewPager = viewPager
        navigatorHelper = NavigatorHelper()
        navigatorHelper?.totalCount = viewPager.adapter!!.count
        navigatorHelper?.setNavigatorScrollListener(this)
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                navigatorHelper?.onPageSelected(position)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                navigatorHelper?.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageScrollStateChanged(state: Int) {
                navigatorHelper?.onPageScrollStateChanged(state)
            }
        })
    }

    override fun onDeselected(index: Int, totalCount: Int) {

    }

    override fun onSelected(index: Int, totalCount: Int) {

    }

    override fun onEnter(index: Int, totalCount: Int, enterPercent: Float, leftToRight: Boolean) {
        marginX = if (leftToRight) {
            (index * slideWidth) * enterPercent
        } else {
            if (index == 1) {
                slideWidth
            } else
                ((1 + index) * slideWidth) * (1 - enterPercent)
        }
        (getChildAt(index) as TextView).setTextColor(AppTools.getArgb(enterPercent, if (isSelected) stateNormalColor else normalColor, if (isSelected) stateSelectedColor else selectedColor))
        invalidate()
    }

    override fun onLeave(index: Int, totalCount: Int, leavePercent: Float, leftToRight: Boolean) {
        (getChildAt(index) as TextView).setTextColor(AppTools.getArgb(1 - leavePercent, if (isSelected) stateNormalColor else normalColor, if (isSelected) stateSelectedColor else selectedColor))
    }

    override fun setBackgroundResource(resid: Int) {
        super.setBackgroundResource(resid)
        mBackgroundTintHelper?.onSetBackgroundResource(resid)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        updateTextColor()
    }

    private fun updateTextColor() {
        //改变当前文本的颜色
        (0 until childCount).forEach {
            (getChildAt(it) as TextView).setTextColor(if (viewPager?.currentItem == it)
                if (isSelected) stateSelectedColor else selectedColor
            else
                if (isSelected) stateNormalColor else normalColor)
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
            if (stateSelectedColorId != -1)
                stateSelectedColor = SkinCompatResources.getInstance().getColorStateList(stateSelectedColorId).defaultColor
            if (stateNormalColorId != -1)
                stateNormalColor = SkinCompatResources.getInstance().getColorStateList(stateNormalColorId).defaultColor
            //改变当前文本的颜色
            updateTextColor()
            paint.color = sliderColor
            invalidate()
        } catch (e: Exception) {
        }
    }

}