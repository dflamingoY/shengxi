package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView
import org.xiaoxingqi.shengxi.R
import skin.support.widget.SkinCompatSupportable

class TabLayoutView : HorizontalScrollView, SkinCompatSupportable {
    override fun applySkin() {

    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TabLayoutView)
        typedArray.recycle()
    }

}