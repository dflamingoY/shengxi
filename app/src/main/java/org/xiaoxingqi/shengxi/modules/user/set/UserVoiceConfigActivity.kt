package org.xiaoxingqi.shengxi.modules.user.set

import android.content.Intent
import kotlinx.android.synthetic.main.activity_user_voice_config.*
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.NewVersionSetData
import org.xiaoxingqi.shengxi.model.NewVersionSetSingleData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class UserVoiceConfigActivity : BaseAct() {

    private var machine = 2
    private var openVoices = 7
    private var movies = 1
    private var books = 1
    private var songs = 1
    private var shareVoice = 2
    private var showAchievement = 1
    private var dubbing = -1
    private var canvas = -1
    override fun getLayoutId(): Int {
        return R.layout.activity_user_voice_config
    }

    override fun initView() {

    }

    override fun initData() {
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        viewMachine.setOnClickListener {
            startActivity<CoverMiniMachineConfigActivity>("value" to machine)
        }
        moreOpenVoices.setOnClickListener {
            startActivity<PrivacyPhotoActivity>("privacy" to openVoices)
        }
        moreMovies.setOnClickListener {
            startActivity<UserResourceConfigActivity>("type" to 1, "value" to movies)
        }
        moreBook.setOnClickListener {
            startActivity<UserResourceConfigActivity>("type" to 2, "value" to books)
        }
        moreMusic.setOnClickListener {
            startActivity<UserResourceConfigActivity>("type" to 3, "value" to songs)
        }
        viewShareVoices.setOnClickListener {
            startActivity<UserMoodListShowConfigActivity>("value" to shareVoice)
        }
        more_topic_privacy.setOnClickListener {
            startActivity(Intent(this, TopicManagerActivity::class.java))
        }
        viewAchieve.setOnClickListener {
            startActivity(Intent(this, AchievementVisibleActivity::class.java).putExtra("value", showAchievement))
        }
        moreDubbing.setOnClickListener {
            if (dubbing != -1)
                startActivity<UserDubbingConfigActivity>("value" to dubbing)
        }
        moreCanvas.setOnClickListener {
            if (canvas != -1)
                startActivity<UserCanvasConfigActivity>("value" to canvas)
        }
    }

    override fun onStart() {
        super.onStart()
        request(0)
    }

    override fun request(flag: Int) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(this, "users/${loginBean.user_id}/settings?settingName=&settingTag=moodbook", NewVersionSetData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as NewVersionSetData
                result.data?.let { list ->
                    list.forEach {
                        when (it.setting_name) {
                            "minimachine_visibility" -> {//迷你时光机
                                machine = it.setting_value
                                viewMachine.setMsgCount(resources.getString(if (it.setting_value == 1) R.string.string_voice_style_setting_7 else R.string.string_privacyAct_17))
                            }
                            "voice_visible_days" -> {//可见性
                                openVoices = it.setting_value
                                moreOpenVoices.setMsgCount(when (it.setting_value) {
                                    -1 -> "全部"
                                    0 -> resources.getString(R.string.string_PrivacyAct_4)
                                    30 -> "最近三十天"
                                    else -> resources.getString(R.string.string_PrivacyAct_3)
                                })
                            }
                            "movie_voice_visibility" -> {
                                movies = it.setting_value
                                moreMovies.setMsgCount(resources.getString(if (it.setting_value == 1) R.string.string_PrivacyAct_1 else R.string.string_PrivacyAct_2))
                            }
                            "book_voice_visibility" -> {
                                books = it.setting_value
                                moreBook.setMsgCount(resources.getString(if (it.setting_value == 1) R.string.string_PrivacyAct_1 else R.string.string_PrivacyAct_2))
                            }
                            "song_voice_visibility" -> {
                                songs = it.setting_value
                                moreMusic.setMsgCount(resources.getString(if (it.setting_value == 1) R.string.string_PrivacyAct_1 else R.string.string_PrivacyAct_2))
                            }
                            "share_voice_visibility" -> {//心情簿参观者权限
                                shareVoice = it.setting_value
                                viewShareVoices.setMsgCount(resources.getString(if (it.setting_value == 2) R.string.string_privacyAct_14 else R.string.string_privacyAct_24))
                            }
                            "dubbing_visibility" -> {
                                dubbing = it.setting_value
                                moreDubbing.setMsgCount(resources.getString(if (it.setting_value == 1) R.string.string_PrivacyAct_1 else if (it.setting_value == 2) R.string.string_PrivacyAct_2 else R.string.string_just_self_visible_title))
                            }
                            "artwork_visibility" -> {
                                canvas = it.setting_value
                                moreCanvas.setMsgCount(resources.getString(if (it.setting_value == 1) R.string.string_PrivacyAct_1 else if (it.setting_value == 2) R.string.string_PrivacyAct_2 else R.string.string_just_self_visible_title))
                            }
                        }
                    }
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V4.2")
        OkClientHelper.get(this, "users/${loginBean.user_id}/settings?settingName=achievement_visibility&settingTag=other", NewVersionSetSingleData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as NewVersionSetSingleData
                result.data?.let { list ->
                    if (list.setting_name == "achievement_visibility") {
                        showAchievement = list.setting_value
                        viewAchieve.setMsgCount(if (list.setting_value == 1) "展示" else "不展示")
                    }
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V4.2")
    }
}