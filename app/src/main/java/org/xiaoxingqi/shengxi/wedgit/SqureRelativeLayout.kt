package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import org.xiaoxingqi.shengxi.R

class SqureRelativeLayout(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    private var scaleRate = 1f

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.CoverLayerView)
        scaleRate = array.getFloat(R.styleable.CoverLayerView_scale_rate, 1.0f)
        array.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val sizeW = MeasureSpec.getSize(widthMeasureSpec)
        val sizeH = (sizeW / scaleRate + 0.5f).toInt()
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(sizeH, MeasureSpec.EXACTLY))
    }

    fun setScaleRate(rate: Float) {
        scaleRate = rate
        invalidate()
    }

}