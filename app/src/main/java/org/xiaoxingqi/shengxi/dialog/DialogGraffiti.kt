package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.dialog_graffiti_help.*
import org.xiaoxingqi.shengxi.R

/**
 * 涂鸦部分的弹窗
 */
class DialogGraffiti(context: Context) : BaseDialog(context) {
    private var title: String? = null
    private var operatorName: String? = null
    override fun getLayoutId(): Int {
        return R.layout.dialog_graffiti_help
    }

    override fun initView() {
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }
        if (!TextUtils.isEmpty(operatorName)) {
            tv_Commit.text = operatorName
        }
        tv_Cancel.setOnClickListener { dismiss() }
        tv_Commit.setOnClickListener {
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

    fun setTitle(title: String, operatorName: String): DialogGraffiti {
        this.operatorName = operatorName
        this.title = title
        return this
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogGraffiti {
        this.onClickListener = onClickListener
        return this
    }

}