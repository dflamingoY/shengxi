package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.layout_alarm_item_click.view.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.impl.OnAlarmItemClickListener

class AlarmItemClickView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseLayout(context, attrs, defStyleAttr) {
    private var onItemClickListener: OnAlarmItemClickListener? = null
    override fun getLayoutId(): Int {
        return R.layout.layout_alarm_item_click
    }

    init {
        val arrays = context.obtainStyledAttributes(attrs, R.styleable.AlarmItemClickView)
        val isVisibleAll = arrays.getBoolean(R.styleable.AlarmItemClickView_all_tab_visible, false)
        arrays.recycle()
        if (isVisibleAll) {
            skinTextAll.visibility = View.GONE
        } else
            skinTextAll.isSelected = true
        skinTextAll.setOnClickListener {
            checkFocus(it, 0)
        }
        skinTextDubbing.setOnClickListener {
            checkFocus(it, 1)
        }
        skinTextFree.setOnClickListener {
            checkFocus(it, 2)
        }
    }

    private fun checkFocus(view: View, type: Int = 0) {
        if (view.isSelected) return
        clearAllFocus()
        view.isSelected = true
        onItemClickListener?.itemClick(type)
    }

    private fun clearAllFocus() {
        skinTextAll.isSelected = false
        skinTextDubbing.isSelected = false
        skinTextFree.isSelected = false
    }

    fun setOnItemClick(listener: OnAlarmItemClickListener) {
        onItemClickListener = listener
    }

    fun hideAllTab() {
        skinTextAll.visibility = View.GONE
    }

    fun setOnItemSelected(type: Int) {
        //选中状态不做处理
        if (type == 0 && skinTextAll.isSelected)
            return
        if (type == 1 && skinTextDubbing.isSelected)
            return
        if (type == 2 && skinTextFree.isSelected)
            return
        clearAllFocus()
        when (type) {
            0 -> skinTextAll.isSelected = true
            1 -> skinTextDubbing.isSelected = true
            2 -> skinTextFree.isSelected = true
        }
    }

    //获取当前选中的item 名称 只需要求交配和freestyle两种风格
    fun getSelectedTabType(): String? {
        return if (skinTextDubbing.isSelected) "1" else if (skinTextFree.isSelected) "2" else if (skinTextAll.isSelected) "0" else null
    }

}