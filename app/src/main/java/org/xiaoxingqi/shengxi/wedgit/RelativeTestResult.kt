package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.widget.RelativeLayout
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools

class RelativeTestResult : RelativeLayout {
    private val paint = Paint()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setWillNotDraw(false)
        setBackgroundResource(R.drawable.shape_test_result_bg)
        paint.isAntiAlias = true
        paint.isDither = true
    }

    override fun onDraw(canvas: Canvas?) {
        val path = Path()
        path.moveTo(AppTools.dp2px(context, 2).toFloat(), AppTools.dp2px(context, 20).toFloat())
        path.lineTo(AppTools.dp2px(context, 92).toFloat(), AppTools.dp2px(context, 20).toFloat())
        path.lineTo(AppTools.dp2px(context, 2).toFloat(), AppTools.dp2px(context, 83).toFloat())
        path.lineTo(AppTools.dp2px(context, 2).toFloat(), AppTools.dp2px(context, 20).toFloat())
        path.close()
        paint.color = (Color.parseColor("#ADF5EA"))
        paint.style = Paint.Style.FILL_AND_STROKE
        canvas?.drawPath(path, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = AppTools.dp2px(context, 4).toFloat()
        paint.color = Color.BLACK
        canvas?.drawPath(path, paint)
    }
}