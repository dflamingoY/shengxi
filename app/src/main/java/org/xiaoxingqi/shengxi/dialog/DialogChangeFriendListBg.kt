package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

/**
 * 更换好友卡背景
 */
class DialogChangeFriendListBg(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_report
    }

    override fun initView() {
        val tvChange = findViewById<TextView>(R.id.tv_Report)
        tvChange.text = context.resources.getString(R.string.string_change_friends_list_bg)
        tvChange.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        findViewById<View>(R.id.tv_Cancel).setOnClickListener {
            dismiss()
        }
        initSystem()
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogChangeFriendListBg {
        this.onClickListener = onClickListener
        return this
    }

}