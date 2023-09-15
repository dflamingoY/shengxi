package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.dialog_admin_report_alarm.*
import org.xiaoxingqi.shengxi.R

class AdminReportAlarmDialog(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    private var isAnonymus = "0"
    private var operatorType = 0//1 操作台词 otherwise 配音
    override fun getLayoutId(): Int {
        return R.layout.dialog_admin_report_alarm
    }

    override fun initView() {
        if (operatorType == 1) {
            tv_deleteDubbing.text = "删除台词(管理员)"
            tvPick.visibility = View.GONE
            tv_hide.text = "隐藏台词"
        }
        if (isAnonymus == "1") {
            tv_anonymous_user.visibility = View.VISIBLE
        }
        tv_Report.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tv_hide.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tv_deleteDubbing.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tv_anonymous_user.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tvPick.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        initSystem()
    }

    fun setAnonymous(isAnonymus: String): AdminReportAlarmDialog {
        this.isAnonymus = isAnonymus
        return this
    }

    fun setType(operatorType: Int): AdminReportAlarmDialog {
        this.operatorType = operatorType
        return this
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): AdminReportAlarmDialog {
        this.onClickListener = onClickListener
        return this
    }

}