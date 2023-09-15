package org.xiaoxingqi.shengxi.modules.echoes

import android.annotation.SuppressLint
import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.activity_response_canvas.*
import kotlinx.android.synthetic.main.item_user_canvas.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.DialogAdminPwdListener
import org.xiaoxingqi.shengxi.impl.OnArtNewMsgEvent
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.modules.listen.soulCanvas.*
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.TimeUtils
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang
import skin.support.SkinCompatManager

/**
 * 展示用户的作品  或其他用户送来的画
 */
class CanvasDetailsActivity : BaseAct() {
    private var id: String? = null
    private var type: String? = null
    private var artworkBean: PaintData.PaintBean? = null
    private lateinit var loginBean: UserInfoData
    override fun getLayoutId(): Int {
        return R.layout.activity_response_canvas
    }

    override fun initView() {

    }

    override fun initData() {
        loginBean = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        id = intent.getStringExtra("id")
        type = intent.getStringExtra("type")
        request(0)
        tv_Title.text = "画"/* when (type) {
            "2" -> "收到的画"
            "3" -> "被收藏的画"
            "4" -> "今日热门推荐"
            "5" -> "画"
            else -> resources.getString(R.string.string_for_user_response_art)
        }*/
        request(1)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            finish()
        }
        layoutCanvas.tv_Action.setOnClickListener {
            artworkBean?.let {
                startActivity(Intent(this, PaintTopicListActivity::class.java)
                        .putExtra("topicId", it.topic_id)
                        .putExtra("topicName", it.topic_name))
            }
        }
        layoutCanvas.roundImg.setOnClickListener {
            artworkBean?.let { bean ->
                startActivity<UserDetailsActivity>("id" to bean.user_id)
            }
        }
        layoutCanvas.tvCollection.setOnClickListener {//收藏
            artworkBean?.let {
                if (TextUtils.isEmpty(it.collection_id)) {
                    addCollection(it)
                } else {
                    deleteCollection(it)
                }
            }
        }
        layoutCanvas.relativeEcho.setOnClickListener {//喜欢
            artworkBean?.let {
                if (it.like_type != 0) {
                    unLike(it, layoutCanvas.iv_Thumb)
                } else
                    like(it, layoutCanvas.iv_Thumb)
            }
        }
        layoutCanvas.lineaer_Recommend.setOnClickListener { _ ->
            artworkBean?.let {
                if (it.user_id == loginBean.data.user_id) {
                    if (it.chat_num == 0) {
                        showToast("还没有涂鸦")
                    } else {
                        if (GraffitiListActivity.instances == null)
                            startActivity<GraffitiListActivity>("resourceId" to it.id)
                    }
                } else {
                    if (it.graffiti_switch != 0) {
                        if (it.dialog_num == 0) {
                            startActivity(Intent(this, CanvasLocalActivity::class.java)
                                    .putExtra("uid", it.user_id)
                                    .putExtra("artworkUrl", it.artwork_url)
                                    .putExtra("artId", it.id)
                                    .putExtra("topicName", it.topic_name))
                        } else {
//                            startActivity<TalkGraffitiDetailsActivity>("resourceId" to it.id, "uid" to it.user_id)
                            TalkGraffitiDetailsActivity.open(this, Intent(this, TalkGraffitiDetailsActivity::class.java)
                                    .putExtra("resourceId", it.id)
                                    .putExtra("uid", it.user_id)
                            )
                        }
                    } else {
                        showToast("对方设置了禁止涂鸦")
                    }
                }
            }
        }
        layoutCanvas.relativeShare.setOnClickListener {
            artworkBean?.let { bean ->
                if (bean.user_id == loginBean.data.user_id) {
                    DialogPainterMore(this).setForbidGraffiti(bean.graffiti_switch).setOnClickListener(View.OnClickListener {
                        when (it.id) {
                            R.id.tv_Delete -> {
                                DialogDeleteConment(this).setHintText(resources.getString(R.string.string_paint_delete_works)).setOnClickListener(View.OnClickListener {
                                    deleteItem(bean)
                                }).show()
                            }
                            R.id.tvBanGraffiti -> {//禁止涂鸦
                                DialogGraffiti(this).setTitle(if (bean.graffiti_switch == 1) resources.getString(R.string.string_graffiti_ban_confirm) else resources.getString(R.string.string_graffiti_change_mode), if (bean.graffiti_switch == 1) resources.getString(R.string.string_confirm) else resources.getString(R.string.string_graffiti_allow)).setOnClickListener(View.OnClickListener {
                                    operatorForbid(bean)
                                }).show()
                            }
                        }
                    }).show()
                } else {
                    DialogPainterReport(this).setReportTitle(resources.getString(R.string.string_report_normal)).setArtReport(true, bean.is_private).setOnClickListener(View.OnClickListener {
                        when (it.id) {
                            R.id.tv_admin_setPrivacy -> {
                                dialogPwd = DialogCommitPwd(this).setOperator("isHidden", "1").setOnResultListener(DialogAdminPwdListener { key, value, pwd ->
                                    adminOperator(FormBody.Builder().add(key, value)
                                            .add("confirmPasswd", pwd).build(), bean)
                                })
                                dialogPwd?.show()
                            }
                            R.id.tv_Report -> {
                                DialogNormalReport(this).setOnClickListener(View.OnClickListener { report ->
                                    when (report.id) {
                                        R.id.tv_Attach -> {
                                            reportArt(bean.id, "1")
                                        }
                                        R.id.tv_Porn -> {
                                            reportArt(bean.id, "2")
                                        }
                                        R.id.tv_Junk -> {
                                            reportArt(bean.id, "3")
                                        }
                                        R.id.tv_illegal -> {
                                            reportArt(bean.id, "4")
                                        }
                                    }
                                }).show()
                            }
                            R.id.tv_admin_delete -> {
                                dialogPwd = DialogCommitPwd(this).setOperator("deleteArt", "1").setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                    adminOperatorDelete(FormBody.Builder().add("confirmPasswd", pwd).build(), bean)
                                })
                                dialogPwd?.show()
                            }
                            R.id.tv_admin_user_details -> {
                                dialogPwd = DialogCommitPwd(this).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                    loginAdmin(pwd, bean.user_id)
                                })
                                dialogPwd?.show()
                            }
                            R.id.tv_Recommend -> {
                                if (bean.top_id != 0) {
                                    showToast("当前作品正被设为今日推荐")
                                } else
                                    checkArtTopStatus(bean)
                            }
                        }
                    }).show()
                }
            }
        }
        layoutCanvas.square_img.setOnClickListener {
            artworkBean?.let {
                if (it.user_id == loginBean.data.user_id) {
                    startActivity<CanvasShowActivity>("artworkUrl" to it.artwork_url, "isSend" to true, "resourceId" to it.id.toString())
                    overridePendingTransition(R.anim.act_enter_alpha, 0)
                } else {
                    if (it.graffiti_switch == 0) {
                        showToast("对方设置了禁止涂鸦")
                    } else {
                        startActivity(Intent(this, CanvasLocalActivity::class.java)
                                .putExtra("artworkUrl", it.artwork_url)
                                .putExtra("artId", it.id)
                                .putExtra("uid", it.user_id)
                                .putExtra("topicName", it.topic_name))
                    }
                }
            }
        }
        layoutCanvas.tv_Sub.setOnClickListener {
            artworkBean?.let {
                startActivity<SelectFriendActivity>("artworkUrl" to it.artwork_url, "resourceId" to it.id.toString())
            }
        }
    }

    private fun addCollection(bean: PaintData.PaintBean) {
        transLayout.showProgress()
        artAddCollection(bean) {
            it?.let { result ->
                if (result.code == 0) {
                    bean.collection_id = result.data.id.toString()
                    layoutCanvas.tvCollection.text = "已收藏"
                } else {
                    showToast(result.msg)
                }
            }
            transLayout.showContent()
        }
    }

    private fun deleteCollection(bean: PaintData.PaintBean) {
        transLayout.showProgress()
        artDeleteCollection(bean) {
            it?.let { result ->
                if (result.code == 0) {
                    bean.collection_id = ""
                    layoutCanvas.tvCollection.text = "收藏"
                } else {
                    showToast(result.msg)
                }
            }
            transLayout.showContent()
        }
    }


    private fun showAnonymous(bean: PaintData.PaintBean) {
        if (bean.is_private == 1) {
            Glide.with(layoutCanvas.roundImg)
                    .load(R.mipmap.icon_user_default)
                    .into(layoutCanvas.roundImg)
            layoutCanvas.tv_UserName.text = resources.getString(R.string.string_anonymous_painter)
            layoutCanvas.iv_user_type.visibility = View.GONE
        } else {
            //是否是自己
            if (bean.user_id == loginBean.data.user_id) {
                glideUtil.loadGlide(loginBean.data.avatar_url, layoutCanvas.roundImg, R.mipmap.icon_user_default, glideUtil.getLastModified(loginBean.data.avatar_url))
                layoutCanvas.tv_UserName.text = loginBean.data.nick_name
            } else {
                glideUtil.loadGlide(bean.user_info.avatar_url, layoutCanvas.roundImg, R.mipmap.icon_user_default, glideUtil.getLastModified(bean.user_info.avatar_url))
                layoutCanvas.tv_UserName.text = bean.user_info.nick_name
            }
        }
    }

    private fun loginAdmin(pwd: String, userId: String) {
        OkClientHelper.post(this, "admin/users/login", FormBody.Builder().add("confirmPasswd", pwd).build(), AdminLoginData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as AdminLoginData
                if (result.code == 0) {
                    dialogPwd?.dismiss()
                    startActivity(Intent(this@CanvasDetailsActivity, UserDetailsActivity::class.java).putExtra("id", userId))
                } else {
                    dialogPwd?.setCallBack()
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                showToast(any.toString())
            }
        })
    }


    /**
     * 超管删除
     */
    private fun adminOperatorDelete(formBody: FormBody, item: PaintData.PaintBean) {
        OkClientHelper.delete(this, "admin/artwork/${item.id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    finish()
                } else {
                    showToast(result.msg)
                    dialogPwd?.setCallBack()
                }
            }

            override fun onFailure(any: Any?) {
                dialogPwd?.setCallBack()
            }
        })
    }

    /**
     * 超管处理设置为隐私
     */
    private fun adminOperator(formBody: FormBody, item: PaintData.PaintBean) {
        OkClientHelper.patch(this, "admin/artwork/${item.id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    dialogPwd?.dismiss()
                } else {
                    showToast(result.msg)
                    dialogPwd?.setCallBack()
                }
            }

            override fun onFailure(any: Any?) {
                dialogPwd?.setCallBack()
            }
        })
    }

    /**
     * 举报艺术品
     */
    private fun reportArt(id: Int, type: String) {
        transLayout.showProgress()
        val formBody = FormBody.Builder()
                .add("reportType", type)
                .add("resourceType", "5")
                .add("resourceId", "$id")
                .build()
        OkClientHelper.post(this, "reports", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    DialogReportSuccess(this@CanvasDetailsActivity).show()
                } else
                    showToast(result.msg)
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    /**
     * 禁止涂鸦,或允许  更新个人界面的涂鸦权限
     */
    private fun operatorForbid(item: PaintData.PaintBean) {
        transLayout.showProgress()
        OkClientHelper.patch(this, "users/${item.user_id}/artwork/${item.id}", FormBody.Builder().add("graffitiSwitch", if (item.graffiti_switch == 0) "1" else "0").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    item.graffiti_switch = if (item.graffiti_switch == 0) 1 else 0
                    layoutCanvas.tv_Recommend.text = if (item.graffiti_switch == 0) "禁止涂鸦" else "涂鸦对话"
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.6")
    }

    /**
     * 删除作品
     */
    private fun deleteItem(item: PaintData.PaintBean) {
        OkClientHelper.delete(this, "users/${item.user_id}/artwork/${item.id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    finish()
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V3.6")
    }

    private fun like(bean: PaintData.PaintBean, publicView: View) {
        transLayout.showProgress()
        OkClientHelper.post(this, "users/${bean.user_id}/artworkLike/${bean.id}", FormBody.Builder().add("likeType", "2").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    bean.like_type = 2
                    SmallBang.attach2Window(this@CanvasDetailsActivity).bang(publicView, 60f, null)
                    publicView.isSelected = true
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.6")
    }

    private fun unLike(bean: PaintData.PaintBean, publicView: View) {
        transLayout.showProgress()
        OkClientHelper.delete(this, "users/${bean.user_id}/artworkLike/${bean.id}", FormBody.Builder().add("likeType", "2").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    SmallBang.attach2Window(this@CanvasDetailsActivity).bang(publicView, 60f, null)
                    publicView.isSelected = false
                    bean.like_type = 0
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.6")
    }

    override fun request(flag: Int) {
        when (flag) {
            1 -> {
                OkClientHelper.get(this, "artworks/${id}", AdminReportGraffitiData::class.java, object : OkResponse {
                    @SuppressLint("SetTextI18n")
                    override fun success(result: Any?) {
                        result as AdminReportGraffitiData
                        if (result.code == 0) {
                            result.data?.let {
                                artworkBean = it
                                Glide.with(layoutCanvas.square_img)
                                        .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(it.artwork_url))
                                                .error(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                                                .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                                        )
                                        .asBitmap()
                                        .load(it.artwork_url)
                                        .into(layoutCanvas.square_img)
                                layoutCanvas.iv_user_type.visibility = if (it.identity_type == 0) View.GONE else View.VISIBLE
                                layoutCanvas.iv_user_type.isSelected = it.identity_type == 1
                                showAnonymous(it)
                                layoutCanvas.tv_Action.text = if (TextUtils.isEmpty(it.topic_name)) "" else "#${it.topic_name}#"
                                layoutCanvas.tvTime.text = TimeUtils.getInstance().paserFriends(this@CanvasDetailsActivity, it.created_at)
                                if (loginBean.data.user_id == artworkBean!!.user_id) {
                                    layoutCanvas.tv_Sub.visibility = View.VISIBLE
                                    layoutCanvas.tvCollection.visibility = View.GONE
                                    layoutCanvas.tv_Recommend.text = "涂鸦对话" + if (result.data.chat_num > 0) " ${result.data.chat_num}" else ""
                                } else {
                                    layoutCanvas.tv_Recommend.text = if (result.data.dialog_num > 0) "涂鸦对话 ${result.data.dialog_num}" else "给ta涂鸦"
                                    layoutCanvas.tv_Sub.visibility = View.GONE
                                    layoutCanvas.tvCollection.visibility = View.VISIBLE
                                }
                                if (result.data.graffiti_switch == 0) layoutCanvas.tv_Recommend.text = "禁止涂鸦"
                                layoutCanvas.tvCollection.text = if (TextUtils.isEmpty(result.data.collection_id)) "收藏" else "已收藏"
                                layoutCanvas.iv_Thumb.isSelected = it.like_type != 0
                                layoutCanvas.tv_Echo.text = "喜欢" + if (result.data.publicly_like_num > 0) " ${result.data.publicly_like_num}" else ""
                                layoutCanvas.ivMore.isSelected = loginBean.data.user_id == result.data.user_id
                            }
                        } else {
                            transLayout.showOffline()
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.2")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun msgReceive(event: OnArtNewMsgEvent) {
        if (TextUtils.isEmpty(event.bean.action)) {
            artworkBean?.let {
                if (it.id == event.bean.data.resource_id) {
                    it.dialog_num++
                    layoutCanvas.tv_Recommend.text = "涂鸦对话 ${it.dialog_num}"
                }
            }
        }
    }
}