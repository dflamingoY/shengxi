package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.impl.DialogAdminPwdListener

/**
 * 请求输入操作密码
 */
class DialogCommitPwd(context: Context) : BaseDialog(context) {
    private lateinit var warnView: View
    private lateinit var editPwd: EditText
    private lateinit var progress: View
    private var orderKey: String? = null
    private var orderValue: String? = null

    override fun getLayoutId(): Int {
        return R.layout.dialog_commit_pwd
    }

    override fun initView() {
        setCancelable(false)
        val hintType = findViewById<TextView>(R.id.tv_hint_type)
        hintType.text = String.format(context.resources.getString(R.string.string_commit_pwd_1), when (orderKey) {
            "flag" -> context.resources.getString(R.string.string_admin_injury)
            "confine" -> context.resources.getString(R.string.string_admin_limit)
            "userStatus" -> context.resources.getString(R.string.string_admin_close)
            "warn" -> context.resources.getString(R.string.string_admin_warn)
            "isHidden" -> context.resources.getString(R.string.string_admin_setPrivacy_1)
            "deleteArt" -> "删除该绘画"
            "deleteWord" -> "删除台词"
            "deleteDubbing" -> "删除配音"
            "hideDubbing" -> "隐藏配音"
            "pick" -> "设为声昔君Pick"
            "topAt" -> "设为今日推荐"
            "topVoice" -> if (orderValue == "0") "取消置顶" else "置顶"
            else -> "管理员登录"
        })
        warnView = findViewById(R.id.tv_warn)
        editPwd = findViewById(R.id.border_edit)
        progress = findViewById(R.id.layout_progress)
        findViewById<View>(R.id.tv_cancel).setOnClickListener { dismiss() }
        findViewById<View>(R.id.tv_commit).setOnClickListener {
            if (editPwd.text.toString().trim().length < 6) {
                Toast.makeText(context, "密码为6位数", Toast.LENGTH_SHORT).show()
            } else {
                warnView.visibility = View.GONE
                progress.visibility = View.VISIBLE
                onResultListener?.onResult(orderKey, orderValue, editPwd.text.toString().trim())
            }
        }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    /**
     * 设置当前操作的类型
     */
    fun setOperator(key: String, value: String?): DialogCommitPwd {
        orderKey = key
        orderValue = value
        return this
    }

    /**
     * 提示密码错误,操作正确, 不会 走此方法
     */
    fun setCallBack() {
        try {
            warnView.visibility = View.VISIBLE
            progress.visibility = View.GONE
        } catch (e: Exception) {

        }
    }

    private var onResultListener: DialogAdminPwdListener? = null

    fun setOnResultListener(onResultListener: DialogAdminPwdListener): DialogCommitPwd {
        this.onResultListener = onResultListener
        return this
    }

}