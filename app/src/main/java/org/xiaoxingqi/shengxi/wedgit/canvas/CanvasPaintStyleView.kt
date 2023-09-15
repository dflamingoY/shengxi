package org.xiaoxingqi.shengxi.wedgit.canvas

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CanvasPaintStyleView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint()

    init {
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        paint.isDither = true
        paint.style = Paint.Style.FILL
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas?) {
        paint.setShadowLayer(2f, 2f, 2f, Color.GRAY)
        canvas?.drawRoundRect(4f, 4f, measuredWidth.toFloat() - 4f, measuredHeight.toFloat() - 4f, 10f, 10f, paint)
        paint.clearShadowLayer()
        paint.maskFilter = null
        canvas?.drawRoundRect(4f, 4f, measuredWidth.toFloat() - 4f, measuredHeight.toFloat() - 4f, 10f, 10f, paint)
        canvas?.save()
        canvas?.rotate(45f)


        canvas?.restore()

    }
}