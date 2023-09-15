package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.dialog_cancel_edit_voice.*
import org.xiaoxingqi.shengxi.R

class DialogCancelEditVoice(context: Context) : BaseDialog(context) {

    private var title: String? = null
    private var msg: String? = null
    override fun getLayoutId(): Int {
        return R.layout.dialog_cancel_edit_voice
    }

    override fun initView() {
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }
        if (!TextUtils.isEmpty(msg)) {
            tvMsg.text = msg
        }
        tvCommit.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tvCancel.setOnClickListener { dismiss() }
        fillWidth()
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogCancelEditVoice {
        this.onClickListener = onClickListener
        return this
    }

    fun setTitle(title: String, msg: String): DialogCancelEditVoice {
        this.title = title
        this.msg = msg
        return this
    }
}