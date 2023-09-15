package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.widget.AppCompatImageView
import android.text.TextUtils
import android.util.AttributeSet
import skin.support.SkinCompatManager

class GreyImageView(context: Context?, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {
    init {
        if (!TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {
            drawable?.mutate()?.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
        }
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        if (!TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) {
            drawable?.mutate()?.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
        }
    }

}