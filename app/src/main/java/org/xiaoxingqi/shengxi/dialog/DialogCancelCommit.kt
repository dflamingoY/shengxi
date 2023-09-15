package org.xiaoxingqi.shengxi.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.widget.TextView
import org.xiaoxingqi.shengxi.R

class DialogCancelCommit(context: Context?) : Dialog(context) {
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    private var title: String? = null
    private var content: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_cancel_commit)
        if (!TextUtils.isEmpty(title))
            findViewById<TextView>(R.id.tv_Title).text = title

        if (!TextUtils.isEmpty(content)) {
            findViewById<TextView>(R.id.tv_content).text = content
        }
        findViewById<View>(R.id.tv_Cancel).setOnClickListener { dismiss() }
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
    }

    fun setTitle_Contnet(title: String, content: String): DialogCancelCommit {
        this.title = title
        this.content = content
        return this
    }


    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogCancelCommit {
        this.onClickListener = onClickListener
        return this
    }
}