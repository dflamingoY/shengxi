package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import org.xiaoxingqi.shengxi.R

class DialogInvalidate(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_invalidate
    }

    override fun initView() {
        findViewById<View>(R.id.tv_Cancel).setOnClickListener { dismiss() }
        val weibo = findViewById<View>(R.id.linearWeibo)
        val wechat = findViewById<View>(R.id.linearWechat)
        val moments = findViewById<View>(R.id.linearMoment)
        val qq = findViewById<View>(R.id.linearQQ)
        val qzone = findViewById<View>(R.id.linearQzone)
        weibo.setOnClickListener { view ->
            onClickListener?.let {
                it.onClick(view)
            }
            dismiss()
        }
        wechat.setOnClickListener { view ->
            onClickListener?.let {
                it.onClick(view)
            }
            dismiss()
        }
        moments.setOnClickListener { view ->
            onClickListener?.let {
                it.onClick(view)
            }
            dismiss()
        }
        qq.setOnClickListener { view ->
            onClickListener?.let {
                it.onClick(view)
            }
            dismiss()
        }
        qzone.setOnClickListener { view ->
            onClickListener?.let {
                it.onClick(view)
            }
            dismiss()
        }
        doAnim(weibo, 100)
        doAnim(wechat, 150)
        doAnim(moments, 200)
        doAnim(qq, 250)
        doAnim(qzone, 300)
        initSystem()
    }

    private fun doAnim(view: View, delayed: Long) {
        val animation = AnimationUtils.loadAnimation(context, R.anim.item_duang_show)
        view.animation = animation
        animation.startOffset = delayed
        animation.start()
    }

    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogInvalidate {
        this.onClickListener = onClickListener
        return this
    }

}