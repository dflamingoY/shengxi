package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import kotlinx.android.synthetic.main.dialog_simple_content.*
import org.xiaoxingqi.shengxi.R

class DialogSimpleContent(context: Context) : BaseDialog(context) {
    private var content: String? = null
    override fun getLayoutId(): Int {
        return R.layout.dialog_simple_content
    }

    override fun initView() {
        content?.let {
            tvContent.text = it
        }
        fillWidth()
    }

    fun setContent(content: String): DialogSimpleContent {
        this.content = content
        return this
    }

}