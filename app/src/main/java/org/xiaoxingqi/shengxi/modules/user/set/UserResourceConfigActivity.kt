package org.xiaoxingqi.shengxi.modules.user.set

import kotlinx.android.synthetic.main.activity_privacy_select.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseThemeNoSwipeActivity
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.saveSetting
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class UserResourceConfigActivity : BaseThemeNoSwipeActivity() {
    private var value = 1
    private var type = 1
    override fun getLayoutId(): Int {
        return R.layout.activity_privacy_select
    }

    override fun initView() {

    }

    override fun initData() {
        type = intent.getIntExtra("type", 1)
        when (type) {
            1 -> {
                tvTitle.text = String.format(resources.getString(R.string.string_privacyAct_27), resources.getString(R.string.string_Movies))
                tv_Content.text = String.format(resources.getString(R.string.string_privacyAct_28), resources.getString(R.string.string_Movies))
                tv_hint_2.text = resources.getString(R.string.string_privacy_resource_1)
            }
            2 -> {
                tvTitle.text = String.format(resources.getString(R.string.string_privacyAct_27), resources.getString(R.string.string_book))
                tv_Content.text = String.format(resources.getString(R.string.string_privacyAct_28), resources.getString(R.string.string_book))
                tv_hint_2.text = resources.getString(R.string.string_privacy_resource_2)
            }
            3 -> {
                tvTitle.text = String.format(resources.getString(R.string.string_privacyAct_27), resources.getString(R.string.string_43))
                tv_Content.text = String.format(resources.getString(R.string.string_privacyAct_28), resources.getString(R.string.string_44))
                tv_hint_2.text = resources.getString(R.string.string_privacy_resource_3)
        }
        }
        value = intent.getIntExtra("value", 1)
        viewAll.isSelected = value == 1
        viewFriend.isSelected = !viewAll.isSelected
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        viewAll.setOnClickListener {
            viewFriend.isSelected = false
            it.isSelected = true
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
            saveSetting(loginBean.user_id, when (type) {
                2 -> "book_voice_visibility"
                3 -> "song_voice_visibility"
                else -> "movie_voice_visibility"
            }, if (viewAll.isSelected) 1 else 2) {
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