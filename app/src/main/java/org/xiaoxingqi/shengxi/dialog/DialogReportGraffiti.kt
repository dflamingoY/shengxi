package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.dialog_report_graffiti.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class DialogReportGraffiti(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    private var isSelf = false
    private var isOwner = false

    override fun getLayoutId(): Int {
        return R.layout.dialog_report_graffiti
    }

    override fun initView() {
        try {
            val bean = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            if (IConstant.userAdminArray.contains(bean.user_id)) {
                tv_admin_delete.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
        }
        if (isSelf) {
            tv_Delete.visibility = View.VISIBLE
            if (!isOwner) {
                tv_Report.visibility = View.GONE
            }
        }
        tv_Cancel.setOnClickListener {
            dismiss()
        }
        tv_admin_delete.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tv_Report.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        tv_Delete.setOnClickListener {
            onClickListener?.onClick(it)
            dismiss()
        }
        initSystem()
    }

    private var onClickListener: View.OnClickListener? = null

    fun setOnClickListener(onClickListener: View.OnClickListener): DialogReportGraffiti {
        this.onClickListener = onClickListener
        return this
    }

    fun isSelf(isSelf: Boolean, isOwner: Boolean = false): DialogReportGraffiti {
        this.isSelf = isSelf
        this.isOwner = isOwner
        return this
    }

}