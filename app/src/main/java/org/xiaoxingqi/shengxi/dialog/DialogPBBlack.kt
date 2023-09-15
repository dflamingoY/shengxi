package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

/*
屏蔽 --成功之后的对应提示
* */
class DialogPBBlack(context: Context) : BaseDialog(context) {

    private var type = "1"
    override fun getLayoutId(): Int {
        return R.layout.dialog_ping_bi_success
    }

    override fun initView() {
        setCancelable(false)
        val title = findViewById<TextView>(R.id.tv_Title)
        val content = findViewById<TextView>(R.id.tv_content)
        when (type) {
            "1" -> {
                title.text = content.resources.getString(R.string.string_black_hint_2)
                content.text = content.resources.getString(R.string.string_black_hint_3)
            }
            "2" -> {
                title.text = content.resources.getString(R.string.string_black_hint_4)
                content.text = content.resources.getString(R.string.string_black_hint_5)
            }
            "3" -> {
                title.text = content.resources.getString(R.string.string_black_hint_6)
                content.text = content.resources.getString(R.string.string_black_hint_7)
            }
        }
        findViewById<View>(R.id.tv_Commit).setOnClickListener {
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

    fun setType(type: String): DialogPBBlack {
        this.type = type
        return this
    }


    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogPBBlack {
        this.onClickListener = onClickListener
        return this
    }

}