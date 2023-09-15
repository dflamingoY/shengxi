package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.utils.AppTools

/**
 *选择日期
 */
class DialogDateEnter(context: Context) : BaseDialog(context) {
    private val matcherYear = "(18|19|20|21)"
    private val matcherMonth = "0[1-9]|1(0|1|2)"
    private var date: String? = null
    override fun getLayoutId(): Int {
        return R.layout.dialog_date_enter
    }

    override fun initView() {
        setCancelable(false)
        findViewById<View>(R.id.iv_close).setOnClickListener { dismiss() }
        val editYear = findViewById<EditText>(R.id.et_year)//18 19  20 21 开头
        val editMonth = findViewById<EditText>(R.id.et_month)//0 1   0123456789
        val editDays = findViewById<EditText>(R.id.et_days)// 0 1 2 3   0123456789
        editYear.isCursorVisible = false
        editMonth.isCursorVisible = false
        editDays.isCursorVisible = false
        editYear.isLongClickable = false
        editMonth.isLongClickable = false
        editDays.isLongClickable = false
        if (!TextUtils.isEmpty(date)) {
            try {
                val split = date!!.split("-")
                if (split.size == 3) {
                    editYear.setText(split[0])
                    editMonth.setText(split[1])
                    editDays.setText(split[2])
                }
            } catch (e: Exception) {
            }
        }
        editYear.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s!!.length == 2) {
                    if (!s!!.toString().matches(Regex(matcherYear))) {
                        editYear.setText("")
                    }
                }
                if (s!!.length == 4) {
                    editMonth.requestFocus()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        editMonth.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //0\\d{0-9}  1{1|2}
                if (s!!.length == 2) {
                    if (!s!!.toString().matches(Regex(matcherMonth))) {
                        editMonth.setText("")
                    } else {
                        editDays.requestFocus()
                    }
                }
                if (s.isEmpty()) {
                    if (!TextUtils.isEmpty(editDays.text.toString().trim()))
                        editDays.setText("")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        editDays.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val month = editMonth.text.toString().trim()
                if (s!!.length == 1) {
                    if (month == "02") {//只允许输入0 1 2
                        if (s.toString() != "0" && s.toString() != "1" && s.toString() != "2") {
                            s.clear()
                        }
                    } else {//0 1 2 3
                        if (s.toString() != "0" && s.toString() != "1" && s.toString() != "2" && s.toString() != "3") {
                            s.clear()
                        }
                    }
                } else if (s!!.length == 2) {
                    if (s!!.toString() == "00") {
                        editDays.setText(s[0].toString())
                        editDays.setSelection(1)
                    } else {
                        if (month == "02") {//(1|2)(1-8) (1|2)(1-9)
                            val year = editYear.text.toString().trim()
                            if (Integer.parseInt(year).rem(4) == 0) {//闰年 2 月 29
                                if (!s!!.toString().matches(Regex("(0|1|2)[0-9]"))) {
                                    editDays.setText(s[0].toString())
                                    editDays.setSelection(1)
                                }
                            } else {
                                if (!s!!.toString().matches(Regex("(0|1|2)[0-8]"))) {
                                    editDays.setText(s[0].toString())
                                    editDays.setSelection(1)
                                }
                            }
                        } else {
                            if (s[0].toString() == "3") {
                                if (month == "04" || month == "06" || month == "09" || month == "11") {//30号
                                    if (s[1].toString() != "0") {
                                        editDays.setText(s[0].toString())
                                        editDays.setSelection(1)
                                    }
                                } else {//31
                                    if (s[1].toString() != "0" && s[1].toString() != "1") {
                                        editDays.setText(s[0].toString())
                                        editDays.setSelection(1)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        findViewById<View>(R.id.tv_commit).setOnClickListener {
            if (!AppTools.isEmptyEt(editYear, 3) && !AppTools.isEmptyEt(editMonth, 1) && !AppTools.isEmptyEt(editDays, 1)) {
                onClickResultListener?.onResult(editYear.text.toString() + "-" + editMonth.text.toString() + "-" + editDays.text.toString())
                dismiss()
            } else {
                Toast.makeText(context, "日期格式不正确", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<View>(R.id.tv_cancel).setOnClickListener {
            editYear.setText("")
            editMonth.setText("")
            editDays.setText("")
            editYear.requestFocus()
        }
        window.setBackgroundDrawable(ColorDrawable(0))
        val m = window!!.windowManager
        val d = m.defaultDisplay
        val p = window!!.attributes
        p.width = d.width //设置dialog的宽度为当前手机屏幕的宽度
        window!!.attributes = p
    }

    fun setLastDate(date: String?): DialogDateEnter {
        this.date = date
        return this
    }

    interface OnClickResultListener {
        fun onResult(date: String)
    }

    private var onClickResultListener: OnClickResultListener? = null

    fun setOnClickResultListener(onClickListener: OnClickResultListener): DialogDateEnter {
        this.onClickResultListener = onClickListener
        return this
    }

}