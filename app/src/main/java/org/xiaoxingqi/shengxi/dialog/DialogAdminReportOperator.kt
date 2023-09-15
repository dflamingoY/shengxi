package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.dialog_report.*
import org.xiaoxingqi.shengxi.R

class DialogAdminReportOperator(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_report
    }

    override fun initView() {
        tv_AddBlack.visibility = View.VISIBLE
        tv_AddBlack.text = "警告"
        tv_Report.text = "封号"
        tv_report_normal.text = "忽略"
        tv_Cancel.setOnClickListener { dismiss() }
        tv_AddBlack.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tv_Report.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tv_report_normal.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        initSystem()
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogAdminReportOperator {
        this.onClickListener = onClickListener
        return this
    }

}