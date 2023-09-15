package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import android.widget.TextView
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatSupportable

class TableScrollSkinPagerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), SkinCompatSupportable, View.OnClickListener {
    private var mTextArray: Array<CharSequence>? = null
    private var defalueColor = Color.parseColor("#888888")
    private var selectColor = Color.parseColor("#282828")
    private var mClickListener: OnClickListener? = null
    private var mStartX = 0
    private var mEndX = 0
    private var mStartFinalX = 0 //动画结束的点坐标
    private var slidWidth = 0 //滑块的宽度
    private val mSlideAnim: SlideAnim = SlideAnim()
    private val TIME = 200
    private var currentChild = 0
    private var isFirst = false
    private var titleMagin = 0f //中建的间隔
    private val DP = 1
    private var paint = Paint()
    private var normald = 0
    private var selectedId = 0

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.BaseTabTitleLayout)
        mTextArray = array.getTextArray(R.styleable.BaseTabTitleLayout_arrayText)
        defalueColor = array.getColor(R.styleable.BaseTabTitleLayout_title_color, defalueColor)
        selectColor = array.getColor(R.styleable.BaseTabTitleLayout_title_select_color, defalueColor)
        titleMagin = array.getDimension(R.styleable.BaseTabTitleLayout_title_margin, titleMagin)
        normald = array.getResourceId(R.styleable.BaseTabTitleLayout_title_color, 0)
        selectedId = array.getResourceId(R.styleable.BaseTabTitleLayout_title_select_color, 0)
        array.recycle()
        SkinCompatResources.init(context)
        applySkin()
        this.orientation = HORIZONTAL
        gravity = Gravity.CENTER
        removeAllViews()
        if (mTextArray != null && mTextArray!!.isNotEmpty()) {
            for (a in mTextArray!!.indices) {
                val textView = TextView(context)
                textView.gravity = Gravity.CENTER
                val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
                if (a != 0) {
                    params.marginStart = titleMagin.toInt()
                }
                textView.text = mTextArray!![a]
                textView.isSelected = false
                textView.setOnClickListener(this)
                this.addView(textView, params)
            }
        }
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (childCount > 0) {
                    val childAt = getChildAt(0)
                    val local = IntArray(2)
                    childAt.getLocationInWindow(local)
                    mStartX = local[0]
                    slidWidth = childAt.width
                    mStartFinalX = childAt.width
                    setCurrentSelect(0)
                }
            }
        })
    }

    /**
     * 设置当前选中
     *
     * @param position
     */
    fun setCurrentSelect(position: Int) {
        selectedTitle(getChildAt(position))
    }

    private fun selectedTitle(view: View) {
        clearAll()
        view.isSelected = true
        mStartFinalX = view.x.toInt() /*+ view.getWidth()*/
        currentChild = indexOfChild(view)
        mSlideAnim.duration = TIME.toLong()
        view.startAnimation(mSlideAnim)
        mSlideAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                checkoutSelected(view)
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })

    }

    private fun checkoutSelected(child: View) {
        for (view in 0 until childCount) {
            (getChildAt(view) as TextView).setTextColor(if (child == getChildAt(view)) {
                selectColor
            } else {
                defalueColor
            })
        }
    }

    private fun clearAll() {
        for (a in 0 until childCount) {
            val tv = getChildAt(a)
            tv.isSelected = false
        }
    }

    private fun onDefaultIndexDraw(canvas: Canvas, x1: Int, y1: Int, x2: Int, y2: Int): Boolean {
        paint.strokeWidth = AppTools.dp2px(context, 3).toFloat()
        paint.isAntiAlias = true
        paint.color = selectColor
        canvas.drawRoundRect(x1.toFloat(), y1 - AppTools.dp2px(context, 5).toFloat(), x2.toFloat(), y2 - AppTools.dp2px(context, 3).toFloat(), 5f, 5f, paint)
        return true
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        onDefaultIndexDraw(canvas!!, mStartX, measuredHeight - DP, mEndX, measuredHeight - DP)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!isFirst && childCount > 0) {
            val tv = getChildAt(0) as TextView
            tv.isSelected = false
            isFirst = true
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun applySkin() {
        try {
            defalueColor = SkinCompatResources.getInstance().getColorStateList(normald).defaultColor
            selectColor = SkinCompatResources.getInstance().getColorStateList(selectedId).defaultColor
            //修改当前选中
        } catch (e: Exception) {

        }
    }

    private inner class SlideAnim : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            super.applyTransformation(interpolatedTime, t)
            mStartX = (mStartX + (mStartFinalX - mStartX) * interpolatedTime).toInt()
            mEndX = mStartX + slidWidth
            postInvalidate()
        }
    }

    override fun onClick(v: View?) {
        if (v?.isSelected!!) {
            return
        }
        if (v is TextView) {
            selectedTitle(v)
            mClickListener?.onClick(v)
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        mClickListener = l
    }
}