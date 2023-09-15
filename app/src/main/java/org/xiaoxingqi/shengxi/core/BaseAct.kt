package org.xiaoxingqi.shengxi.core

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
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
import org.xiaoxingqi.shengxi.impl.DialogAdminPwdListener
import org.xiaoxingqi.shengxi.impl.ItemOperator
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.alarm.BaseAlarmBean
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.home.REQUEST_RECORD
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils
import org.xiaoxingqi.shengxi.utils.TimeUtils
import org.xiaoxingqi.shengxi.wedgit.TransLayout
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang

abstract class BaseAct : BaseActivity() {
    protected var commentBean: BaseBean? = null
    protected lateinit var glideUtil: GlideJudeUtils
    protected var dialogPwd: DialogCommitPwd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    open fun request(flag: Int) {

    }

    fun initSystem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
            val decorView = window.decorView
            //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
            val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            decorView.systemUiVisibility = option
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }


    fun showToast(text: String) {
        if (!TextUtils.isEmpty(text))
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    /**
     * 弹共享次数完
     */
    @SuppressLint("InflateParams")
    fun showEmptyCount(count: String) {
        val toast = Toast(this)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.duration = Toast.LENGTH_SHORT
        val view = LayoutInflater.from(this).inflate(R.layout.toast_no_share_count, null, false)
        view.findViewById<TextView>(R.id.tv_Share_Count_hint).text = resources.getString(R.string.string_50) + count
        toast.view = view
        toast.show()
    }

    /**
     * 删除
     */
    fun delete(bean: BaseBean) {
        OkClientHelper.delete(this, "users/${bean.user_id}/voices/${bean.voice_id}", FormBody.Builder()
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    operator?.onDelete(bean)
                    queryVoicesStatus(bean.created_at)


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
     * 查询当天是否有心情
     */
    fun queryVoicesStatus(createAt: Int = 0) {
        val date = TimeUtils.parseCalender((System.currentTimeMillis() / 1000).toInt())
        if (TimeUtils.parseCalender(createAt) != date) {
            return
        }
        val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(this, "users/${obj.user_id}/voices/calendar/$date", VoiceData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as VoiceData
                if (result.data == null || result.data.size == 0) {//有数据
                    SPUtils.setString(this@BaseAct, IConstant.FIRST_PUSH_VOICES + obj.user_id, "")
                }
            }

            override fun onFailure(any: Any?) {
            }
        }, "V4.2")
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
     * 查询是否能够回声
     */
    fun queryPermission(bean: BaseBean, transLayout: TransLayout, sendPath: String? = null) {
        transLayout.showProgress()
        OkClientHelper.get(this, "chats/check/${bean.user_id}/${bean.voice_id}", ChechOutReplyData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as ChechOutReplyData).code == 0) {
                    commentBean = bean
                    startActivityForResult(Intent(this@BaseAct, RecordVoiceActivity::class.java)
                            .putExtra("avatar", bean.user.avatar_url)
                            .putExtra("sendPath", sendPath ?: "")
                            .putExtra("isBusy", result.data.auto_reply)
                            .putExtra("hobby", if (result.data.chat_hobby != null) result.data.chat_hobby.name else "")
                            , REQUEST_RECORD)
                    overridePendingTransition(0, 0)
                } else {
                    if (SPUtils.getLong(this@BaseAct, IConstant.USERLIMITTIME, 0L) > System.currentTimeMillis() / 1000) {
                        DialogLimitTalk(this@BaseAct).show()
                    } else
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
     * 查询共享次数
     */
    fun queryCount(bean: BaseBean, textView: TextView, imageView: View) {
        OkClientHelper.get(this, "voices/share/statistics", RecommendCountData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as RecommendCountData
                if (result.code == 0) {

                    if (bean.is_shared == "1") {//取消共享
                        DialogUnShareWorld(this@BaseAct).setCountText(result.data.share_num.toString()).setOnClickListener(View.OnClickListener {
                            unShare(bean, textView, imageView)
                        }).show()
                    } else {
                        if (result.data.can_share == 1 && result.data.left_times > 0) {
                            DialogShareWorld(this@BaseAct).setCountText(result.data.tips).setOnClickListener(View.OnClickListener {
                                share(bean, textView, imageView)
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

            }
        })
    }

    /**
     * 取消共享到世界
     */
    private fun unShare(bean: BaseBean, textView: TextView, imageView: View) {
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
            }

            override fun onFailure(any: Any?) {
                operator?.onFailure(any)
            }
        })
    }

    /**
     * 共享到世界
     */
    private fun share(bean: BaseBean, textView: TextView, imageView: View) {
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
            }

            override fun onFailure(any: Any?) {
                operator?.onFailure(any)
            }
        })
    }

    /**
     * 取消共鸣
     */
    fun unThumb(bean: BaseBean, view: View) {
        view.isSelected = false
        OkClientHelper.delete(this, "users/${bean.user_id}/voices/${bean.voice_id}/collection", FormBody.Builder()
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
//                SmallBang.attach2Window(this@BaseAct).bang(view, 60f, null)
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
                bean.isNetStatus = false
                operator?.onFailure(any)
            }
        })
    }

    /**
     * 共鸣
     */
    fun thumb(bean: BaseBean, view: View?, needDelay: Int = 0) {
        OkClientHelper.post(this, "users/${bean.user_id}/voices/${bean.voice_id}/collection", FormBody.Builder()
                .apply {
                    if (needDelay != 0)
                        this.add("needDelay", "$needDelay")
                }
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    bean.is_collected = 1
                    view?.isSelected = true
                    view?.let {
                        SmallBang.attach2Window(this@BaseAct).bang(it, 60f, null)
                    }
                    operator?.onthumb(if (needDelay == 0) null else bean)
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
     * 请求添加好友
     */
    fun requestFriends(bean: BaseBean, view: View) {
        val formBody = FormBody.Builder()
                .add("toUserId", bean.user_id.toString())
                .build()
        val loginBean = PreferenceTools.getObj(this@BaseAct, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "users/${loginBean.user_id}/friendslog", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    view.isSelected = false
                    operator?.onFriend()
                    if (SPUtils.getInt(this@BaseAct, IConstant.TOTALLENGTH + loginBean.user_id, 0) == 0) {
                        DialogCreateVoice(this@BaseAct).show()
                    } else if (SPUtils.getBoolean(this@BaseAct, IConstant.STRANGEVIEW + loginBean.user_id, false)) {
                        DialogUserSet(this@BaseAct).setOnClickListener(View.OnClickListener {
                            addWhiteList(bean.user_id.toString())
                        }).show()
                    }
                } else {
                    if (SPUtils.getLong(this@BaseAct, IConstant.USERLIMITTIME, 0L) > System.currentTimeMillis() / 1000) {
                        DialogLimitTalk(this@BaseAct).show()
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
                    DialogAddWhiteHint(this@BaseAct).show()
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V3.6")
    }

    /**
     * 添加播放次数
     */
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
     * 回声
     */
    fun comment(formBody: FormBody) {
        OkClientHelper.post(this, "chats/${commentBean?.user_id}/${commentBean?.voice_id}", formBody, EchoesData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as EchoesData).code == 0) {
                    showToast(resources.getString(R.string.string_echo_Success))
                    operator?.onComment(result.data.chat_id)
                } else {
                    operator?.onFailure(result)
                }
            }

            override fun onFailure(any: Any?) {
                operator?.onFailure(any)
            }
        })
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
                    DialogReportSuccess(this@BaseAct).show()
                else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
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

    fun sendPwdMsg(userId: String) {
        OkClientHelper.post(this, "loginCheckCode/sms", FormBody.Builder().add("toUserId", userId).build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    showToast("短信已发送")
                } else
                    showToast(result.msg)
            }
        }, "V4.3")
    }

    fun adminPick(item: BaseAlarmBean, formBody: FormBody) {
        OkClientHelper.patch(this, "admin/dubbings/${item.id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    //删除成功
                    showToast("操作成功")
                    dialogPwd?.dismiss()
                } else {
                    dialogPwd?.setCallBack()
                }
            }

            override fun onFailure(any: Any?) {
                dialogPwd?.setCallBack()
            }
        })
    }

    fun checkPickStatus(item: BaseAlarmBean) {
        OkClientHelper.get(this, "dubbings/${item.id}/pickStatus", IntegerRespData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
            }

            override fun success(result: Any?) {
                result as IntegerRespData
                if (result.code == 0) {
                    when (result.data.pick_status) {
                        0 -> {//未设置过
                            dialogPwd = DialogCommitPwd(this@BaseAct).setOperator("pick", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                adminPick(item, FormBody.Builder().add("confirmPasswd", pwd).add("pickedAt", "${System.currentTimeMillis() / 1000}").build())
                            })
                            dialogPwd?.show()
                        }
                        1 -> {//正在
                            showToast("当前配音正被设备声昔君pick")
                        }
                        2 -> {//设置过
                            DialogDeleteWording(this@BaseAct).setOtherTitle(resources.getString(R.string.string_alarm_setting_pick_1), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                                dialogPwd = DialogCommitPwd(this@BaseAct).setOperator("pick", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                    adminPick(item, FormBody.Builder().add("confirmPasswd", pwd).add("pickedAt", "${System.currentTimeMillis() / 1000}").build())
                                })
                                dialogPwd?.show()
                            }).show()
                        }
                    }
                }
            }
        }, "V4.3")
    }

    fun checkArtTopStatus(bean: PaintData.PaintBean) {
        OkClientHelper.get(this, "artworks/${bean.id}/pick", IntegerRespData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                result as IntegerRespData
                if (result.code == 0) {
                    if (result.data == null) {
                        dialogPwd = DialogCommitPwd(this@BaseAct).setOperator("topAt", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                            addArtTop(bean, FormBody.Builder().add("confirmPasswd", pwd).add("topAt", "${System.currentTimeMillis() / 1000}").build())
                        })
                        dialogPwd?.show()
                    } else
                        when (result.data.top_status) {
                            0 -> {//未设置过
                                dialogPwd = DialogCommitPwd(this@BaseAct).setOperator("topAt", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                    addArtTop(bean, FormBody.Builder().add("confirmPasswd", pwd).add("topAt", "${System.currentTimeMillis() / 1000}").build())
                                })
                                dialogPwd?.show()
                            }
                            1 -> {//正在
                                showToast("当前作品正被设为今日推荐")
                            }
                            2 -> {//设置过
                                DialogDeleteWording(this@BaseAct).setOtherTitle("该作品曾被推荐过,确定再次推荐", resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                                    dialogPwd = DialogCommitPwd(this@BaseAct).setOperator("topAt", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                        addArtTop(bean, FormBody.Builder().add("confirmPasswd", pwd).add("topAt", "${System.currentTimeMillis() / 1000}").build())
                                    })
                                    dialogPwd?.show()
                                }).show()
                            }
                            3 -> {
                                DialogDeleteWording(this@BaseAct).setOtherTitle("该作品曾被推荐过,确定再次推荐", resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                                    dialogPwd = DialogCommitPwd(this@BaseAct).setOperator("topAt", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                        addArtTop(bean, FormBody.Builder().add("confirmPasswd", pwd).add("topAt", "${System.currentTimeMillis() / 1000}").build())
                                    })
                                    dialogPwd?.show()
                                }).show()
                            }
                        }
                }
            }
        }, "V4.3")
    }

    fun addArtTop(bean: PaintData.PaintBean, formBody: FormBody) {
        OkClientHelper.patch(this, "admin/artwork/${bean.id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
                dialogPwd?.setCallBack()
            }

            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    showToast("操作成功")
                    dialogPwd?.dismiss()
                } else {
                    dialogPwd?.setCallBack()
                    showToast(result.msg)
                }
            }
        })
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
