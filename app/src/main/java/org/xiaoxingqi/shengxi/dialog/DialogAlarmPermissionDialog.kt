package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import kotlinx.android.synthetic.main.dialog_alarm_permission.*
import org.xiaoxingqi.shengxi.R

class DialogAlarmPermissionDialog(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_alarm_permission
    }

    override fun initView() {
        ivClose.setOnClickListener {
            dismiss()
        }

        tvSetting.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogAlarmPermissionDialog {
        this.onClickListener = onClickListener
        return this
    }
}