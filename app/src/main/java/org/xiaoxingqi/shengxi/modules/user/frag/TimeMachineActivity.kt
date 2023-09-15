package org.xiaoxingqi.shengxi.modules.user.frag

import kotlinx.android.synthetic.main.activity_time_machine.*
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.impl.SensorChangeMoodEvent
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class TimeMachineActivity : BaseAct() {


    override fun changSpeakModel(type: Int) {
        EventBus.getDefault().post(SensorChangeMoodEvent(type))
    }

    var userId: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_time_machine
    }

    override fun initView() {

    }

    override fun initData() {
        userId = intent.getStringExtra("userId")
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (loginBean.user_id != userId) {
            tv_Title.text = resources.getString(R.string.string_title_5)
        }
        supportFragmentManager.beginTransaction().replace(R.id.frameContainer, TimeMachineFragment()).commit()
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
    }
}