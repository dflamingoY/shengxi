package org.xiaoxingqi.shengxi.core

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.http.GlideJudeUtils
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.ItemOperator
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.home.REQUEST_RECORD
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.modules.showAchieve
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.TransLayout
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang

abstract class BaseNormalActivity : BaseObjectActivity() {
    protected lateinit var glideUtil: GlideJudeUtils
    protected var commentBean: BaseBean? = null
    protected var dialogPwd: DialogCommitPwd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSystem()
        glideUtil = GlideJudeUtils(this)
        setContentView(getLayoutId())
        initView()
        initData()
        initEvent()
    }

    abstract fun getLayoutId(): Int
    abstract fun initView()
    abstract fun initData()
    abstract fun initEvent()
    open fun request(flag: Int) {}

    fun initSystem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (Build.DISPLAY.contains("flyme", true))
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            } catch (e: Exception) {
            }
        }*/
    }

    /**
     * 弹共享次数完
     */
    fun showEmptyCount(count: String) {
        val toast = Toast(this)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.duration = Toast.LENGTH_SHORT
        val view = LayoutInflater.from(this).inflate(R.layout.toast_no_share_count, null, false)
        view.findViewById<TextView>(R.id.tv_Share_Count_hint).text = resources.getString(R.string.string_50) + count
        toast.view = view
        toast.show()
    }

    fun addPlays(bean: BaseBean, textView: TextView?, function: (BaseBean) -> Unit) {
        OkClientHelper.get(this, "users/${bean.user_id}/voices/${bean.voice_id}", BaseRepData::class.java, object : OkResponse {
            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                bean.played_num++
                textView?.text = "${resources.getString(R.string.string_Listener)} ${bean.played_num}"
                if ((result as BaseRepData).code == 0)
                    function(bean)
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    /**
     * 查询共享次数
     */
    fun queryCount(bean: BaseBean, textView: TextView, imageView: View, transLayout: TransLayout) {
        transLayout.showProgress()
        OkClientHelper.get(this, "voices/share/statistics", RecommendCountData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as RecommendCountData
                transLayout.showContent()
                if (result.code == 0) {
                    if (bean.is_shared == "1") {//取消共享
                        DialogUnShareWorld(this@BaseNormalActivity).setCountText(result.data.share_num.toString()).setOnClickListener(View.OnClickListener {
                            unShare(bean, textView, imageView, transLayout)
                        }).show()
                    } else {
                        if (result.data.can_share == 1 && result.data.left_times > 0) {
                            DialogShareWorld(this@BaseNormalActivity).setCountText(result.data.tips).setOnClickListener(View.OnClickListener {
                                share(bean, textView, imageView, transLayout)
                            }).show()
                        } else {
                            showEmptyCount(((result.data.nearby_at - System.currentTimeMillis() / 1000).toInt() / 60f).toInt().toString() + "分钟")
                        }
                    }
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    /**
     * 共享到世界
     */
    private fun share(bean: BaseBean, textView: TextView, imageView: View, transLayout: TransLayout) {
        transLayout.showProgress()
        OkClientHelper.post(this, "voices/share", FormBody.Builder().add("voiceId", bean.voice_id)
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    bean.is_shared = "1"
                    imageView.isSelected = true
                    textView.text = resources.getString(R.string.string_unshare_world)
                    showToast(resources.getString(R.string.string_shared_world))
                    operator?.onRecommend(bean)
                } else {
                    operator?.onFailure(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                operator?.onFailure(any)
                transLayout.showContent()
            }
        })
    }

    /**
     * 取消共享到世界
     */
    private fun unShare(bean: BaseBean, textView: TextView, imageView: View, transLayout: TransLayout) {
        transLayout.showProgress()
        OkClientHelper.delete(this, "users/${bean.user_id}/voices/${bean.voice_id}/share", FormBody.Builder()
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    bean.is_shared = "0"
                    imageView.isSelected = false
                    textView.text = resources.getString(R.string.string_share_world)
                    showToast(resources.getString(R.string.string_unShare_Success))
                    operator?.onUnRecommend(bean)
                } else {
                    operator?.onFailure(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                operator?.onFailure(any)
                transLayout.showContent()
            }
        })
    }

    /**
     * 查询是否能够回声
     */
    fun queryPermission(bean: BaseBean, transLayout: TransLayout, sendPath: String? = null) {
        transLayout.showProgress()
        OkClientHelper.get(this, "chats/check/${bean.user_id}/${bean.voice_id}", ChechOutReplyData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as ChechOutReplyData).code == 0) {
                    commentBean = bean
                    startActivityForResult(Intent(this@BaseNormalActivity, RecordVoiceActivity::class.java)
                            .putExtra("avatar", bean.user.avatar_url)
                            .putExtra("sendPath", sendPath ?: "")
                            .putExtra("isBusy", result.data.auto_reply)
                            .putExtra("hobby", if (result.data.chat_hobby != null) result.data.chat_hobby.name else "")
                            , REQUEST_RECORD)
                    overridePendingTransition(0, 0)
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    /**
     * 取消共鸣 禁止频繁点击取消共鸣
     */
    fun unThumb(bean: BaseBean, view: View) {
        view.isSelected = false
        OkClientHelper.delete(this, "users/${bean.user_id}/voices/${bean.voice_id}/collection", FormBody.Builder()
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
//                SmallBang.attach2Window(this@BaseNormalActivity).bang(view, 60f, null)
                if ((result as BaseRepData).code == 0) {
                    bean.is_collected = 0
                    view.isSelected = false
                    operator?.onUnThumb(bean)
                } else {
                    operator?.onFailure(result.msg)
                }
                bean.isNetStatus = false
            }

            override fun onFailure(any: Any?) {
                operator?.onFailure(any)
                bean.isNetStatus = false
            }
        })
    }

    /**
     * 共鸣
     */
    fun thumb(bean: BaseBean, view: View?, delay: Int = 0) {
        OkClientHelper.post(this, "users/${bean.user_id}/voices/${bean.voice_id}/collection", FormBody.Builder()
                .apply {
                    if (delay != 0)
                        this.add("needDelay", "$delay")
                }
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                view?.let {
                    SmallBang.attach2Window(this@BaseNormalActivity).bang(it, 60f, null)
                }
                if ((result as BaseRepData).code == 0) {
                    bean.is_collected = 1
                    view?.isSelected = true
                    operator?.onthumb(if (delay == 0) null else bean)
                } else {
                    operator?.onFailure(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                operator?.onFailure(any)
            }
        })
    }

    /**
     * 举报
     */
    fun report(id: String, type: String, toUserId: String) {
        val formBody = FormBody.Builder()
                .add("reasonType", type)
                .add("resourceType", "1")
                .add("resourceId", id)
                .add("toUserId", toUserId)
                .build()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "users/${loginBean.user_id}/shield", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    showToast(resources.getString(R.string.string_report_success))
                    operator?.onReport(type)
                } else {
                    operator?.onFailure(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                operator?.onFailure(any)
            }
        }, "V3.4")
    }

    /**
     * 请求添加好友
     */
    fun requestFriends(bean: BaseBean, view: View) {
        val formBody = FormBody.Builder()
                .add("toUserId", bean.user_id.toString())
                .build()
        val loginBean = PreferenceTools.getObj(this@BaseNormalActivity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "users/${loginBean.user_id}/friendslog", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    view.isSelected = false
                    operator?.onFriend()
                    if (SPUtils.getInt(this@BaseNormalActivity, IConstant.TOTALLENGTH + loginBean.user_id, 0) == 0) {
                        DialogCreateVoice(this@BaseNormalActivity).show()
                    } else if (SPUtils.getBoolean(this@BaseNormalActivity, IConstant.STRANGEVIEW + loginBean.user_id, false)) {
                        DialogUserSet(this@BaseNormalActivity).setOnClickListener(View.OnClickListener {
                            addWhiteList(bean.user_id.toString())
                        }).show()
                    }
                } else {
                    if (SPUtils.getLong(this@BaseNormalActivity, IConstant.USERLIMITTIME, 0L) > System.currentTimeMillis() / 1000) {
                        DialogLimitTalk(this@BaseNormalActivity).show()
                        operator?.onFailure("")
                    } else
                        operator?.onFailure(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                operator?.onFailure(any)
            }
        })
    }

    /**
     * 将用户加入白名单
     */
    fun addWhiteList(userId: String) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "users/${loginBean.user_id}/whitelist", FormBody.Builder().add("toUserId", userId).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    showToast("已经对方加入白名单")
                    DialogAddWhiteHint(this@BaseNormalActivity).show()
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V3.6")
    }

    /**
     *共享到世界,
     * 设置私密操作
     * 设置封面图
     */
    fun setVoicePrivacy(bean: BaseBean) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(this, "users/${loginBean.user_id}/voices/${bean.voice_id}", FormBody.Builder().add("privateStatus", if (bean.is_private == 1) "0" else "1").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    bean.is_private = if (bean.is_private == 1) 0 else 1
                    bean.chat_num = 0
                    bean.is_shared = "0"
                    operator?.onPrivacy(bean)
                    if (bean.is_private == 1) {
                        showToast("设置隐私成功")
                    } else {
                        showToast("取消隐私成功")
                    }
                } else {
                    operator?.onFailure(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                operator?.onFailure(any)
            }
        })
    }


    /**
     * 取消关注
     */
    fun deletedSubscriber(bean: BaseBean) {
        OkClientHelper.delete(this, "userSubscription/${bean.subscription_id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    bean.subscription_id = 0
                    operator?.onUnRecommend(bean)//三个广场页替用推荐回调
                    showToast("已取消关注")
                } else {
                    operator?.onFailure(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                operator?.onFailure(any)
            }
        }, "V4.1")
    }

    /**
     * 添加关注
     */
    fun addSubscriber(bean: BaseBean) {
        OkClientHelper.post(this, "userSubscription", FormBody.Builder().add("toUserId", "${bean.user_id}").add("voiceId", bean.voice_id).build(), IntegerRespData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as IntegerRespData
                if (result.code == 0) {
                    try {
                        bean.subscription_id = result.data.id
                        operator?.onRecommend(bean)//三个广场页替用推荐回调
                        showToast("关注成功!在「" + when (bean.resource_type) {
                            1 -> "聊电影"
                            2 -> "读过的书"
                            3 -> "唱回忆"
                            else -> "聊电影"
                        } + "」广场页收听")
                    } catch (e: Exception) {
                    }
                } else {
                    operator?.onFailure(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                operator?.onFailure(any)
            }
        }, "V4.1")
    }

    /**
     * 举报四条, 非法
     */
    fun reportNormalItem(userId: String, type: String, vararg resourceType: String) {
        val formBody = FormBody.Builder()
                .add("resourceId", userId)
                .add("reportType", type)
                .add("resourceType", if (resourceType.isNotEmpty()) resourceType[0] else "1")
                .build()
        OkClientHelper.post(this, "reports", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0)
//                    showToast(resources.getString(R.string.string_report_success))
                    DialogReportSuccess(this@BaseNormalActivity).show()
                else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }


    /**
     * 超管设置该条声兮为隐私状态
     */
    fun adminSetPrivacy(formBody: FormBody, voiceBean: BaseBean) {
        OkClientHelper.patch(this, "admin/voices/${voiceBean.voice_id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    operator?.onAdminPrivacy(voiceBean)
                    showToast("操作已执行")
                } else {
                    operator?.onAdminFail()
                }
            }

            override fun onFailure(any: Any?) {
                operator?.onFailure(any)
            }
        })
    }

    fun pushTravel(userId: String) {
        OkClientHelper.post(this, "achievement", FormBody.Builder().add("achievementType", "2").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    SPUtils.setString(this@BaseNormalActivity, IConstant.FIRST_PUSH_ACHIEVEMENT + userId, TimeUtils.parseCalender((System.currentTimeMillis() / 1000).toInt()))
                    showAchieve(type = 1)
                } else
                    showToast(result.msg)
            }

            override fun onFailure(any: Any?) {

            }
        }, "V4.3")

    }

    /**
     * 弹土司
     */
    fun showToast(text: String?) {
        if (!TextUtils.isEmpty(text))
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    fun showToastInLocation(text: String) {
        val makeText = Toast.makeText(this, text, Toast.LENGTH_SHORT)
        makeText.setGravity(Gravity.CENTER, 0, 0)
        makeText.show()
    }

    private var operator: ItemOperator? = null

    protected fun setItemOperator(operator: ItemOperator) {
        this.operator = operator
    }

    override fun onDestroy() {
        super.onDestroy()
        glideUtil.cancelAll()
    }
}