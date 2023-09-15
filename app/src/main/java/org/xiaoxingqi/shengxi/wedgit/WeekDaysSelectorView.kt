package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.util.AttributeSet
import kotlinx.android.synthetic.main.layout_week_days_selector.view.*
import org.xiaoxingqi.shengxi.R

class WeekDaysSelectorView(context: Context, attrs: AttributeSet?) : BaseLayout(context, attrs) {

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.EchoTypeView)
        val title = array.getString(R.styleable.EchoTypeView_echoTitle)
        array.recycle()
        tvWeekName.text = title
        customCheck.isEnabled = false
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_week_days_selector
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        customCheck.isSelected = selected
    }

    override fun isSelected(): Boolean {
        return customCheck.isSelected
    }

}