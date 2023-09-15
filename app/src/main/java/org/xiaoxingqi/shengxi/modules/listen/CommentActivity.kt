package org.xiaoxingqi.shengxi.modules.listen

import android.app.Activity
import android.content.Intent
import kotlinx.android.synthetic.main.activity_comment.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.utils.AppTools

class CommentActivity : BaseAct() {
    override fun getLayoutId(): Int {
        return R.layout.activity_comment
    }

    override fun initView() {

    }

    override fun initData() {


    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            finish()
        }
        tv_Commit.setOnClickListener {
            if (!AppTools.isEmptyEt(et_Content, 0)) {
                setResult(Activity.RESULT_OK, Intent().putExtra("result", et_Content.text.toString().trim()))
                finish()
            }
        }
    }
}