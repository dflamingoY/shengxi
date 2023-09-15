package org.xiaoxingqi.shengxi.modules.user.set

import kotlinx.android.synthetic.main.activity_privacy_select.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseThemeNoSwipeActivity

class AchieveFriendVisibleActivity : BaseThemeNoSwipeActivity() {
    private var value = 1
    override fun getLayoutId(): Int {
        return R.layout.activity_privacy_select
    }

    override fun initView() {
        value = intent.getIntExtra("value", 1)
        tvTitle.text = "展示我的成就"
        tv_Content.text = "个人信息小卡片展示记录小能手和穿越小达人成就"
        viewAll.setTitle("展示")
        viewFriend.setTitle("不展示")
        if (value == 1) {
            viewAll.isSelected = true
        } else
            viewFriend.isSelected = true
    }

    override fun initData() {
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
        super.finish()


    }
}