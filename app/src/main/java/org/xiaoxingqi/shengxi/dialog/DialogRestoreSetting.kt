package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.dialog_privacy.*
import org.xiaoxingqi.shengxi.R

class DialogRestoreSetting(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_privacy
    }

    override fun initView() {
        tvTitle.text = context.resources.getString(R.string.string_restory_settings1)
        tv_Desc.text = context.getString(R.string.string_restory_settings2)
        tv_Commit.text = context.resources.getString(R.string.string_restory_settings3)
        tv_Cancel.text = context.resources.getString(R.string.string_cancel)
        tv_Commit.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tv_Cancel.setOnClickListener {
            dismiss()
        }
        fillWidth()
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogRestoreSetting {
        this.onClickListener = onClickListener
        return this
    }

}