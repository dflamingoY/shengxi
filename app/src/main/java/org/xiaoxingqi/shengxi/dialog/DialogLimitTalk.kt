package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

/**
 * 限制回声聊天
 */
class DialogLimitTalk(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_limit
    }

    override fun initView() {
        setCancelable(false)
        findViewById<TextView>(R.id.tv_limit_1).text = context.resources.getString(R.string.string_limit_2)
        findViewById<View>(R.id.tv_cancel).setOnClickListener { dismiss() }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }
}