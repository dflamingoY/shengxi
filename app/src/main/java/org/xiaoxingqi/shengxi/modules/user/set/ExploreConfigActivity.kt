package org.xiaoxingqi.shengxi.modules.user.set

import android.content.Intent
import kotlinx.android.synthetic.main.activity_explore_config.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.PatchData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

class ExploreConfigActivity : BaseAct() {
    private var topicPrivacy = 0
    override fun getLayoutId(): Int {
        return R.layout.activity_explore_config
    }

    override fun initView() {

    }

    override fun initData() {

    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        viewShake.setOnClickListener {
            startActivity(Intent(this, ShakePrivacyActivity::class.java))
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
        viewTopicVoice.setOnClickListener {
            startActivity(Intent(this, TopicShowPrivacy::class.java).putExtra("privacy", topicPrivacy))
        }
    }

    override fun onStart() {
        super.onStart()
        request(0)
    }

    override fun request(flag: Int) {
        val loginData = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(this, "users/${loginData.user_id}/setting", PatchData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as PatchData
                result.data?.let {
                    topicPrivacy = it.display_same_topic
                    viewTopicVoice.setMsgCount(if (topicPrivacy == 1) resources.getString(R.string.string_show) else resources.getString(R.string.string_unshow))
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }
}