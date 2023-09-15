package org.xiaoxingqi.shengxi.modules.user

import android.app.Activity
import android.content.Intent
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import kotlinx.android.synthetic.main.activity_edit_desc.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct

class EditDescActivity : BaseAct() {
    override fun getLayoutId(): Int {
        return R.layout.activity_edit_desc
    }

    override fun initView() {

    }

    override fun initData() {
        tv_Commit.isSelected = false
        intent.getStringExtra("desc")?.let {
            et_Desc.setText(it)
            tv_Commit.isSelected = true
            tv_Count.text = "${it.length} / 20"
            try {
                et_Desc.setSelection(it.length)
            } catch (e: Exception) {

            }
        }
    }

    override fun initEvent() {
        tv_Commit.setOnClickListener {
            val desc = et_Desc.text.toString().trim()
            setResult(Activity.RESULT_OK, Intent().putExtra("desc", if (desc.length > 20) desc.substring(0, 20) else desc))
            finish()
        }
        et_Desc.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length!! > 20) {
                    tv_Count.text = Html.fromHtml(String.format(resources.getString(R.string.string_count_20), s.length.toString()))
                } else
                    tv_Count.text = "${s?.length} / 20"
//                tv_Hint.visibility = if (s?.length!! > 20) View.VISIBLE else View.GONE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        btn_Back.setOnClickListener { finish() }
    }
}