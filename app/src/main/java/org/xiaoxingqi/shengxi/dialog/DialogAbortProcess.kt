package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Process
import kotlinx.android.synthetic.main.dialog_hint_change_set.*
import org.xiaoxingqi.shengxi.R

class DialogAbortProcess(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_hint_change_set
    }

    override fun initView() {
        setCancelable(false)
        tv_content.text = "不开启存储权限或手机权限将会\n关闭声昔，确定不开启吗?"
        tv_Commit.text = "取消"
        tv_Cancel.text = "确定"
        tv_Commit.setOnClickListener { dismiss() }
        tv_Cancel.setOnClickListener {
            Process.killProcess(Process.myPid())
        }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }
}