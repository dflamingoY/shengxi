package org.xiaoxingqi.shengxi.modules.listen

import android.app.Activity
import android.content.Intent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_find_add_word.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct

class FindWordActivity : BaseAct() {
    private var isEdit = false
    private var name: String? = null
    private var dataList: Array<String>? = null
    private var index = -1
    override fun getLayoutId(): Int {
        return R.layout.activity_find_add_word
    }

    override fun initView() {

    }

    override fun initData() {
        isEdit = intent.getBooleanExtra("isEdit", false)
        name = intent.getStringExtra("name")
        val allWord = intent.getStringExtra("data")
        if (!TextUtils.isEmpty(allWord)) {
            dataList = allWord.split(" ").filter {
                it.isNotEmpty()
            }.toTypedArray()
        }
        if (isEdit) {
            index = dataList!!.indexOf(name!!)
            etContent.hint = resources.getString(R.string.string_7)
            etContent.setText(name)
            tvCancel.text = "完成"
            ivClear.visibility = View.VISIBLE
            etContent.setSelection(name!!.length)
        }
    }

    override fun initEvent() {
        /*etContent.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (!isEdit) {
                    var result = etContent.text.toString().trim()
                    if (result.length > 2) {
                        result = result.substring(0, 2)
                    }
                    if (dataList != null) {
                        if (dataList!!.contains(result)) {
                            showCenter()
                            return@setOnEditorActionListener true
                        }
                    }
                    setResult(Activity.RESULT_OK, Intent().putExtra("name", result))
                    finish()
                }
                return@setOnEditorActionListener true
            }
            false
        }*/
        tvCancel.setOnClickListener {
            var result = etContent.text.toString().trim()
            if (!TextUtils.isEmpty(result)) {
                if (result.length > 2) {
                    result = result.substring(0, 2)
                }
                if (dataList != null) {
                    if (dataList!!.contains(result)) {
                        showCenter()
                        return@setOnClickListener
                    }
                }
                if (index != -1) {
                    dataList!![index] = result
                }
                setResult(Activity.RESULT_OK, Intent().putExtra("name", result))
            }
            finish()
        }
        etContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                ivClear.visibility = if (s!!.isNotEmpty()) View.VISIBLE else View.GONE
                tvCancel.text = if (s!!.isNotEmpty()) "完成" else "取消"
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        ivClear.setOnClickListener {
            etContent.setText("")
        }
    }

    private fun showCenter() {
        val toast = Toast.makeText(this, "此经历已添加过", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }
}