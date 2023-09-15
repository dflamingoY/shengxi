package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.dialog_delete_resource.*
import org.xiaoxingqi.shengxi.R

class DialogDeleteResource(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_delete_resource
    }

    override fun initView() {
        tvConfirm.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tvCancel.setOnClickListener { dismiss() }
        initSystem()
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogDeleteResource {
        this.onClickListener = onClickListener
        return this
    }
}