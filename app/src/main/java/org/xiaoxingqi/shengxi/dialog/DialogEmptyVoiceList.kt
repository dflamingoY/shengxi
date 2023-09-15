package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

class DialogEmptyVoiceList(context: Context) : BaseDialog(context) {

    override fun getLayoutId(): Int {
        return R.layout.dialog_empty_voice_list
    }

    private var isSelf = true
    override fun initView() {
        val textView = findViewById<TextView>(R.id.tv_SendVoice)
        if (!isSelf) {
            findViewById<ImageView>(R.id.iv_Hint).setImageResource(R.mipmap.icon_self_empty_dialog_list)
            textView.text = context.resources.getString(R.string.string_statue_off)
            findViewById<View>(R.id.iv_Click).visibility = View.GONE
            findViewById<TextView>(R.id.tv_Empty_Hint).text = context.resources.getString(R.string.string_empty_other_voice)
        }
        findViewById<View>(R.id.iv_Click).setOnClickListener {
            dismiss()
        }
        textView.setOnClickListener {
            if (isSelf) {
                onClickListener?.onClick(it)
            }
            dismiss()
        }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
        window.attributes.windowAnimations = R.style.AlphaDialogAnim
    }

    fun isSelf(isSelf: Boolean): DialogEmptyVoiceList {
        this.isSelf = isSelf
        return this
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogEmptyVoiceList {
        this.onClickListener = onClickListener
        return this
    }


}