package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class DialogChatOperator(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_report
    }

    override fun initView() {
        val addblack = findViewById<TextView>(R.id.tv_AddBlack)
        addblack.setOnClickListener { view ->
            onClickListener?.onClick(view)
            dismiss()
        }
        val report = findViewById<View>(R.id.tv_Report)
        report.setOnClickListener { view ->
            onClickListener?.onClick(view)
            dismiss()
        }
        val addDelete = findViewById<TextView>(R.id.tv_Delete)
        val tvSendMsg = findViewById<TextView>(R.id.tv_admin_delete)
        if (isCustom) {
            addblack.visibility = View.GONE
            report.visibility = View.GONE
        }
        addDelete.visibility = View.VISIBLE
        addDelete.setOnClickListener { view ->
            onClickListener?.onClick(view)
            dismiss()
        }
        findViewById<View>(R.id.tv_Cancel).setOnClickListener { dismiss() }
        findViewById<View>(R.id.tv_report_normal).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tvSendMsg.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        val loginBean = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (loginBean.user_id == "1") {
            if (isTopicType) {
                tvSendMsg.visibility = View.VISIBLE
                tvSendMsg.text = "发送密码提示短信"
            }
            findViewById<TextView>(R.id.tv_admin_user_details).let {
                it.text = "编辑回复语言"
                it.visibility = View.VISIBLE
                it
            }.setOnClickListener {
                onClickListener?.onClick(it)
                dismiss()
            }
        }
        addDelete.text = context.resources.getString(R.string.string_delete_hint_11)
        initSystem()
    }

    fun setIsCustom(isCustom: Boolean): DialogChatOperator {
        this.isCustom = isCustom
        return this
    }

    private var isTopicType = false
    fun isPwdTopic(isTopicType: Boolean): DialogChatOperator {
        this.isTopicType = isTopicType
        return this
    }

    private var isCustom = false
    private var onClickListener: View.OnClickListener? = null
    fun setOnClickListener(onClickListener: View.OnClickListener): DialogChatOperator {
        this.onClickListener = onClickListener
        return this
    }
}