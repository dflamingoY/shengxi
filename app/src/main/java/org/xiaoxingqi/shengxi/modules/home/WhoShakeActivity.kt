package org.xiaoxingqi.shengxi.modules.home

import kotlinx.android.synthetic.main.activity_time_machine.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct

class WhoShakeActivity : BaseAct() {

    override fun getLayoutId(): Int {
        return R.layout.activity_time_machine
    }

    override fun initView() {
        tv_Title.text = "你是谁"
    }

    override fun initData() {
        supportFragmentManager.beginTransaction().replace(R.id.frameContainer, ShakeFragment()).commit()
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
    }
}