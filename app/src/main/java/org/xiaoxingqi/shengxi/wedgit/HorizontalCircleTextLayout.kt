package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import kotlinx.android.synthetic.main.layout_circle_text.view.*
import org.xiaoxingqi.shengxi.R

class HorizontalCircleTextLayout(context: Context, attrs: AttributeSet?) : BaseLayout(context, attrs) {

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.ToggleLayoutView)
        val title = array.getString(R.styleable.ToggleLayoutView_typeName)
        val isSelected = array.getBoolean(R.styleable.ToggleLayoutView_toggleState, false)
        array.recycle()
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }
        viewStatus.isSelected = isSelected
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_circle_text
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        viewStatus.isSelected = isSelected
        tvTitle.isSelected = isSelected
    }

}