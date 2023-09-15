package org.xiaoxingqi.shengxi.modules.user.set

import kotlinx.android.synthetic.main.activity_privacy_home_tab.*
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseThemeNoSwipeActivity
import org.xiaoxingqi.shengxi.impl.HomeTabChangeEvent
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils

//默认0 0表示操场, 1 表示寝室
class PrivacyHomeTabActivity : BaseThemeNoSwipeActivity() {
    private lateinit var loginBean: LoginData.LoginBean
    private var defaultIndex: String = "0"

    override fun getLayoutId(): Int {
        return R.layout.activity_privacy_home_tab
    }

    override fun initView() {

    }

    override fun initData() {
        loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        defaultIndex = SPUtils.getString(this, IConstant.TAB_HOME_INDEX + loginBean.user_id, "0")
        if (defaultIndex == "0") {
            viewGround.isSelected = true
        } else {
            viewRoom.isSelected = true
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        viewGround.setOnClickListener {
            SPUtils.setString(this, IConstant.TAB_HOME_INDEX + loginBean.user_id, "0")
            viewGround.isSelected = true
            viewRoom.isSelected = false
        }
        viewRoom.setOnClickListener {
            SPUtils.setString(this, IConstant.TAB_HOME_INDEX + loginBean.user_id, "1")
            viewGround.isSelected = false
            viewRoom.isSelected = true
        }
    }

    private fun getCurrent(): String {
        return if (viewGround.isSelected) "0" else "1"
    }

    override fun finish() {
        //更新主界面切换位置
        if (defaultIndex != getCurrent()) {
            EventBus.getDefault().post(HomeTabChangeEvent(getCurrent()))
        }
        super.finish()
    }
}