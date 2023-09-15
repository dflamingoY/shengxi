package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import skin.support.widget.SkinCompatImageView

class MultipleImageView(context: Context?, attrs: AttributeSet?) : SkinCompatImageView(context, attrs) {
    private var url: String? = null

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    fun setLoadUrl(url: String) {

    }
}