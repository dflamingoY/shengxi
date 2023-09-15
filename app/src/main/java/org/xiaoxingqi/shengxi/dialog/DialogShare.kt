package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import org.xiaoxingqi.shengxi.R

class DialogShare(context: Context, isWechat: Boolean) : BaseDialog(context, R.style.FullDialogTheme) {
    private var isWechat = false

    init {
        this.isWechat = isWechat
    }

    override fun getLayoutId(): Int {
        return R.layout.dialog_share
    }

    override fun initView() {
        val wechat = findViewById<View>(R.id.linearWechat)
        val moment = findViewById<View>(R.id.linearMoment)
        val qq = findViewById<View>(R.id.linearQQ)
        val qzone = findViewById<View>(R.id.linearQzone)
        if (isWechat) {
            findViewById<View>(R.id.linearW).visibility = View.VISIBLE
            findViewById<View>(R.id.linearQ).visibility = View.GONE
        } else {
            findViewById<View>(R.id.linearW).visibility = View.GONE
            findViewById<View>(R.id.linearQ).visibility = View.VISIBLE
        }
        doAnim(wechat, 100)
        doAnim(moment, 150)
        doAnim(qq, 100)
        doAnim(qzone, 150)
        wechat.setOnClickListener { view ->
            onClickListener?.let {
                it.onClick(view)
            }
            dismiss()
        }
        moment.setOnClickListener { view ->
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
        findViewById<View>(R.id.tv_Cancel).setOnClickListener { dismiss() }
        initSystem()
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogShare {
        this.onClickListener = onClickListener
        return this
    }

    private fun doAnim(view: View, delayed: Long) {
        val animation = AnimationUtils.loadAnimation(context, R.anim.item_duang_show)
        view.animation = animation
        animation.startOffset = delayed
        animation.start()
    }

}