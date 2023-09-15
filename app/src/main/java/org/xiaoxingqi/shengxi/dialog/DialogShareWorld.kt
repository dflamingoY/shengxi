package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

class DialogShareWorld(context: Context) : BaseDialog(context) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    override fun getLayoutId(): Int {
        return R.layout.dialog_share_world
    }

    private var countText: String? = null
    override fun initView() {
        if (!TextUtils.isEmpty(countText)) {
            findViewById<TextView>(R.id.tv_Desc).text = countText
        }
        findViewById<View>(R.id.tv_Cancel).setOnClickListener {
            dismiss()
        }
        findViewById<View>(R.id.tv_Commit).setOnClickListener { view ->
            onClickListener?.let {
                it.onClick(view)
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

    fun setCountText(count: String): DialogShareWorld {
        countText = count
        return this
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogShareWorld {
        this.onClickListener = onClickListener
        return this
    }

}