package org.xiaoxingqi.shengxi.modules.user.set

import android.os.Handler
import android.view.KeyEvent
import kotlinx.android.synthetic.main.activity_shake_privacy.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.ShakePrivacyData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class ShakePrivacyActivity : BaseAct() {
    private var shakeBean: ShakePrivacyData.ShakePrivacyBean? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_shake_privacy
    }

    override fun initView() {

    }

    override fun initData() {
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            if (judeChanged()) {
                updateSet()
            } else
                finish()
        }
        toggle_share.setOnClickListener {
            toggle_share.isSelected = !toggle_share.isSelected
        }
        toggle_movies.setOnClickListener {
            toggle_movies.isSelected = !toggle_movies.isSelected
        }
        toggle_book.setOnClickListener {
            toggle_book.isSelected = !toggle_book.isSelected
        }
        toggle_song.setOnClickListener {
            toggle_song.isSelected = !toggle_song.isSelected
        }
    }

    override fun request(flag: Int) {
        val loginData = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(this, "users/${loginData.user_id}/whoareyouSetting", ShakePrivacyData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as ShakePrivacyData
                if (result.code == 0) {
                    if (result.data != null) {
                        shakeBean = result.data
                        toggle_share.isSelected = result.data.be_from_share == 0
                        toggle_movies.isSelected = result.data.be_from_movie == 0
                        toggle_book.isSelected = result.data.be_from_book == 0
                        toggle_song.isSelected = result.data.be_from_song == 0
                    }
                }
                Handler().postDelayed({
                    //延时300ms 给开关做动画
                    transLayout.showContent()
                }, 300)
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.6")
    }

    private fun updateSet() {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val body = FormBody.Builder()
                .add("BeFromShare", "${if (toggle_share.isSelected) 0 else 1}")
                .add("BeFromMovie", "${if (toggle_movies.isSelected) 0 else 1}")
                .add("BeFromBook", "${if (toggle_book.isSelected) 0 else 1}")
                .add("BeFromSong", "${if (toggle_song.isSelected) 0 else 1}")
                .build()
        OkClientHelper.post(this, "users/${loginBean.user_id}/whoareyouSetting", body, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    finish()
                } else {
                    showToast(result.msg)
                    transLayout.showContent()
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.6")
    }

    /**
     * 判断是否界面切换
     */
    private fun judeChanged(): Boolean {
        shakeBean?.let {
            val home = it.be_from_share != if (toggle_share.isSelected) 0 else 1
            val memory = it.be_from_movie != if (toggle_movies.isSelected) 0 else 1
            val machine = it.be_from_book != if (toggle_book.isSelected) 0 else 1
            val park = it.be_from_song != if (toggle_song.isSelected) 0 else 1
            return home or memory or machine or park
        }
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (judeChanged()) {
                updateSet()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}