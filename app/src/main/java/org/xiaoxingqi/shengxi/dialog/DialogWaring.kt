package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import org.xiaoxingqi.shengxi.R

class DialogWaring(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_waring
    }

    override fun initView() {
        setCancelable(false)
        findViewById<View>(R.id.tv_Commit).setOnClickListener { view ->
            onClickListener?.let {
                it.onClick(view)
            }
            dismiss()
        }
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogWaring {
        this.onClickListener = onClickListener
        return this
    }

}