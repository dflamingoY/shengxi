package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_report.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class DialogReport(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_report
    }

    private var status: Boolean = false
    private var blackStatus: String? = null
    private var reportStatus: Boolean = true
    private var reportTitle: String? = null
    private var userId: String? = null
    private var isReportItem = false
    private var resourceType = 0
    private var isFollowed = false
    private var isCollection = false
    private var collectionTitle: String? = null
    private var isTopEnable = false
    private var topStatus: Boolean = false
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
                if (isTopEnable) {
                    tvTop.visibility = View.VISIBLE
                    tvTop.text = if (topStatus) "取消置顶" else "置顶"
                }
            }
        } catch (e: Exception) {
        }

        val followView = findViewById<TextView>(R.id.tv_Follow)
        if (resourceType != 0) {
            followView.visibility = View.VISIBLE
            followView.text = if (isFollowed) "取消" else {
                ""
            } + when (resourceType) {
                1 -> context.resources.getString(R.string.string_follow_movies)
                2 -> context.resources.getString(R.string.string_follow_book)
                3 -> context.resources.getString(R.string.string_follow_song)
                else -> context.resources.getString(R.string.string_follow_movies)
            }
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
        if (isCollection) {
            findViewById<View>(R.id.tvCollection).visibility = View.VISIBLE
            if (!TextUtils.isEmpty(collectionTitle)) {
                findViewById<TextView>(R.id.tvCollection).text = collectionTitle
            }
        }
        val report = findViewById<TextView>(R.id.tv_Report)
        if (!TextUtils.isEmpty(reportTitle)) {
            report.text = reportTitle
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
        findViewById<View>(R.id.tv_admin_user_details).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        if (isReportItem) {
            findViewById<View>(R.id.tv_report_normal).visibility = View.GONE
            findViewById<View>(R.id.tv_admin_setPrivacy).visibility = View.GONE
        }
        findViewById<View>(R.id.tv_report_normal).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        followView.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        findViewById<View>(R.id.tvCollection).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tvTop.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        initSystem()
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogReport {
        this.onClickListener = onClickListener
        return this
    }

    fun addBlack(text: String?): DialogReport {
        blackStatus = text
        return this
    }

    /**
     * 超级管理员, 设置当前声兮为隐私
     */
    fun setAdminUserId(userId: String): DialogReport {
        this.userId = userId
        return this
    }

    /**
     * 设置删除对话, ${声兮单页, 私聊页}
     */
    fun setDeleteStatus(status: Boolean): DialogReport {
        this.status = status
        return this
    }

    /**
     * 私聊页,当用户是声兮小二时可以举报用户的音频
     */
    fun setReportStatus(reportStatus: Boolean): DialogReport {
        this.reportStatus = reportStatus
        return this
    }

    /**
     *  sheng兮单页和私聊, 是举报音频 不是屏蔽
     * */
    fun setReportTitle(reportTitle: String): DialogReport {
        this.reportTitle = reportTitle
        return this
    }

    fun setIsReportNormal(isReportItem: Boolean): DialogReport {
        this.isReportItem = isReportItem
        return this
    }

    /**
     * 设置是否是书影音资源
     */
    fun setResource(resourceType: Int, isFollowed: Boolean = false): DialogReport {
        this.resourceType = resourceType
        this.isFollowed = isFollowed
        return this
    }

    fun setCollectionAble(isCollection: Boolean, collectionTitle: String? = null): DialogReport {
        this.isCollection = isCollection
        this.collectionTitle = collectionTitle
        return this
    }

    /**
     * @param isTopEnable 能否置顶
     * @param topStatus 是否正在置顶
     */
    fun isTopEnable(isTopEnable: Boolean, topStatus: Boolean): DialogReport {
        this.isTopEnable = isTopEnable
        this.topStatus = topStatus
        return this
    }


}