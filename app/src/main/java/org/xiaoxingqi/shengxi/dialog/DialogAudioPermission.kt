package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import kotlinx.android.synthetic.main.dialog_permission_audio.*
import org.xiaoxingqi.shengxi.R

class DialogAudioPermission(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_permission_audio
    }

    override fun initView() {
        setCancelable(false)
        relativeRequestPermission.setOnClickListener {
            onClickListener?.onClick(it)
        }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogAudioPermission {
        this.onClickListener = onClickListener
        return this
    }

}