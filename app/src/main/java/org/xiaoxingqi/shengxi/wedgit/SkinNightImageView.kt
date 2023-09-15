package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.widget.AppCompatImageView
import android.text.TextUtils
import android.util.AttributeSet
import skin.support.SkinCompatManager

class SkinNightImageView(context: Context?, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {

    init {
        if (!TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {
            drawable?.mutate()?.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (!TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {
            drawable?.mutate()?.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
        }
    }
}