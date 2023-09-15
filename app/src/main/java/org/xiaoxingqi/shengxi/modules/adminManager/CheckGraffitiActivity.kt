package org.xiaoxingqi.shengxi.modules.adminManager

import kotlinx.android.synthetic.main.activity_check_graffiti.*
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct

//被举报的涂鸦
class CheckGraffitiActivity : BaseAct() {
    override fun getLayoutId(): Int {
        return R.layout.activity_check_graffiti
    }

    override fun initView() {

    }

    override fun initData() {

    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        tvDetails.setOnClickListener {
            startActivity<CheckGraffitiListActivity>()
        }
    }
}