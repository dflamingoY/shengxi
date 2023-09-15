package org.xiaoxingqi.zxing.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import org.xiaoxingqi.shengxi.R
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatSupportable

class GroupChildView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs), SkinCompatSupportable {
    private var childWidth = 0
    private var childHeight = 0
    private val paint = Paint()
    private var colorId = 0
    private var paintColor = Color.parseColor("#4d333333")

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.GroupChildView)
        colorId = array.getResourceId(R.styleable.GroupChildView_layer_color, 0)
        array.recycle()
        setWillNotDraw(false)
        SkinCompatResources.init(getContext())
        applySkin()
        paint.color = paintColor
        paint.isDither = true
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
//        paint.alpha = 77
    }

    override fun applySkin() {
        try {
            paintColor = SkinCompatResources.getInstance().getColorStateList(colorId).defaultColor
        } catch (e: Exception) {
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val childAt = getChildAt(0)
        childAt?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                childAt.viewTreeObserver.removeOnGlobalLayoutListener(this)
                childWidth = childAt.width
                childHeight = childAt.height
                postInvalidate()
            }
        })
    }

    //draw bg
    override fun onDraw(canvas: Canvas?) {
        if (childWidth == 0 || childHeight == 0) {
            return
        }
        val region = Region(0, 0, measuredWidth, measuredHeight)
        val region1 = Region((measuredWidth - childWidth) / 2, (measuredHeight - childHeight) / 2, (measuredWidth - childWidth) / 2 + childWidth, childHeight + (measuredHeight - childHeight) / 2)
        region.op(region1, Region.Op.DIFFERENCE)
        var rect = Rect()
        val iterator = RegionIterator(region)
        while (iterator.next(rect)) {
            canvas?.drawRect(rect, paint)
        }
    }

}