package org.xiaoxingqi.shengxi.modules.user.set

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_topic_manager.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.model.AboutUserCommentData.CreateTopicData
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.ITopicChangeEvent
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.PatchData
import org.xiaoxingqi.shengxi.model.PrivacyTopicData
import org.xiaoxingqi.shengxi.model.SearchTopicData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.listen.EditTopicSearchActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.wedgit.UserTopicView

/**
 * 心情簿-关于的话题管理
 */
class TopicManagerActivity : BaseAct() {
    companion object {
        private const val REQUEST_SET = 0x00
        private const val REQUEST_TOPIC_1 = 0x01
        private const val REQUEST_TOPIC_2 = 0x02
        private const val REQUEST_TOPIC_3 = 0x03
    }

    private var group = 0
    override fun getLayoutId(): Int {
        return R.layout.activity_topic_manager
    }

    override fun initView() {
        userTopic1.setTitle(resources.getString(R.string.string_topic_loveTopic) + 1)
        userTopic2.setTitle(resources.getString(R.string.string_topic_loveTopic) + 2)
        userTopic3.setTitle(resources.getString(R.string.string_topic_loveTopic) + 3)
        ivDrawable.setImageResource(R.drawable.draw_privacy_home_user_voice_cover)
    }

    override fun initData() {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val topicData = PreferenceTools.getObj(this, IConstant.USER_TOPIC_CACHE + "_${loginBean.user_id}", PrivacyTopicData::class.java)
        if (topicData != null && topicData.data != null) {
            for (bean in topicData.data) {
                when (bean.weight) {
                    1 -> {
                        userTopic1.setTopic(bean.topic_name)
                    }
                    2 -> {
                        userTopic2.setTopic(bean.topic_name)
                    }
                    3 -> {
                        userTopic3.setTopic(bean.topic_name)
                    }
                }
            }
        }
        request(0)
        request(1)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        moreAbout.setOnClickListener {
            startActivityForResult(Intent(this, MovieScanPrivacyActivity::class.java)
                    .putExtra("type", 3)
                    .putExtra("title", resources.getString(R.string.string_topic_loveTopic1))
                    .putExtra("group", group), REQUEST_SET)
        }
        userTopic1.setOnClickListener { startActivityForResult(Intent(this, EditTopicSearchActivity::class.java), REQUEST_TOPIC_1) }
        userTopic2.setOnClickListener { startActivityForResult(Intent(this, EditTopicSearchActivity::class.java), REQUEST_TOPIC_2) }
        userTopic3.setOnClickListener { startActivityForResult(Intent(this, EditTopicSearchActivity::class.java), REQUEST_TOPIC_3) }
        userTopic1.setOnDeletedListener(deletedListener = object : UserTopicView.OnDeletedTopicListener {
            override fun deleted() {
                deleteTopic(1)
            }
        })
        userTopic2.setOnDeletedListener(deletedListener = object : UserTopicView.OnDeletedTopicListener {
            override fun deleted() {
                deleteTopic(2)
            }
        })
        userTopic3.setOnDeletedListener(deletedListener = object : UserTopicView.OnDeletedTopicListener {
            override fun deleted() {
                deleteTopic(3)
            }
        })
    }

    private fun update(flag: Int) {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(this, "users/" + loginBean.user_id + "/setting", FormBody.Builder().add("favoriteTopic", flag.toString()).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    group = flag
                    moreAbout.setMsgCount(if (flag == 0) resources.getString(R.string.string_PrivacyAct_1) else resources.getString(R.string.string_PrivacyAct_2))
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        when (flag) {
            0 -> {
                val loginData = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "users/${loginData.user_id}/setting", PatchData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as PatchData
                        transLayout.showContent()
                        if (result.code == 0) {
                            group = result.data.favorite_topic
                            moreAbout.setMsgCount(if (result.data.favorite_topic == 0) resources.getString(R.string.string_PrivacyAct_1) else resources.getString(R.string.string_PrivacyAct_2))
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            1 -> {
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "users/${loginBean.user_id}/favorite/topic", PrivacyTopicData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as PrivacyTopicData
                        if (result.code == 0) {
                            PreferenceTools.saveObj(this@TopicManagerActivity, IConstant.USER_TOPIC_CACHE + "_${loginBean.user_id}", result)
                            if (result.data != null) {
                                for (bean in result.data) {
                                    when (bean.weight) {
                                        1 -> {
                                            userTopic1.setTopic(bean.topic_name)
                                        }
                                        2 -> {
                                            userTopic2.setTopic(bean.topic_name)
                                        }
                                        3 -> {
                                            userTopic3.setTopic(bean.topic_name)
                                        }
                                    }
                                }
                            }
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V3.2")
            }
        }
    }

    /**
     * 删除喜欢的话题
     */
    private fun deleteTopic(weight: Int) {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.delete(this, "users/${loginBean.user_id}/favorite/topic/weight/$weight", FormBody.Builder().build(),
                BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                transLayout.showContent()
                if (result.code == 0) {
                    showToast("删除成功")
                    EventBus.getDefault().post(ITopicChangeEvent(loginBean.user_id))
                    request(1)
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.2")
    }

    /**
     * 更新最喜欢的话题
     */
    private fun updateTopic(weight: Int, topicId: String) {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(this, "users/${loginBean.user_id}/favorite/topic/weight/$weight", FormBody.Builder().add("topicId", topicId).build(),
                BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                transLayout.showContent()
                if (result.code == 0) {
                    showToast("设置成功")
                    EventBus.getDefault().post(ITopicChangeEvent(loginBean.user_id))
                    request(1)
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.2")
    }

    /**
     * 创建不存在的话题
     */
    private fun createTopic(topicName: String, index: Int) {
        OkClientHelper.post(this, "topics", FormBody.Builder().add("topicName", topicName).build(), CreateTopicData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as CreateTopicData
                if (result.code == 0) {
                    updateTopic(index, result.data.id.toString())
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SET) {
                val group = data?.getIntExtra("group", 0)
                update(group!!)
            } else if (requestCode == REQUEST_TOPIC_1) {
                val topicBean = data?.getParcelableExtra<SearchTopicData.SearchTopicBean>("topicBean")
                userTopic1.setTopic(topicBean?.topic_name!!)
                if (TextUtils.isEmpty(topicBean?.topic_id)) {
                    createTopic(topicBean!!.topic_name, 1)
                } else {
                    updateTopic(1, topicBean.topic_id)
                }
            } else if (requestCode == REQUEST_TOPIC_2) {
                val topicBean = data?.getParcelableExtra<SearchTopicData.SearchTopicBean>("topicBean")
                userTopic2.setTopic(topicBean?.topic_name!!)
                if (TextUtils.isEmpty(topicBean?.topic_id)) {
                    createTopic(topicBean!!.topic_name, 2)
                } else {
                    updateTopic(2, topicBean.topic_id)
                }
            } else if (requestCode == REQUEST_TOPIC_3) {
                val topicBean = data?.getParcelableExtra<SearchTopicData.SearchTopicBean>("topicBean")
                userTopic3.setTopic(topicBean?.topic_name!!)
                if (TextUtils.isEmpty(topicBean?.topic_id)) {
                    createTopic(topicBean!!.topic_name, 3)
                } else {
                    updateTopic(3, topicBean.topic_id)
                }
            }
        }
    }
}