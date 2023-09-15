package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import android.widget.TextView
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class DialogFriendsOperator(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    private var status = 0//0=不是好友且没拉黑，1=是好友，2=已拉黑，
    private var userStatus: String? = "1"
    private var flag: String? = "0"
    private var limitAt = 0L
    private var isWhite = 0
    override fun getLayoutId(): Int {
        return R.layout.dialog_friend_operator
    }

    override fun initView() {
        try {
            val bean = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            if (IConstant.userAdminArray.contains(bean.user_id)) {
                findViewById<View>(R.id.linear_operator).visibility = View.VISIBLE
            }
        } catch (e: Exception) {

        }
        val remark = findViewById<View>(R.id.tv_Remark)
        remark.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        val report = findViewById<TextView>(R.id.tv_Report)
        if (status == 2) {
            report.text = "取消屏蔽"
        } else {
            report.text = context.resources.getString(R.string.string_Report)
        }
        report.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        val delete = findViewById<View>(R.id.tv_Delete)
        delete.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        remark.visibility = if (status == 1) View.VISIBLE else View.GONE
        delete.visibility = if (status == 1) View.VISIBLE else View.GONE
        findViewById<TextView>(R.id.tv_add_white_list).visibility = if (status == 1) View.GONE else View.VISIBLE
        findViewById<View>(R.id.tv_Cancel).setOnClickListener {
            dismiss()
        }
        findViewById<TextView>(R.id.tv_admin_warn).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        val tvLimit = findViewById<TextView>(R.id.tv_admin_limit)
        if (System.currentTimeMillis() / 1000 <= limitAt) {
            tvLimit.text = context.resources.getString(R.string.string_admin_limit_clear)
        }
        tvLimit.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        val tvInjure = findViewById<TextView>(R.id.tv_admin_injury)
        if ("1" == flag) {
            tvInjure.text = context.resources.getString(R.string.string_admin_injury_clear)
        }
        tvInjure.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        findViewById<TextView>(R.id.tv_add_white_list).let {
            it.text = if (isWhite == 1) context.resources.getText(R.string.string_remove_white_list) else context.resources.getString(R.string.string_dialog_change_set_3)
            it
        }.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        val tvClose = findViewById<TextView>(R.id.tv_admin_close)
        if ("3" == userStatus) {
            tvClose.text = context.resources.getString(R.string.string_admin_close_clear)
        }
        tvClose.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        findViewById<View>(R.id.tv_report_normal).setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        initSystem()
    }

    /**
     * 是否被拉黑状态
     * @status  是否被屏蔽
     * @userStatus 超管处理的用户状态
     * @flag 是否设置仙人掌`
     * @limitAt 关禁闭
     */
    fun setRelation(status: Int, userStatus: String?, flag: String?, limitAt: Long): DialogFriendsOperator {
        this.status = status
        this.userStatus = userStatus
        this.flag = flag
        this.limitAt = limitAt
        return this
    }

    /**
     * 设置是否在白名单
     */
    fun setInWhiteList(isWhite: Int): DialogFriendsOperator {
        this.isWhite = isWhite
        return this
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogFriendsOperator {
        this.onClickListener = onClickListener
        return this
    }
}