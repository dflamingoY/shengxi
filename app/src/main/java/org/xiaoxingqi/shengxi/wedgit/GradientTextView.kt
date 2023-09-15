package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Shader
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import org.xiaoxingqi.shengxi.R

class GradientTextView : AppCompatTextView {
    private var startColor: Int = 0
    private var endColor = 0
    private var centerColor = 0

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.GradientTextView)
        startColor = array.getColor(R.styleable.GradientTextView_startColor, 0)
        endColor = array.getColor(R.styleable.GradientTextView_endColor, 0)
        centerColor = array.getColor(R.styleable.GradientTextView_centerColor, 0)
        array.recycle()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val linear = LinearGradient(0f, 0f,
                width.toFloat(), height.toFloat(),
                intArrayOf(startColor, centerColor, endColor), null, Shader.TileMode.CLAMP)
        paint.shader = linear
    }
}