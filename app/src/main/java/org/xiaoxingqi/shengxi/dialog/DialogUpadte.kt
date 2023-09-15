package org.xiaoxingqi.shengxi.dialog

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.view.View
import android.view.ViewAnimationUtils
import kotlinx.android.synthetic.main.dialog_update.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools

class DialogUpadte(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_update
    }

    private var updateFlag = 0
    override fun initView() {
        setCancelable(updateFlag == 0)
        if (updateFlag == 1) {//强制更新
            iv_Logo.visibility = View.GONE
            tv_soft_hint.visibility = View.GONE
            tv_NewFeature.visibility = View.VISIBLE
            tv_NewFeature.text = Html.fromHtml(context.resources.getString(R.string.string_newVersion))
            tv_Update.text = "一键更新"
            tv_Update.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            tv_Foc_Hint.visibility = View.VISIBLE
        } else {
            tv_Update.text = "一键升级你的心情簿"
        }
        tv_Update.setOnClickListener { view ->
            onClickListener?.let {
                it.onClick(view)
            }
            if (updateFlag == 1) {
                val reveal = ViewAnimationUtils.createCircularReveal(tv_Update, tv_Update.width / 2, tv_Update.height / 2, AppTools.dp2px(context, 104).toFloat(), AppTools.dp2px(context, 18).toFloat())
                reveal.duration = 320
                reveal.start()
                reveal.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        tv_Update.visibility = View.GONE
                    }
                })
            } else
                dismiss()
        }
        window!!.setBackgroundDrawable(ColorDrawable(0))
    }

    fun setText(hint: String?): DialogUpadte {
        this.hint = hint
        return this
    }

    private var hint: String? = null

    fun setFocusUpdate(flag: Int): DialogUpadte {
        updateFlag = flag
        return this
    }

    fun updateProgress(progress: Float) {
        tv_DownLoad.text = "${(progress * 100).toInt()}"
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogUpadte {
        this.onClickListener = onClickListener
        return this
    }

}