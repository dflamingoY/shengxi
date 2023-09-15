package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import org.xiaoxingqi.shengxi.R

/**
 * 通话结束之后,提示是否添加好友的提示 ,强制关闭不提示添加好友,
 */
class DialogAddFriends(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_add_friends
    }

    override fun initView() {
        setCancelable(false)
        findViewById<View>(R.id.tv_Cancel).setOnClickListener {
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
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogAddFriends {
        this.onClickListener = onClickListener
        return this
    }
}