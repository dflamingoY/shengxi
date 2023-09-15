package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.util.AttributeSet
import kotlinx.android.synthetic.main.layout_personality_expend.view.*
import org.xiaoxingqi.shengxi.R

class PersonalityExpentLayout : BaseLayout {
    private var isCollsped = true
    override fun getLayoutId(): Int {
        return R.layout.layout_personality_expend
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        tv_More.setOnClickListener { v ->
            if (isCollsped) {//折叠状态
                tv_ShowText.maxLines = Integer.MAX_VALUE
                isCollsped = false
                tv_More.text = "收起"
            } else {
                tv_ShowText.maxLines = 2
                isCollsped = true
                tv_More.text = "展开阅读更多"
            }
            tv_ShowText.requestLayout()
            mListener?.expend(isCollsped)
        }
    }

    fun setText(text: String) {
        tv_ShowText.text = text
        tv_ShowText.maxLines = 2
        isCollsped = true
    }

    private var mListener: OnExpendListener? = null

    fun stOnExpendListener(listener: OnExpendListener) {
        mListener = listener
    }

    interface OnExpendListener {
        fun expend(isExpend: Boolean)
    }
}