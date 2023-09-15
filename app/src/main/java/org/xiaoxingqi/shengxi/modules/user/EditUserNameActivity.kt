package org.xiaoxingqi.shengxi.modules.user

import android.app.Activity
import android.content.Intent
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import kotlinx.android.synthetic.main.activity_edit_username.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.utils.AppTools

class EditUserNameActivity : BaseAct() {
    private var isEditRemark = false
    private var originName: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_edit_username
    }

    override fun initView() {

    }

    override fun initData() {
        tv_Commit.isSelected = false
        intent.getStringExtra("name")?.let {
            tv_Commit.isSelected = true
            et_Name.setText(it)
            tv_Count.text = "${it.length}/10"
            try {
                et_Name.setSelection(it.length)
            } catch (e: Exception) {

            }
        }
        if (intent.getBooleanExtra("isName", true)) {
            tv_Title.text = resources.getString(R.string.string_editNameAct_1)
        } else {
            isEditRemark = true
            tv_Title.text = resources.getString(R.string.string_remark)
            et_Name.hint = "请输入用户名，若未输入将显示其原名"
            originName = intent.getStringExtra("originName")
            tv_Commit.isSelected = true
        }
    }

    override fun initEvent() {
        et_Name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!isEditRemark) {
                    tv_Commit.isSelected = s!!.isNotEmpty()
                }
                if (s!!.length > 10) {
                    tv_Count.text = Html.fromHtml(String.format(resources.getString(R.string.string_count_10), s?.length.toString()))
                } else {
                    tv_Count.text = "${s?.length} / 10"
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        tv_Commit.setOnClickListener {
            if ((!it.isSelected || AppTools.isEmptyEt(et_Name, 0)) && !isEditRemark) {
                return@setOnClickListener
            }
            val name = et_Name.text.toString().trim()
            if (isEditRemark && TextUtils.isEmpty(name)) {
                setResult(Activity.RESULT_OK, Intent().putExtra("name", originName))
            } else
                setResult(Activity.RESULT_OK, Intent().putExtra("name", if (name.length > 10) name.substring(0, 10) else name))
            finish()
        }
        btn_Back.setOnClickListener { finish() }
    }

}