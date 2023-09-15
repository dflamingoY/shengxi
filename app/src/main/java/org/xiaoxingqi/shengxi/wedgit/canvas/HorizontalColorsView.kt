package org.xiaoxingqi.shengxi.wedgit.canvas

import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import org.xiaoxingqi.shengxi.utils.AppTools

class HorizontalColorsView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint()
    private var mask: GradientDrawable? = null
    private val slidPaint = Paint()

    init {
        paint.isAntiAlias = true
        paint.isDither = true
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        slidPaint.isAntiAlias = true
        slidPaint.isDither = true
        slidPaint.color = Color.WHITE
        slidPaint.style = Paint.Style.FILL
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val gradient = LinearGradient(0f, 0f, w.toFloat(), h.toFloat(), buildHueColorArray(), null, Shader.TileMode.MIRROR)
        paint.shader = gradient
        mask = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, null)
        mask?.setColor(Color.BLACK)
        val radio = AppTools.dp2px(context, 11).toFloat()
        mask?.cornerRadii = floatArrayOf(radio, radio, radio, radio, 0f, 0f, 0f, 0f)
    }

    private fun buildHueColorArray(): IntArray {
        val hue = IntArray(361)
        var count = 0
        var i = hue.size - 1
        while (i >= 0) {
            hue[count] = Color.HSVToColor(floatArrayOf(i.toFloat(), 1f, 1f))
            i--
            count++
        }

        return hue
    }

    override fun onDraw(canvas: Canvas?) {
        if (mask != null) {
            canvas?.saveLayer(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), null)
            mask?.setBounds(0, 0, measuredWidth, measuredHeight)
            mask?.draw(canvas)
            canvas?.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paint)
            canvas?.restore()
        }
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas?.drawRoundRect(moveX - 10, 0f, moveX + 10, measuredHeight.toFloat(), 10f, 10f, slidPaint)
    }

    private var hValue = 0f
    private var sValue = 1f
    private var vValue = 1f
    private var alpha = 255
    private var moveX = 0f
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when {
            event?.action == MotionEvent.ACTION_DOWN -> {
                moveX = event?.x
                val hsv = floatArrayOf(0f, 0f, 1f)
                hValue = 360f - (moveX * 360f / measuredWidth)
                hsv[0] = hValue
                hsv[1] = 1f
                val hsvToColor = Color.HSVToColor(alpha, hsv)
                colorResultListener?.colorResult(hsvToColor)
            }
            event?.action == MotionEvent.ACTION_MOVE -> {
                val rawX = event?.x
                moveX = rawX
                val hsv = floatArrayOf(0f, 0f, 1f)
                hValue = 360f - (rawX * 360f / measuredWidth)
                hsv[0] = hValue
                hsv[1] = 1f
                val hsvToColor = Color.HSVToColor(alpha, hsv)
                colorResultListener?.colorResult(hsvToColor)
            }
        }
        invalidate()
        return true
    }

    /**
     * 合成颜色
     */
    fun getColor(alpha: Int): Int {
        this.alpha = alpha
        return Color.HSVToColor(alpha, floatArrayOf(hValue, sValue, vValue))
    }

    interface OnColorResultListener {
        fun colorResult(color: Int)
    }

    private var colorResultListener: OnColorResultListener? = null
    fun setOnColorResult(colorResultListener: OnColorResultListener) {
        this.colorResultListener = colorResultListener
    }
}