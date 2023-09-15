package org.xiaoxingqi.shengxi.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools

class DialogTestShare : Dialog {

    constructor(context: Context) : super(context, R.style.FullDialogTheme) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_share_test)
        findViewById<View>(R.id.tv_Cancel).setOnClickListener {
            dismiss()
        }
        findViewById<View>(R.id.tv_ShareWechat).setOnClickListener { view ->
            onClickListener?.onClick(view)
            dismiss()
        }
        findViewById<View>(R.id.tv_ShareWeibo).setOnClickListener { view ->
            onClickListener?.onClick(view)
            dismiss()
        }
        findViewById<View>(R.id.tv_ShareQQ).setOnClickListener { view ->
            onClickListener?.onClick(view)
            dismiss()
        }
        findViewById<View>(R.id.tv_CreatePhoto).setOnClickListener { view ->
            onClickListener?.onClick(view)
            dismiss()
        }
        val p = window!!.attributes
        p.gravity = Gravity.BOTTOM
        p.width = AppTools.getWindowsWidth(context)
        window!!.attributes = p
    }

    var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogTestShare {
        this.onClickListener = onClickListener
        return this
    }
}