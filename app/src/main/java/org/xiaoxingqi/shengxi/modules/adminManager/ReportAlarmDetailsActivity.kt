package org.xiaoxingqi.shengxi.modules.adminManager

import android.view.View
import kotlinx.android.synthetic.main.activity_admin_report_details.btn_Back
import kotlinx.android.synthetic.main.activity_report_alarm_details.*
import kotlinx.android.synthetic.main.item_alarm_dub.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct

class ReportAlarmDetailsActivity : BaseAct() {
    override fun getLayoutId(): Int {
        return R.layout.activity_report_alarm_details
    }

    override fun initView() {
        linearOperate.visibility = View.GONE

    }

    override fun initData() {

    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        tvDelete.setOnClickListener {

        }
        tvHide.setOnClickListener {

        }
    }
}