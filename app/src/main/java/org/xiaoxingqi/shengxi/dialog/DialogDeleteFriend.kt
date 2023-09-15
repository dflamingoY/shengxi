package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

class DialogDeleteFriend(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_delete_friend
    }

    override fun initView() {
        findViewById<TextView>(R.id.tv_Commit).setOnClickListener { view ->
            onClickListener?.let {
                it.onClick(view)
            }
            dismiss()
        }
        findViewById<View>(R.id.tv_Cancel).setOnClickListener {
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
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogDeleteFriend {
        this.onClickListener = onClickListener
        return this
    }
}