package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.util.AttributeSet
import kotlinx.android.synthetic.main.layout_score_music.view.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.impl.OnScoreClickListener

class SelectorScoreMusicView : BaseLayout {

    override fun getLayoutId(): Int {
        return R.layout.layout_score_music
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    private fun initView() {
        linearGood.setOnClickListener {
            linearGood.isSelected = true
            linearNormal.isSelected = false
            linearBad.isSelected = false
            onScoreListener?.onClick()
        }
        linearNormal.setOnClickListener {
            linearGood.isSelected = false
            linearNormal.isSelected = true
            linearBad.isSelected = false
            onScoreListener?.onClick()
        }
        linearBad.setOnClickListener {
            linearGood.isSelected = false
            linearNormal.isSelected = false
            linearBad.isSelected = true
            onScoreListener?.onClick()
        }
    }

    fun getScore(): String {
        if (linearGood.isSelected) {
            return "200"
        }
        if (linearNormal.isSelected) {
            return "150"
        }
        if (linearBad.isSelected) {
            return "100"
        }
        return ""
    }

    private var onScoreListener: OnScoreClickListener? = null

    fun setOnScoreClickListener(listener: OnScoreClickListener) {
        onScoreListener = listener
    }
}