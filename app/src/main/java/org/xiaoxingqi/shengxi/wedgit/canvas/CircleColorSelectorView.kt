package org.xiaoxingqi.shengxi.wedgit.canvas

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatSupportable

class CircleColorSelectorView : View, SkinCompatSupportable {

    private var circleColor = Color.BLACK
    private var circlePadding = 0f
    private var circlePaddingColor = Color.TRANSPARENT
    private var circleShadowColor = Color.TRANSPARENT
    private var circleLayerColor = Color.TRANSPARENT
    private var circlePaddingColorId = 0
    private var circleShallowColorId = 0
    private var circleLayerColorId = 0
    private var circleColorId = 0
    private val paint = Paint()

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.CircleColorSelectorView)
        circleColor = array.getColor(R.styleable.CircleColorSelectorView_circle_color, Color.BLACK)
        circlePadding = array.getDimension(R.styleable.CircleColorSelectorView_circle_padding, 0f)
        circlePaddingColor = array.getColor(R.styleable.CircleColorSelectorView_circle_padding_color, circlePaddingColor)
        circleShadowColor = array.getColor(R.styleable.CircleColorSelectorView_circle_shadow_color, circleShadowColor)
        circleLayerColor = array.getColor(R.styleable.CircleColorSelectorView_circle_layer_color, circleLayerColor)
        if (array.hasValue(R.styleable.CircleColorSelectorView_circle_shadow_color)) {
            circleShallowColorId = array.getResourceId(R.styleable.CircleColorSelectorView_circle_shadow_color, 0)
        }
        if (array.hasValue(R.styleable.CircleColorSelectorView_circle_padding_color)) {
            circlePaddingColorId = array.getResourceId(R.styleable.CircleColorSelectorView_circle_padding_color, 0)
        }
        if (array.hasValue(R.styleable.CircleColorSelectorView_circle_layer_color)) {
            circleLayerColorId = array.getResourceId(R.styleable.CircleColorSelectorView_circle_layer_color, 0)
        }
        if (array.hasValue(R.styleable.CircleColorSelectorView_circle_color)) {
            circleColorId = array.getResourceId(R.styleable.CircleColorSelectorView_circle_color, 0)
        }
        array.recycle()
        paint.color = circleColor
        paint.isAntiAlias = true
        paint.isDither = true
        paint.isFilterBitmap = true
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        SkinCompatResources.init(getContext())
        applySkin()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        paint.color = circleShadowColor
        paint.style = Paint.Style.FILL
        paint.setShadowLayer(1f, 1f, 1f, circleShadowColor)
        canvas?.drawCircle(measuredWidth / 2f, measuredHeight / 2f, measuredWidth / 2f - AppTools.dp2px(context, 1), paint)
        paint.clearShadowLayer()
        paint.color = circleColor
        canvas?.drawCircle(measuredWidth / 2f, measuredHeight / 2f, measuredWidth / 2f - AppTools.dp2px(context, 1), paint)
        if (isSelected) {
            paint.color = circlePaddingColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = AppTools.dp2px(context, 2).toFloat()
            canvas?.drawCircle(measuredWidth / 2f, measuredHeight / 2f, measuredWidth / 2f - AppTools.dp2px(context, 4).toFloat(), paint)
        }
        paint.color = circleLayerColor
        paint.style = Paint.Style.FILL
        canvas?.drawCircle(measuredWidth / 2f, measuredHeight / 2f, measuredWidth / 2f - AppTools.dp2px(context, 1), paint)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        invalidate()
    }

    fun setColor(color: Int) {
        circleColor = color
        invalidate()
    }

    fun getColor(): Int {
        return circleColor
    }

    override fun applySkin() {
        try {
            if (circleColorId != 0) {
                val circleColorList = SkinCompatResources.getInstance().getColorStateList(circleColorId)
                circleColor = circleColorList.defaultColor
            }
            if (circlePaddingColorId != 0) {
                val color = SkinCompatResources.getInstance().getColorStateList(circlePaddingColorId)
                circlePaddingColor = color.defaultColor
            }
            if (circleShallowColorId != 0) {
                val shadowColor = SkinCompatResources.getInstance().getColorStateList(circleShallowColorId)
                circleShadowColor = shadowColor.defaultColor
            }
            if (circleLayerColorId != 0) {
                val layerColor = SkinCompatResources.getInstance().getColorStateList(circleLayerColorId)
                circleLayerColor = layerColor.defaultColor
            }
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}