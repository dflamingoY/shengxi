package org.xiaoxingqi.shengxi.modules.user

import android.content.Intent
import android.view.View
import kotlinx.android.synthetic.main.activity_privacy.*
import kotlinx.android.synthetic.main.activity_privacy.btn_Back
import okhttp3.FormBody
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogRestoreSetting
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.NewVersionSetData
import org.xiaoxingqi.shengxi.model.PatchData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.echoes.ChatActivity
import org.xiaoxingqi.shengxi.modules.user.set.*
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

const val LOCATIONSERVER = 0x0000

class PrivacyActivity : BaseAct() {
    private var privacyBean: PatchData.PatchBean? = null
    private var showAchievement = 1
    private var showLight = 1
    override fun getLayoutId(): Int {
        return R.layout.activity_privacy
    }

    override fun initView() {

    }

    override fun initData() {

    }

    override fun onStart() {
        super.onStart()
        request(0)
    }

    override fun onResume() {
        super.onResume()
        request(2)
    }

    override fun initEvent() {
        moreReceived.setOnClickListener {
            privacyBean?.let {
                startActivity(Intent(this, PrivacySelectActivity::class.java)
                        .putExtra("privacy", it.chat_with)
                        .putExtra("isComment", true)
                )
            }
        }

        moreBlack.setOnClickListener {
            startActivity(Intent(this, BlackListActivity::class.java))
        }
//        moreLocation.setOnClickListener {
//            startActivityForResult(Intent(this, LocationServerActivity::class.java), LOCATIONSERVER)
//        }
        moreChat.setOnClickListener {
            privacyBean?.let {
                startActivity(Intent(this, PrivacySelectActivity::class.java)
                        .putExtra("privacy", it.chat_pri_with)
                        .putExtra("isComment", false)
                )
            }
        }
        btn_Back.setOnClickListener { finish() }
        /* moreOpenPhoto.setOnClickListener {
             privacyBean?.let {
                 startActivity(Intent(this, PrivacyPhotoActivity::class.java).putExtra("privacy", it.strange_view))
             }
         }
         moreMovies.setOnClickListener {
             startActivity(Intent(this, MoviePrivacyActivity::class.java))
         }
         moreBook.setOnClickListener {
             startActivity(Intent(this, BookPrivacyActivity::class.java))
         }
         moreMusic.setOnClickListener {
             startActivity(Intent(this, MusicPrivacyActivity::class.java))
         }
         moreTopic.setOnClickListener {
             startActivity(Intent(this, TopicManagerActivity::class.java))
         }
         more_shake.setOnClickListener { startActivity(Intent(this, ShakePrivacyActivity::class.java)) }
         more_topic_privacy.setOnClickListener {
             privacyBean?.let {
                 startActivity(Intent(this, TopicShowPrivacy::class.java).putExtra("privacy", it.display_same_topic))
             }
         }*/

        tvRestore.setOnClickListener {
            DialogRestoreSetting(this).setOnClickListener(View.OnClickListener {
                request(1)
            }).show()
        }
        viewVoices.setOnClickListener {
            startActivity(Intent(this, UserVoiceConfigActivity::class.java))
        }
        viewListen.setOnClickListener {
            startActivity(Intent(this, ExploreConfigActivity::class.java))
        }
        relative_Custom.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java)
                    .putExtra("uid", "1")
                    .putExtra("userName", "声昔小二")
                    .putExtra("unreadCount", 0)
                    .putExtra("chatId", "")
            )
        }
        viewAchieve.setOnClickListener {
            startActivity(Intent(this, AchievementVisibleActivity::class.java).putExtra("value", showAchievement))
        }
        moreLight.setOnClickListener {
            startActivity<LightPrivacyActivity>("value" to showLight)
        }
    }

    override fun request(flag: Int) {
        val loginData = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "users/${loginData.user_id}/setting", PatchData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as PatchData
                        result.data?.let {
                            privacyBean = it
                            if (it.chat_pri_with == 0) {
                                moreChat.setMsgCount(resources.getString(R.string.string_PrivacyAct_1))
                            } else {
                                moreChat.setMsgCount(resources.getString(R.string.string_PrivacyAct_2))
                            }
                            if (it.chat_with == 0) {//回声
                                moreReceived.setMsgCount(resources.getString(R.string.string_PrivacyAct_1))
                            } else {
                                moreReceived.setMsgCount(resources.getString(R.string.string_PrivacyAct_2))
                            }
                            /*moreOpenPhoto.setMsgCount(when (it.strange_view) {
                                0 -> resources.getString(R.string.string_PrivacyAct_4)
                                7 -> resources.getString(R.string.string_PrivacyAct_3)
                                30 -> "最近三十天"
                                -1 -> "全部"
                                else -> resources.getString(R.string.string_PrivacyAct_4)
                            })*/
                            if (it.gps_switch == 1) {
                                moreLocation.setMsgCount(resources.getString(R.string.string_statue_on))
                            } else {
                                moreLocation.setMsgCount(resources.getString(R.string.string_statue_off))
                            }
//                    more_topic_privacy.setMsgCount(if (it.display_same_topic == 1) resources.getString(R.string.string_show) else resources.getString(R.string.string_unshow))
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            1 -> {
                transLayout.showProgress()
                OkClientHelper.patch(this, "users/${loginData.user_id}/settings/init", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }

                    override fun success(result: Any?) {
                        transLayout.showContent()
                        if ((result as BaseRepData).code == 0) {
                            showToast("已恢复默认设置")
                            request(0)
                            request(2)
                        } else
                            showToast(result.msg)
                    }
                }, "V4.2")
            }
            2 -> {
                OkClientHelper.get(this, "users/${loginData.user_id}/settings?settingName=&settingTag=other", NewVersionSetData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as NewVersionSetData
                        result.data?.let { list ->
                            list.forEach {
                                if (it.setting_name == "achievement_visibility") {
                                    showAchievement = it.setting_value
                                    viewAchieve.setMsgCount(if (it.setting_value == 1) "展示" else "不展示")
                                } else if (it.setting_name == "voice_collection_visibility") {
                                    showLight = it.setting_value
                                    moreLight.setMsgCount(if (it.setting_value == 1) "展示" else "不展示")
                                }
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.2")
            }
        }
    }
}