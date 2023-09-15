package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.dialog_delete_wording.*
import org.xiaoxingqi.shengxi.R

class DialogDeleteCheers(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_delete_wording
    }

    override fun initView() {
        tv_Report.text = context.resources.getString(R.string.string_delete_cheers_hint_1)
        tvSecondTitle.text = context.resources.getString(R.string.string_delete_cheers_hint_2)
        tv_Delete.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tv_Cancel.setOnClickListener { dismiss() }
        initSystem()
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogDeleteCheers {
        this.onClickListener = onClickListener
        return this
    }

}