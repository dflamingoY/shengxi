package org.xiaoxingqi.shengxi.modules.listen.addItem

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import kotlinx.android.synthetic.main.activity_add_item_name.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.utils.AppTools

/*
添加名字
 */
class AddItemNameActivity : BaseAct() {
    private var limitCount = 100
    override fun getLayoutId(): Int {
        return R.layout.activity_add_item_name
    }

    override fun initView() {
        tv_push.isSelected = false
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        limitCount = intent.getIntExtra("count", 100)
        intent.getStringExtra("title")?.let {
            tv_title.text = it
        }
        tv_limit_count.text = tv_limit_count.text.toString().replace("20", "$limitCount")
        val name = intent.getStringExtra("name")
        tv_count.text = "0 / $limitCount"
        if (!TextUtils.isEmpty(name)) {
            tv_push.isSelected = true
            et_content.setText(name)
            tv_count.text = "${name.length} / $limitCount"
            try {
                et_content.setSelection(name.length)
            } catch (e: Exception) {
            }
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        tv_push.setOnClickListener {
            if (!it.isSelected || AppTools.isEmptyEt(et_content, 0)) {
                return@setOnClickListener
            }
            val name = et_content.text.toString().trim()
            setResult(Activity.RESULT_OK, Intent().putExtra("name", if (name.length > limitCount) name.substring(0, limitCount) else name))
            finish()
        }
        et_content.addTextChangedListener(object : TextWatcher {
            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                tv_push.isSelected = s!!.isNotEmpty()
                if (s.length > limitCount) {
                    tv_count.text = Html.fromHtml(String.format(resources.getString(R.string.string_count_10).replace("10", limitCount.toString()), s?.length.toString()))
                } else {
                    tv_count.text = "${s?.length} / $limitCount"
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

    }
}