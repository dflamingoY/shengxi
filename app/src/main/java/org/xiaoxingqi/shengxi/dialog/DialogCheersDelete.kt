package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.dialog_delete_cheers.*
import org.xiaoxingqi.shengxi.R

class DialogCheersDelete(context: Context) : BaseDialog(context) {
    private var title: String? = null
    override fun getLayoutId(): Int {
        return R.layout.dialog_delete_cheers
    }

    override fun initView() {
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }
        tvCommit.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tvCancel.setOnClickListener { dismiss() }
        fillWidth()
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogCheersDelete {
        this.onClickListener = onClickListener
        return this
    }

    fun setTitle(title: String): DialogCheersDelete {
        this.title = title
        return this
    }
}