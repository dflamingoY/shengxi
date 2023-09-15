package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.support.v7.widget.AppCompatEditText
import android.text.TextUtils
import android.util.AttributeSet
import org.xiaoxingqi.shengxi.R

class CustomEditText : AppCompatEditText {
    private val paint = Paint()
    private var borderMargin = 0F
    private var count = 0
    private var radius = 0f
    private var borderWidth = 0F

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.CustomEditText)
        borderWidth = array.getDimension(R.styleable.CustomEditText_customWidth, 0f)
        borderMargin = array.getDimension(R.styleable.CustomEditText_customMargin, 0f)
        radius = array.getDimension(R.styleable.CustomEditText_customRadius, 0f)
        count = array.getInt(R.styleable.CustomEditText_customCount, 0)
        array.recycle()
        paint.isDither = true
        paint.isAntiAlias = true
        paint.color = Color.parseColor("#eeeeee")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mWidth = borderWidth * count + borderMargin * (count - 1)
        super.onMeasure(MeasureSpec.makeMeasureSpec(mWidth.toInt(), MeasureSpec.EXACTLY), heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = Color.parseColor("#eeeeee")
        paint.style = Paint.Style.FILL
        for (a in 0 until count) {
            canvas?.drawRoundRect((a * borderWidth + a * borderMargin), 0f, ((a + 1) * borderWidth + a * borderMargin), measuredHeight.toFloat(), radius, radius, paint)
        }
        paint.color = textColors.defaultColor
        paint.textSize = textSize
        paint.style = Paint.Style.FILL
        /**
         * 计算文字的宽高
         */
        val textRect = Rect()
        val v = paint.measureText("9")
        paint.getTextBounds("9", 0, 1, textRect)
        val trim = text.toString().trim { it <= ' ' }
        if (!TextUtils.isEmpty(trim)) {
            for (a in 0 until trim.length) {
                canvas?.drawText(trim[a].toString(), a * width + a * borderMargin + width / 2 - v / 2, (measuredHeight / 2 + textRect.height() / 2).toFloat(), paint)
            }
        }
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        invalidate()
    }

    /**
     * 設置过滤器
     */
    fun setFilter() {

    }

}