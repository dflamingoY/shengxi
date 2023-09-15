package org.xiaoxingqi.shengxi.core

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.http.GlideJudeUtils
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.DialogAdminPwdListener
import org.xiaoxingqi.shengxi.impl.IDeleteHomeStatus
import org.xiaoxingqi.shengxi.impl.ItemOperator
import org.xiaoxingqi.shengxi.impl.StopPlayInterFace
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.alarm.BaseAlarmBean
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.home.REQUEST_RECORD
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordVoiceActivity
import org.xiaoxingqi.shengxi.modules.showAchieve
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils
import org.xiaoxingqi.shengxi.utils.TimeUtils
import org.xiaoxingqi.shengxi.wedgit.TransLayout
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang

abstract class BaseFragment : Fragment() {
    var mView: View? = null
    protected var commentBean: BaseBean? = null
    protected var dialogPwd: DialogCommitPwd? = null

    protected lateinit var glideUtil: GlideJudeUtils
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (mView != null) {
            val parent = mView?.parent?.let { it as ViewGroup }
            parent?.removeAllViews()
        } else {
            mView = inflater.inflate(getLayoutId(), null)
            glideUtil = GlideJudeUtils(activity)
            initView(mView)
            initData()
            initEvent()
        }
        return mView
    }

    abstract fun getLayoutId(): Int
    abstract fun initView(view: View?)
    abstract fun initData()
    abstract fun initEvent()
    open fun request(flag: Int) {

    }

    protected fun sendObserver() {
        EventBus.getDefault().post(object : StopPlayInterFace {})
    }


    fun showToast(text: String) {
        try {
            if (!TextUtils.isEmpty(text))
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
        }
    }

    fun showEmptyCount(count: String) {
        val toast = Toast(activity)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.duration = Toast.LENGTH_SHORT
        val view = LayoutInflater.from(activity).inflate(R.layout.toast_no_share_count, null, false)
        view.findViewById<TextView>(R.id.tv_Share_Count_hint).text = resources.getString(R.string.string_50) + count
        toast.view = view
        toast.show()
    }

    /**
     * 添加播放次数
     */
    fun addPlays(bean: BaseBean, textView: TextView?, function: (BaseBean) -> Unit) {
        OkClientHelper.get(activity, "users/${bean.user_id}/voices/${bean.voice_id}", BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    bean.played_num++
                    textView?.text = "${resources.getString(R.string.string_Listener)} ${bean.played_num}"
                    function(bean)
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    /**
     * 查询共享次数
     */
    fun queryCount(bean: BaseBean, view: TextView, ivView: View) {
        OkClientHelper.get(activity, "voices/share/statistics", RecommendCountData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as RecommendCountData
                if (result.code == 0) {
                    if (bean.is_shared == "1") {//取消共享
                        DialogUnShareWorld(activity!!).setCountText(result.data.share_num.toString()).setOnClickListener(View.OnClickListener {
                            unShare(bean, view, ivView)
                        }).show()
                    } else {
                        if (result.data.can_share == 1 && result.data.left_times > 0) {
                            DialogShareWorld(activity!!).setCountText(result.data.tips).setOnClickListener(View.OnClickListener {
                                share(bean, view, ivView)
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
        }, "V4.5")
    }

    /**
     * 取消共享到世界
     */
    fun unShare(bean: BaseBean, view: TextView, ivView: View) {
        OkClientHelper.delete(activity, "users/${bean.user_id}/voices/${bean.voice_id}/share", FormBody.Builder()
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    SmallBang.attach2Window(activity).bang(ivView, 60f, null)
                    bean.is_shared = "0"
                    ivView.isSelected = false
                    view.text = resources.getString(R.string.string_share_world)
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
    fun share(bean: BaseBean, view: TextView, ivView: View) {
        OkClientHelper.post(activity, "voices/share", FormBody.Builder().add("voiceId", bean.voice_id)
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    SmallBang.attach2Window(activity).bang(ivView, 60f, null)
                    bean.is_shared = "1"
                    ivView.isSelected = true
                    view.text = resources.getString(R.string.string_unshare_world)
                    showToast(resources.getString(R.string.string_shared_world))
                    operator?.onRecommend(bean)
                } else {
                    operator?.onFailure(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                operator?.onFailure(any)
            }
        }, "V4.5")
    }

    /**
     * 取消共鸣
     */
    fun unThumb(bean: BaseBean, view: View) {
        view.isSelected = false
        OkClientHelper.delete(activity, "users/${bean.user_id}/voices/${bean.voice_id}/collection", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
//                SmallBang.attach2Window(activity).bang(view, 60f, null)
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

    private var loginBean: LoginData.LoginBean? = null

    /**
     * 共鸣
     */
    fun tuhmb(bean: BaseBean, view: View?, needDelay: Int = 0) {
        if (loginBean == null) {
            loginBean = PreferenceTools.getObj(context, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        }
        if ("1" == loginBean?.user_id) {
            return
        }
        OkClientHelper.post(activity, "users/${bean.user_id}/voices/${bean.voice_id}/collection", FormBody.Builder()
                .apply {
                    if (needDelay != 0)
                        this.add("needDelay", "$needDelay")
                }
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                view?.let {
                    SmallBang.attach2Window(activity).bang(it, 60f, null)
                }
                if ((result as BaseRepData).code == 0) {
                    bean.is_collected = 1
                    view?.isSelected = true
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
     * 举报
     */
    fun report(id: String, type: String, toUserId: String) {
        val formBody = FormBody.Builder()
                .add("reasonType", type)
                .add("resourceType", "1")
                .add("resourceId", id)
                .add("toUserId", toUserId)
                .build()
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(activity, "users/${loginBean.user_id}/shield", formBody, BaseRepData::class.java, object : OkResponse {
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
     * 删除
     */
    fun delete(bean: BaseBean) {
        OkClientHelper.delete(activity, "users/${bean.user_id}/voices/${bean.voice_id}", FormBody.Builder()
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    queryVoicesStatus(bean.created_at)
                    operator?.onDelete(bean)
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
     * 回声
     */
    fun comment(formBody: FormBody) {
        OkClientHelper.post(activity, "chats/${commentBean?.user_id}/${commentBean?.voice_id}", formBody, CommentCallBackData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as CommentCallBackData).code == 0) {
                    showToast(resources.getString(R.string.string_echo_Success))
                    operator?.onComment(result.data.chat_id)
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
     * 查询是否能够回声
     */
    fun queryPermission(bean: BaseBean, view: TransLayout, sendPath: String? = null) {
        view.showProgress()
        OkClientHelper.get(activity, "chats/check/${bean.user_id}/${bean.voice_id}", ChechOutReplyData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as ChechOutReplyData).code == 0) {
                    commentBean = bean
                    startActivityForResult(Intent(activity, RecordVoiceActivity::class.java)
                            .putExtra("avatar", bean.user.avatar_url)
                            .putExtra("sendPath", sendPath ?: "")
                            .putExtra("isBusy", result.data.auto_reply)
                            .putExtra("hobby", if (result.data.chat_hobby != null) result.data.chat_hobby.name else "")
                            , REQUEST_RECORD)
                    activity?.overridePendingTransition(0, 0)
                } else {
                    if (SPUtils.getLong(activity, IConstant.USERLIMITTIME, 0L) > System.currentTimeMillis() / 1000) {
                        DialogLimitTalk(activity!!).show()
                    } else
                        showToast(result.msg)
                }
                view.showContent()
            }

            override fun onFailure(any: Any?) {
                view.showContent()
            }
        })
    }

    /**
     * 设置私密操作
     */
    fun setVoicePrivacy(bean: BaseBean) {
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(activity, "users/${loginBean.user_id}/voices/${bean.voice_id}", FormBody.Builder().add("privateStatus", if (bean.is_private == 1) "0" else "1").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    bean.chat_num = 0
                    bean.is_private = if (bean.is_private == 1) 0 else 1
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
        OkClientHelper.patch(activity, "admin/voices/${voiceBean.voice_id}", formBody, BaseRepData::class.java, object : OkResponse {
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
     * 加入白名单
     */
    fun addWhiteBlack(userId: String) {
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(activity, "users/${loginBean.user_id}/whitelist", FormBody.Builder().add("toUserId", userId).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    showToast("已将对方加入白名单")
                    operator?.onFailure("")
                } else {
                    operator?.onFailure(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                operator?.onFailure("")
            }
        }, "V3.6")
    }

    fun reportNormalItem(userId: String, type: String, resourceType: Int = 1) {
        val formBody = FormBody.Builder()
                .add("resourceId", userId)
                .add("reportType", type)
                .add("resourceType", "$resourceType")
                .build()
        OkClientHelper.post(activity, "reports", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0)
                    activity?.let { DialogReportSuccess(it).show() }
                else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    fun addSubscriber(bean: BaseBean) {
        OkClientHelper.post(activity, "userSubscription", FormBody.Builder().add("toUserId", "${bean.user_id}").add("voiceId", bean.voice_id).build(), IntegerRespData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as IntegerRespData
                if (result.code == 0) {
                    bean.subscription_id = result.data.id
                    operator?.onRecommend(bean)//三个广场页替用推荐回调
                } else {
                    operator?.onFailure(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                operator?.onFailure(any)
            }
        }, "V4.1")
    }

    fun friends(bean: BaseBean, view: View) {
        val infoData = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val formBody = FormBody.Builder()
                .add("toUserId", bean.user_id.toString())
                .build()
        OkClientHelper.post(activity, "users/${infoData.user_id}/friendslog", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    operator?.onFriend()
                    bean.friend_status = 1
                    view.isSelected = false
                    if (SPUtils.getInt(activity, IConstant.TOTALLENGTH + infoData.user_id, 0) == 0) {
                        activity?.let { DialogCreateVoice(it).show() }
                    } else if (SPUtils.getBoolean(activity, IConstant.STRANGEVIEW + infoData.user_id, false)) {
                        activity?.let {
                            DialogUserSet(it).setOnClickListener(View.OnClickListener {
                                addWhiteBlack(bean.user_id.toString())
                            }).show()
                        }
                    }
                } else {
                    if (SPUtils.getLong(activity, IConstant.USERLIMITTIME, 0L) > System.currentTimeMillis() / 1000) {
                        DialogLimitTalk(activity!!).show()
                        operator?.onFailure("")
                    } else {
                        operator?.onFailure(result.msg)
                    }
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
        OkClientHelper.delete(activity, "userSubscription/${bean.subscription_id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    bean.subscription_id = 0
                    operator?.onUnRecommend(bean)//三个广场页替用推荐回调
                } else {
                    operator?.onFailure(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                operator?.onFailure(any)
            }
        }, "V4.1")
    }

    fun pushTravel(userId: String) {
        OkClientHelper.post(activity, "achievement", FormBody.Builder().add("achievementType", "2").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    SPUtils.setString(activity, IConstant.FIRST_PUSH_ACHIEVEMENT + userId, TimeUtils.parseCalender((System.currentTimeMillis() / 1000).toInt()))
                    activity!!.showAchieve(type = 1)
                } else
                    showToast(result.msg)
            }

            override fun onFailure(any: Any?) {

            }
        }, "V4.3")
    }

    /**
     * 查询当天是否有心情
     */
    fun queryVoicesStatus(createAt: Int = 0, isPush: Boolean = false) {
        val date = TimeUtils.parseCalender((System.currentTimeMillis() / 1000).toInt())
        if (!isPush && TimeUtils.parseCalender(createAt) != date) {//删除非当天的记录不做查询
            return
        }
        val obj = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(activity, "users/${obj.user_id}/voices/calendar/$date", VoiceData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as VoiceData
                if (result.data != null) {//有数据
                    if (isPush)
                        if (result.data.size == 1) {
                            activity!!.showAchieve(type = 0)
                        }
                } else {
                    SPUtils.setString(activity, IConstant.FIRST_PUSH_VOICES + obj.user_id, "")
                    EventBus.getDefault().post(IDeleteHomeStatus())
                }
            }

            override fun onFailure(any: Any?) {
            }
        }, "V4.2")
    }

    fun adminPick(item: BaseAlarmBean, formBody: FormBody) {
        OkClientHelper.patch(activity, "admin/dubbings/${item.id}", formBody, BaseRepData::class.java, object : OkResponse {
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
        OkClientHelper.get(activity, "dubbings/${item.id}/pickStatus", IntegerRespData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
            }

            override fun success(result: Any?) {
                result as IntegerRespData
                if (result.code == 0) {
                    when (result.data.pick_status) {
                        0 -> {//未设置过
                            dialogPwd = DialogCommitPwd(activity!!).setOperator("pick", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                adminPick(item, FormBody.Builder().add("confirmPasswd", pwd).add("pickedAt", "${System.currentTimeMillis() / 1000}").build())
                            })
                            dialogPwd?.show()
                        }
                        1 -> {//正在
                            showToast("当前配音正被设为声昔君pick")
                        }
                        2 -> {//设置过
                            DialogDeleteWording(activity!!).setOtherTitle(resources.getString(R.string.string_alarm_setting_pick_1), resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                                dialogPwd = DialogCommitPwd(activity!!).setOperator("pick", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
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
        OkClientHelper.get(activity, "artworks/${bean.id}/pick", IntegerRespData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                result as IntegerRespData
                if (result.code == 0) {
                    if (result.data == null) {
                        dialogPwd = DialogCommitPwd(activity!!).setOperator("topAt", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                            addArtTop(bean, FormBody.Builder().add("confirmPasswd", pwd).add("topAt", "${System.currentTimeMillis() / 1000}").build())
                        })
                        dialogPwd?.show()
                    } else
                        when (result.data.top_status) {
                            0 -> {//未设置过
                                dialogPwd = DialogCommitPwd(activity!!).setOperator("topAt", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                    addArtTop(bean, FormBody.Builder().add("confirmPasswd", pwd).add("topAt", "${System.currentTimeMillis() / 1000}").build())
                                })
                                dialogPwd?.show()
                            }
                            1 -> {//正在
                                showToast("当前作品正被设为今日推荐")
                            }
                            2 -> {//设置过
                                DialogDeleteWording(activity!!).setOtherTitle("该作品曾被推荐过,确定再次推荐", resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                                    dialogPwd = DialogCommitPwd(activity!!).setOperator("topAt", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                        addArtTop(bean, FormBody.Builder().add("confirmPasswd", pwd).add("topAt", "${System.currentTimeMillis() / 1000}").build())
                                    })
                                    dialogPwd?.show()
                                }).show()
                            }
                            3 -> {
                                DialogDeleteWording(activity!!).setOtherTitle("该作品曾被推荐过,确定再次推荐", resources.getString(R.string.string_confirm)).setOnClickListener(View.OnClickListener {
                                    dialogPwd = DialogCommitPwd(activity!!).setOperator("topAt", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                        addArtTop(bean, FormBody.Builder().add("confirmPasswd", pwd).add("topAt", "${System.currentTimeMillis() / 1000}").build())
                                    })
                                    dialogPwd?.show()
                                }).show()
                            }
                        }
                } else {
                    showToast(result.msg)
                }
            }
        }, "V4.3")
    }

    fun addArtTop(bean: PaintData.PaintBean, formBody: FormBody) {
        OkClientHelper.patch(activity, "admin/artwork/${bean.id}", formBody, BaseRepData::class.java, object : OkResponse {
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
    fun setOperator(operator: ItemOperator) {
        this.operator = operator
    }

    /***
     * 当解除与activity关联时
     * 解决fragment嵌套fragment出现的问题：no activity
     */
    override fun onDetach() {
        super.onDetach()
        try {
            //参数是固定写法
            val childFragmentManager = Fragment::class.java.getDeclaredField("mChildFragmentManager")
            childFragmentManager.isAccessible = true
            childFragmentManager.set(this, null)
        } catch (e: NoSuchFieldException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }
    }

    override fun onDestroy() {
        try {
            glideUtil.cancelAll()
        } catch (e: Exception) {
        }
        super.onDestroy()
    }

}