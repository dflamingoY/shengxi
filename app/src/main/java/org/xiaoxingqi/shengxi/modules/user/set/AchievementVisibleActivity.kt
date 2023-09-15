package org.xiaoxingqi.shengxi.modules.user.set

import kotlinx.android.synthetic.main.activity_privacy_select.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseThemeNoSwipeActivity
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.saveSetting
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class AchievementVisibleActivity : BaseThemeNoSwipeActivity() {
    private var value = 1
    override fun getLayoutId(): Int {
        return R.layout.activity_privacy_select
    }

    override fun initView() {

    }

    override fun initData() {
        value = intent.getIntExtra("value", 1)
        tvTitle.text = "成就设置"
        tv_Content.text = "展示我的成就"
        viewAll.setTitle("展示")
        viewFriend.setTitle("不展示")
        if (value == 1) {
            viewAll.isSelected = true
        } else
            viewFriend.isSelected = true
    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            finish()
        }
        viewAll.setOnClickListener {
            it.isSelected = true
            viewFriend.isSelected = false
        }
        viewFriend.setOnClickListener {
            it.isSelected = true
            viewAll.isSelected = false
        }
    }

    override fun finish() {
        if (value == if (viewAll.isSelected) 1 else 2) {//没变
            super.finish()
        } else {
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            transLayout.showProgress()
            saveSetting(loginBean.user_id, "achievement_visibility", if (viewAll.isSelected) 1 else 2, "other") {
                transLayout.showContent()
                if (it != null) {
                    if (it.code == 0)
                        super.finish()
                    else
                        showToast(it.msg)
                }
            }
        }
    }


}