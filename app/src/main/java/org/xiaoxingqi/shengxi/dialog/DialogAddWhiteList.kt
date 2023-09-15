package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

/**
 * 设置中将用户加入白名单
 */
class DialogAddWhiteList(context: Context) : BaseDialog(context) {

    override fun getLayoutId(): Int {
        return R.layout.dialog_hint_change_set
    }

    override fun initView() {
        findViewById<TextView>(R.id.tv_content).text = context.resources.getString(R.string.string_add_white_list_desc)
        findViewById<TextView>(R.id.tv_Cancel).apply {
            text = "取消"
        }.setOnClickListener {
            dismiss()
        }
        findViewById<View>(R.id.tv_Commit).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogAddWhiteList {
        this.onClickListener = onClickListener
        return this
    }

}