package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class DialogPainterReport(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    private var status: Boolean = false
    private var blackStatus: String? = null
    private var reportStatus: Boolean = true
    private var reportTitle: String? = null
    private var userId: String? = null
    private var isArt = false
    private var isPrivacy: Int = 0

    override fun getLayoutId(): Int {
        return R.layout.dialog_painter_report
    }

    override fun initView() {
        val textView = findViewById<TextView>(R.id.tv_AddBlack)
        if (!TextUtils.isEmpty(blackStatus)) {
            textView.text = blackStatus
            textView.visibility = View.VISIBLE
        }
        try {
            val bean = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            if (IConstant.userAdminArray.contains(bean.user_id)) {
                findViewById<View>(R.id.tv_admin_setPrivacy).visibility = View.VISIBLE
                if (isArt) {
                    findViewById<View>(R.id.tv_admin_delete).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.tv_admin_setPrivacy).text = context.resources.getString(R.string.string_admin_set_privavy_art)
                    findViewById<View>(R.id.tv_Recommend).visibility = View.VISIBLE
//                    if (isPrivacy == 1)
//                        findViewById<View>(R.id.tv_admin_user_details).visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
        }
        val view = findViewById<View>(R.id.tv_Delete)
        view.visibility =
                if (status) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        view.setOnClickListener { view ->
            onClickListener?.let { it.onClick(view) }
            dismiss()
        }
        textView.setOnClickListener { view ->
            onClickListener?.let { it.onClick(view) }
            dismiss()
        }
        val report = findViewById<TextView>(R.id.tv_Report)
        if (!TextUtils.isEmpty(reportTitle)) {
            report.text = reportTitle
            if (!isArt) {
                findViewById<View>(R.id.tv_admin_setPrivacy).visibility = View.GONE
            }
        }
        report.visibility = if (reportStatus) View.VISIBLE else View.GONE
        report.setOnClickListener { view ->
            onClickListener?.let { it.onClick(view) }
            dismiss()
        }
        findViewById<View>(R.id.tv_admin_setPrivacy).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        findViewById<View>(R.id.tv_Cancel).setOnClickListener { dismiss() }
        findViewById<View>(R.id.tv_admin_delete).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        findViewById<View>(R.id.tv_Recommend).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        findViewById<View>(R.id.tv_admin_user_details).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        initSystem()
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogPainterReport {
        this.onClickListener = onClickListener
        return this
    }

    fun addBlack(text: String?): DialogPainterReport {
        blackStatus = text
        return this
    }

    /**
     * 超级管理员, 设置当前声兮为隐私
     */
    fun setAdminUserId(userId: String): DialogPainterReport {
        this.userId = userId
        return this
    }

    /**
     * 设置删除对话, ${声兮单页, 私聊页}
     */
    fun setDeleteStatus(status: Boolean): DialogPainterReport {
        this.status = status
        return this
    }

    /**
     * 私聊页,当用户是声兮小二时可以举报用户的音频
     */
    fun setReportStatus(reportStatus: Boolean): DialogPainterReport {
        this.reportStatus = reportStatus
        return this
    }

    /**
     *  sheng兮单页和私聊, 是举报音频 不是屏蔽
     * */
    fun setReportTitle(reportTitle: String): DialogPainterReport {
        this.reportTitle = reportTitle
        return this
    }

    /**
     * 设置是否是举报艺术品
     */
    fun setArtReport(isArt: Boolean, isPrivacy: Int): DialogPainterReport {
        this.isArt = isArt
        this.isPrivacy = isPrivacy
        return this
    }


}