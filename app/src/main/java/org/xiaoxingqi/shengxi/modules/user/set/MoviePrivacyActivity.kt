package org.xiaoxingqi.shengxi.modules.user.set

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.view.KeyEvent
import kotlinx.android.synthetic.main.activity_movie_privacy.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseThemeNoSwipeActivity
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.FirmData
import org.xiaoxingqi.shengxi.model.PatchData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class MoviePrivacyActivity : BaseThemeNoSwipeActivity() {
    private val REQUEST_SCAN_CODE = 0x00
    private var list = 0
    private var fileView: PatchData.FilmReview? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_movie_privacy
    }

    override fun initView() {
        tvTitle.text = resources.getString(R.string.string_talk_movies) + resources.getString(R.string.string_privacySettingAct_1)
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
        moreAbout.setOnClickListener {
            startActivityForResult(Intent(this, MovieScanPrivacyActivity::class.java)
                    .putExtra("type", 0)
                    .putExtra("group", list), REQUEST_SCAN_CODE)
        }
        toggleHomePage.setOnClickListener {
            toggleHomePage.isSelected = !toggleHomePage.isSelected
        }
        toggleHomePage.setOnToggleListener {
            tv_Save.isSelected = judeChanged()
        }
        toggleMemory.setOnClickListener {
            toggleMemory.isSelected = !toggleMemory.isSelected
        }
        toggleMemory.setOnToggleListener {
            tv_Save.isSelected = judeChanged()
        }
        toggleMachine.setOnClickListener {
            toggleMachine.isSelected = !toggleMachine.isSelected
        }
        toggleMachine.setOnToggleListener {
            tv_Save.isSelected = judeChanged()
        }
        toggleMoviePark.setOnClickListener {
            toggleMoviePark.isSelected = !toggleMoviePark.isSelected
        }
        toggleMoviePark.setOnToggleListener {
            tv_Save.isSelected = judeChanged()
        }
        tv_Save.setOnClickListener {
            if (tv_Save.isSelected) {
                updateSet()
            }
        }
        toggleMovie_details.setOnClickListener {
            toggleMovie_details.isSelected = !toggleMovie_details.isSelected
        }
        toggleMovie_smiler.setOnClickListener {
            toggleMovie_smiler.isSelected = !toggleMovie_smiler.isSelected
        }
        toggleMovie_attention.setOnClickListener {
            toggleMovie_attention.isSelected = !it.isSelected
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                val loginData = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "users/${loginData.user_id}/visibility/1", FirmData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as FirmData
                        if (result.code == 0) {
                            if (result.data != null) {
                                fileView = result.data
                                list = result.data.review
                                moreAbout.setMsgCount(if (result.data.review == 0) resources.getString(R.string.string_PrivacyAct_1) else resources.getString(R.string.string_PrivacyAct_2))
                                toggleHomePage.isSelected = result.data.homepage == 0
                                toggleMemory.isSelected = result.data.memory == 0
                                toggleMachine.isSelected = result.data.machine == 0
                                toggleMoviePark.isSelected = result.data.square == 0
                                toggleMovie_details.isSelected = result.data.entry == 0
                                toggleMovie_smiler.isSelected = result.data.hobby == 0
                                toggleMovie_attention.isSelected = result.data.subscribe == 0
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
        }
    }

    /**
     * 更新设置
     */
    private fun updateSet() {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val build = FormBody.Builder()
                .add("review", "$list")
                .add("homepage", "${if (toggleHomePage.isSelected) 0 else 1}")
                .add("memory", "${if (toggleMemory.isSelected) 0 else 1}")
                .add("machine", "${if (toggleMachine.isSelected) 0 else 1}")
                .add("square", "${if (toggleMoviePark.isSelected) 0 else 1}")
                .add("entry", "${if (toggleMovie_details.isSelected) 0 else 1}")
                .add("hobby", "${if (toggleMovie_smiler.isSelected) 0 else 1}")
                .add("subscribe", "${if (toggleMovie_attention.isSelected) 0 else 1}")
                .build()
        OkClientHelper.post(this, "users/${loginBean.user_id}/visibility/1", build, BaseRepData::class.java, object : OkResponse {
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
        fileView?.let {
            val review = list != it.review
            val home = it.homepage != if (toggleHomePage.isSelected) 0 else 1
            val memory = it.memory != if (toggleMemory.isSelected) 0 else 1
            val machine = it.machine != if (toggleMachine.isSelected) 0 else 1
            val park = it.square != if (toggleMoviePark.isSelected) 0 else 1
            val entry = it.entry != if (toggleMovie_details.isSelected) 0 else 1
            val hobby = it.hobby != if (toggleMovie_smiler.isSelected) 0 else 1
            val subscribe = it.subscribe != if (toggleMovie_attention.isSelected) 0 else 1
            return home or memory or machine or park or review or entry or hobby or subscribe
        }
        return false
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SCAN_CODE) {
                val group = data?.getIntExtra("group", 0)
                list = group!!
                moreAbout.setMsgCount(if (group == 0) resources.getString(R.string.string_PrivacyAct_1) else resources.getString(R.string.string_PrivacyAct_2))
                tv_Save.isSelected = judeChanged()
            }
        }
    }
}