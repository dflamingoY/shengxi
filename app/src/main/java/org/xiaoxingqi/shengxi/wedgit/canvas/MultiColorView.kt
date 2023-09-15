package org.xiaoxingqi.shengxi.wedgit.canvas

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatSupportable

class MultiColorView(context: Context, attrs: AttributeSet?) : View(context, attrs), SkinCompatSupportable {


    private val paint = Paint()
    private lateinit var shader: Shader
    private var paddingColor = Color.WHITE
    private var layerColor = Color.WHITE
    private var paddingColorId = 0
    private var layerColorId = 0
    private var shadowColor = Color.WHITE
    private var shadowColorId = 0

    /**
     * 解析id
     */
    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.CircleColorSelectorView)
        paddingColor = array.getColor(R.styleable.CircleColorSelectorView_circle_padding_color, paddingColor)
        layerColor = array.getColor(R.styleable.CircleColorSelectorView_circle_layer_color, layerColor)
        shadowColor = array.getColor(R.styleable.CircleColorSelectorView_circle_shadow_color, shadowColor)
        if (array.hasValue(R.styleable.CircleColorSelectorView_circle_padding_color)) {
            paddingColorId = array.getResourceId(R.styleable.CircleColorSelectorView_circle_padding_color, 0)
        }
        if (array.hasValue(R.styleable.CircleColorSelectorView_circle_layer_color)) {
            layerColorId = array.getResourceId(R.styleable.CircleColorSelectorView_circle_layer_color, 0)
        }
        if (array.hasValue(R.styleable.CircleColorSelectorView_circle_shadow_color)) {
            shadowColorId = array.getResourceId(R.styleable.CircleColorSelectorView_circle_shadow_color, 0)
        }
        array.recycle()
        paint.isDither = true
        paint.isAntiAlias = true
        SkinCompatResources.init(getContext())
        applySkin()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        shader = SweepGradient(w / 2f, h / 2f, intArrayOf(
                Color.parseColor("#FFDDFA6A")
                , Color.parseColor("#FF9ED061")
                , Color.parseColor("#FF03C7FF")
                , Color.parseColor("#FF4481FD")
                , Color.parseColor("#FF8A4DFE")
                , Color.parseColor("#FFC933FF")
                , Color.parseColor("#FFE83784")
                , Color.parseColor("#FFFE6059")
                , Color.parseColor("#FFFF6353")
                , Color.parseColor("#FFFF8642")
                , Color.parseColor("#FFFEB443")
                , Color.parseColor("#FFFCCC41")
                , Color.parseColor("#FFFCF96A")
                , Color.parseColor("#FFEAF060")
        ), null)
        paint.shader = shader
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        paint.color = shadowColor
        paint.style = Paint.Style.FILL
        paint.setShadowLayer(1f, 1f, 1f, shadowColor)
        canvas?.drawCircle(measuredWidth / 2f, measuredHeight / 2f, measuredWidth / 2f - AppTools.dp2px(context, 1), paint)
        paint.clearShadowLayer()
        paint.shader = shader
        canvas?.drawCircle(measuredWidth / 2f, measuredHeight / 2f, measuredWidth / 2f - AppTools.dp2px(context, 1), paint)
        if (isSelected) {
            paint.color = paddingColor
            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = AppTools.dp2px(context, 2).toFloat()
            canvas?.drawCircle(measuredWidth / 2f, measuredHeight / 2f, measuredWidth / 2f - AppTools.dp2px(context, 4), paint)
        }
        paint.color = layerColor
        paint.shader = null
        paint.style = Paint.Style.FILL
        canvas?.drawCircle(measuredWidth / 2f, measuredHeight / 2f, measuredWidth / 2f - AppTools.dp2px(context, 1), paint)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        invalidate()
    }

    override fun applySkin() {
        try {
            val paddingColorList = SkinCompatResources.getInstance().getColorStateList(paddingColorId)
            val layerColorList = SkinCompatResources.getInstance().getColorStateList(layerColorId)
            val shadowColorList = SkinCompatResources.getInstance().getColorStateList(shadowColorId)
            paddingColor = paddingColorList.defaultColor
            layerColor = layerColorList.defaultColor
            shadowColor = shadowColorList.defaultColor
            invalidate()
        } catch (e: Exception) {

        }
    }
}