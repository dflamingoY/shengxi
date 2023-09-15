package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

class DialogLimitCanvas(context: Context) : BaseDialog(context) {
    private var dTime: Long = 0
    private var title: String? = null

    override fun getLayoutId(): Int {
        return R.layout.dialog_limit_canvas
    }

    override fun initView() {
        setCancelable(false)
        findViewById<View>(R.id.tv_close).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        var hour = 0
        var minutes: Int
        when {
            dTime < 60 -> minutes = 1
            dTime < 3600 -> minutes = (dTime / 60).toInt()
            else -> {
                hour = (dTime / 3600).toInt()
                minutes = (dTime.rem(3600) / 60).toInt()
            }
        }
        findViewById<TextView>(R.id.tv_match_hint).text = if (TextUtils.isEmpty(title))
            String.format(context.resources.getString(R.string.string_limit_canvas), hour, minutes)
        else {
            String.format(title!!, hour, minutes)
        }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    fun setLimitInfo(dTime: Long): DialogLimitCanvas {
        this.dTime = dTime
        return this
    }

    //string_limit_grafftit
    fun setTitle(title: String): DialogLimitCanvas {
        this.title = title
        return this
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListenern(onClickListener: View.OnClickListener): DialogLimitCanvas {
        this.onClickListener = onClickListener
        return this
    }


}