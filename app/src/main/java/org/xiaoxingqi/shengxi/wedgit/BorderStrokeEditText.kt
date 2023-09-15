package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.*
import android.os.Build
import android.support.v7.widget.AppCompatEditText
import android.text.TextUtils
import android.util.AttributeSet
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatSupportable

/**
 * 边框颜色, 文字颜色
 */
class BorderStrokeEditText(context: Context?, attrs: AttributeSet?) : AppCompatEditText(context, attrs), SkinCompatSupportable {
    private var paint = Paint()
    private var strokeWidth = 58f
    private var margin = 24f
    private var radius = 10f
    private var strokeColor = 0//边框颜色
    private var strokeId = 0//边框的资源id
    private var colorResourceId = 0//默认颜色

    init {
        val array = context?.obtainStyledAttributes(attrs, R.styleable.BorderStrokeEditText)
        strokeColor = array?.getColor(R.styleable.BorderStrokeEditText_strokeColor, 0)!!
        strokeId = array.getResourceId(R.styleable.BorderStrokeEditText_strokeColor, 0)
        array?.recycle()
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SkinTextAppearance)
        if (typedArray.hasValue(R.styleable.SkinTextAppearance_android_textColor)) {
            colorResourceId = typedArray.getResourceId(R.styleable.SkinTextAppearance_android_textColor, 0)
        }
        typedArray.recycle()
        SkinCompatResources.init(getContext())
        applySkin()
        paint.isDither = true
        paint.isAntiAlias = true
        strokeWidth = AppTools.dp2px(context, 58).toFloat()
        margin = AppTools.dp2px(context, 24).toFloat()
        radius = AppTools.dp2px(context, 10).toFloat()
    }

    override fun onDraw(canvas: Canvas?) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = AppTools.dp2px(context, 1).toFloat()
        paint.color = strokeColor
        for (a in 0 until 2) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas?.drawRoundRect(measuredWidth / 2f - strokeWidth - margin / 2f + a * (strokeWidth + margin), AppTools.dp2px(context, 10).toFloat(), measuredWidth / 2f - margin / 2f + a * (strokeWidth + margin), strokeWidth + AppTools.dp2px(context, 10).toFloat(), radius, radius, paint)
            } else {
                canvas?.drawRoundRect(RectF(measuredWidth / 2f - strokeWidth - margin / 2f + a * (strokeWidth + margin), AppTools.dp2px(context, 10).toFloat(), measuredWidth / 2f - margin / 2f + a * (strokeWidth + margin), strokeWidth + AppTools.dp2px(context, 10).toFloat()), radius, radius, paint)
            }
        }
        if (!TextUtils.isEmpty(text.toString().trim())) {
            val textRect = Rect()
            paint.color = textColors.defaultColor
            paint.textSize = textSize
            paint.style = Paint.Style.FILL
            val trim = text.toString().trim()
            if (!TextUtils.isEmpty(trim)) {
                for (a in 0 until trim.length) {
                    val v = paint.measureText(text.toString().trim()[a].toString())
                    paint.getTextBounds(text.toString().trim(), a, a + 1, textRect)
                    canvas?.drawText(trim[a].toString(), measuredWidth / 2f - strokeWidth - margin / 2f + a * (strokeWidth + margin) + strokeWidth / 2 - v / 2, (strokeWidth / 2f + textRect.height() / 2 + AppTools.dp2px(context, 10).toFloat()-textRect.bottom), paint)
                }
            }
        }
    }

    override fun applySkin() {
        try {
            val color = SkinCompatResources.getInstance().getColorStateList(strokeId)
            strokeColor = color.defaultColor
            val color1 = SkinCompatResources.getInstance().getColorStateList(colorResourceId)
            setTextColor(color1.defaultColor)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        invalidate()
    }

}