package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

/**
 * 流星电话等待配对的dialog
 */
class CallWaitDialog(context: Context) : BaseDialog(context) {
    private lateinit var tv_Tick: TextView
    private var type = 0  //0 电话 1 画板
    override fun getLayoutId(): Int {
        return R.layout.dialog_wait_call
    }

    override fun initView() {
        setCancelable(false)
        tv_Tick = findViewById(R.id.tv_Tick)
        val relativeWait = findViewById<View>(R.id.relative_Wait)
        relativeWait.setBackgroundResource(if (type == 0) R.drawable.shape_call_wait else R.drawable.shape_canvas_wait)
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    fun setCurrentType(type: Int): CallWaitDialog {
        this.type = type
        return this
    }

    /**
     *设置当前等待的时长
     */
    fun timeTick(tick: Int) {
        tv_Tick.text = "${context.resources.getString(R.string.string_voice_call_19)} ${tick}秒"
    }
}