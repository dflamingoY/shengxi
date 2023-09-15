package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Toast
import org.xiaoxingqi.shengxi.R

/**
 * 非0s 用户请求加好友,如果当前的隐私设置为禁止陌生用户浏览声兮和相册
 */
class DialogUserSet(context: Context) : BaseDialog(context) {

    override fun getLayoutId(): Int {
        return R.layout.dialog_hint_change_set
    }

    override fun initView() {
        findViewById<View>(R.id.tv_Cancel).setOnClickListener {
            Toast.makeText(context, "好友请求已发送", Toast.LENGTH_SHORT).show()
            dismiss()
        }
        findViewById<View>(R.id.tv_Commit).setOnClickListener {
            //            context.startActivity(Intent(context, PrivacyPhotoActivity::class.java))
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

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogUserSet {
        this.onClickListener = onClickListener
        return this
    }
}