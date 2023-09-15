package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

/**
 * 是否断开魔法画板和语音通话的弹窗
 */
class DialogDismissConnect(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_cance_echoes
    }

    override fun initView() {
        setCancelable(false)
        findViewById<TextView>(R.id.tv_Title).text = context.resources.getString(R.string.string_connect_1)
        val commit = findViewById<TextView>(R.id.tv_Commit)
        commit.text = context.resources.getString(R.string.string_close_call)
        val cancel = findViewById<TextView>(R.id.tv_Cancel)
        cancel.text = context.resources.getString(R.string.string_not_close_call)
        cancel.setOnClickListener {
            dismiss()
        }
        commit.setOnClickListener {
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
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogDismissConnect {
        this.onClickListener = onClickListener
        return this
    }


}