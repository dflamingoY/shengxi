package org.xiaoxingqi.shengxi.dialog

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewAnimationUtils
import kotlinx.android.synthetic.main.dialog_voluntary_update.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils
import kotlin.math.ceil

class DialogVoluntaryUpdate(context: Context) : BaseDialog(context) {
    private var isForce = 0
    private var time = 0
    override fun getLayoutId(): Int {
        return R.layout.dialog_voluntary_update
    }

    override fun initView() {
        setCancelable(false)
        if (0 != isForce) {
            tv_hint.text = context.resources.getString(R.string.string_old_hint_update_force)
            tv_Cancel.visibility = View.GONE
            tv_Commit.text = "去瞧瞧"
        }
        tv_Cancel.setOnClickListener {
            val loginBean = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            SPUtils.setLong(context, IConstant.ISHINTUPDATE + loginBean.user_id, (System.currentTimeMillis()))
            dismiss()
        }
        tv_Commit.setOnClickListener {
            if (isForce == 0) {
                dismiss()
            } else {
                val reveal = ViewAnimationUtils.createCircularReveal(tv_Commit, tv_Commit.width / 2, tv_Commit.height / 2, AppTools.dp2px(context, 104).toFloat(), AppTools.dp2px(context, 18).toFloat())
                reveal.duration = 320
                reveal.start()
                reveal.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        tv_Commit.visibility = View.GONE
                    }
                })
            }
            onClickListener?.onClick(it)
        }
        window!!.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    /**
     * 设置是否强制更新
     */
    fun setForce(force: Int, time: Int): DialogVoluntaryUpdate {
        this.time = time
        this.isForce = force
        return this
    }

    fun updateProgress(progress: Float) {
        tv_DownLoad.text = "${(progress * 100).toInt()}"
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogVoluntaryUpdate {
        this.onClickListener = onClickListener
        return this
    }


}