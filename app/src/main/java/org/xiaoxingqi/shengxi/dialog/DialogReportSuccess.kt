package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import kotlinx.android.synthetic.main.dialog_ping_bi_success.*
import kotlinx.android.synthetic.main.dialog_report_img.*
import org.xiaoxingqi.shengxi.R

/**
 * 普通举报,举报成功之后的弹窗
 */
class DialogReportSuccess(context: Context) : BaseDialog(context) {

    override fun getLayoutId(): Int {
//        return R.layout.dialog_ping_bi_success
        return R.layout.dialog_report_img
    }

    override fun initView() {
//        tv_Title.text = context.getString(R.string.string_reported)
//        tv_content.text = context.getString(R.string.string_reported_content)
//        tv_Commit.setOnClickListener {
//            dismiss()
//        }
        ivReportImg.setOnClickListener { dismiss() }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }
}