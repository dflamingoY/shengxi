package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.view_alarm_set.view.*
import org.xiaoxingqi.shengxi.R

class ViewAlarmSetting(context: Context, attrs: AttributeSet?) : BaseLayout(context, attrs) {

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.ViewMoreGroupView)
        val title = array.getString(R.styleable.ViewMoreGroupView_title_name)
        val secondTitle = array.getString(R.styleable.ViewMoreGroupView_second_title)
        val isShowHint = array.getBoolean(R.styleable.ViewMoreGroupView_title_selected, false)
        array.recycle()
        if (!TextUtils.isEmpty(title))
            tvTitle.text = title
        if (!TextUtils.isEmpty(secondTitle))
            tvDesc.text = secondTitle
        tvIgnore.visibility = if (isShowHint) View.VISIBLE else View.GONE
    }

    override fun getLayoutId(): Int {
        return R.layout.view_alarm_set
    }

    override fun setOnClickListener(l: OnClickListener?) {
        tvSetting.setOnClickListener(l)
    }

}