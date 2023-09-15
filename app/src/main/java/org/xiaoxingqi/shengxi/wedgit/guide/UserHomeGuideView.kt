package org.xiaoxingqi.shengxi.wedgit.guide

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import kotlinx.android.synthetic.main.layout_user_home_guide.view.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.wedgit.BaseLayout


class UserHomeGuideView(context: Context, attrs: AttributeSet?) : BaseLayout(context, attrs) {

    override fun getLayoutId(): Int {
        return R.layout.layout_user_home_guide
    }

    init {
        val tv = TypedValue()
        context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
        val actionBarHeight = resources.getDimensionPixelSize(tv.resourceId)
        val layoutParams = relativeParams.layoutParams as LayoutParams
        layoutParams.topMargin = AppTools.getStatusBarHeight(context) + (actionBarHeight - AppTools.dp2px(context, 23)) / 2
        relativeParams.layoutParams = layoutParams
    }

}