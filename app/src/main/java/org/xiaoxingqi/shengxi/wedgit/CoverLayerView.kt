package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import org.xiaoxingqi.shengxi.R

class CoverLayerView : View {
    private val paint = Paint()
    private lateinit var region: Region
    private var scaleRate = 1f

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.CoverLayerView)
        scaleRate = array.getFloat(R.styleable.CoverLayerView_scale_rate, 1.0f)
        array.recycle()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.isFilterBitmap = true
        paint.color = Color.parseColor("#80000000")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        region = Region(0, 0, w, h)
        val op = Region()
        op.set(Rect(0, (h / 2f - w / 2f / scaleRate).toInt(), w, (h / 2f + w / 2f / scaleRate).toInt()))
        region.op(op, Region.Op.DIFFERENCE)
    }

    override fun onDraw(canvas: Canvas?) {
        val iterator = RegionIterator(region)
        val rect = Rect()
        while (iterator.next(rect)) {
            canvas?.drawRect(rect, paint)
        }
    }

    fun getRect(): Rect {
        return Rect(0, (height / 2f - width / 2f / scaleRate).toInt(), width, (height / 2f + width / 2f / scaleRate).toInt())
    }

    /**
     * 设置缩放率
     */
    fun setScaleRate(rate: Float) {
        scaleRate = rate
        region = Region(0, 0, width, height)
        val op = Region()
        op.set(Rect((width / 2f - width * scaleRate / 2).toInt(), (height / 2f - width / 2f).toInt(), (width / 2f + width * scaleRate / 2).toInt(), (height / 2f + width / 2f).toInt()))
        region.op(op, Region.Op.DIFFERENCE)
        invalidate()
    }
}