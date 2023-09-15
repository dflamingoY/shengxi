package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

class DialogDeleteChatComment(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    private var hintText: String? = null

    override fun getLayoutId(): Int {
        return R.layout.dialog_delete_chat_comment
    }

    override fun initView() {
        findViewById<View>(R.id.tv_Commit).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        if (!TextUtils.isEmpty(hintText)) {
            findViewById<TextView>(R.id.tv_Hint).text = hintText
        }
        findViewById<View>(R.id.tv_Cancel).setOnClickListener { dismiss() }
        window.setBackgroundDrawable(ColorDrawable(0))
        initSystem()
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogDeleteChatComment {
        this.onClickListener = onClickListener
        return this
    }

    fun setHint(hintText: String): DialogDeleteChatComment {
        this.hintText = hintText
        return this
    }
}