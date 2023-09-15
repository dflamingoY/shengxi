package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import kotlinx.android.synthetic.main.dialog_alarm_warn.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.modules.listen.alarm.AlarmPermissionActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.SPUtils

class DialogAlarmWarn(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_alarm_warn
    }

    override fun initView() {
        setCancelable(false)
        tvSetPermission.setOnClickListener {
            context.startActivity(Intent(context, AlarmPermissionActivity::class.java))
            if (customCheck.isSelected) {
                SPUtils.setBoolean(context, IConstant.IGNORE_ALARM_HINT, false)
            }
            dismiss()
        }
        linear_no_again.setOnClickListener {
            customCheck.isSelected = !customCheck.isSelected
        }
        tv_Close.setOnClickListener {
            if (customCheck.isSelected) {
                SPUtils.setBoolean(context, IConstant.IGNORE_ALARM_HINT, false)
            }
            dismiss()
        }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }
}