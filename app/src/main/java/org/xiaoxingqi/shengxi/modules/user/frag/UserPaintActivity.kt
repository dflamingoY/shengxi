package org.xiaoxingqi.shengxi.modules.user.frag

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.activity_user_home_paint.*
import kotlinx.android.synthetic.main.view_empty_art.*
import kotlinx.android.synthetic.main.view_home_paint_empty.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.DialogAdminPwdListener
import org.xiaoxingqi.shengxi.impl.ImpUpdatePaint
import org.xiaoxingqi.shengxi.impl.OnArtNewMsgEvent
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.listen.soulCanvas.*
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.TimeUtils
import org.xiaoxingqi.shengxi.wedgit.GroupToggleView
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang
import skin.support.SkinCompatManager

class UserPaintActivity : BaseAct() {
    private lateinit var userId: String
    private lateinit var adapter: QuickAdapter<PaintData.PaintBean>
    private val mData by lazy { ArrayList<PaintData.PaintBean>() }
    private var lastId: String = ""
    private lateinit var userInfoData: UserInfoData
    private var currentPage = 0//默认展示我的界面
    private var isSelf = true
    private lateinit var loadmore: View
    private var artCount = 0
    override fun getLayoutId(): Int {
        return R.layout.activity_user_home_paint
    }

    override fun initView() {
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorIndecators),
                ContextCompat.getColor(this, R.color.colorMovieTextColor),
                ContextCompat.getColor(this, R.color.color_Text_Black))
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        userInfoData = PreferenceTools.getObj(this, IConstant.USERCACHE, UserInfoData::class.java)
        userId = intent.getStringExtra("uid")
        if (userInfoData.data.user_id == userId) {//自己
            glideUtil.loadGlide(userInfoData.data.avatar_url, ivUserLogo, 0, glideUtil.getLastModified(userInfoData.data.avatar_url))
            tvUserName.text = userInfoData.data.nick_name
            ivUserType.visibility = if (userInfoData.data.identity_type != 0) View.VISIBLE else View.GONE
            ivUserType.isSelected = userInfoData.data.identity_type == 1
            headButton.visibility = View.VISIBLE
        } else {
            isSelf = false
            tv_Title.text = "ta的画"
            relativeMy.visibility = View.GONE
        }
        adapter = object : QuickAdapter<PaintData.PaintBean>(this, R.layout.item_user_canvas, mData) {
            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: PaintData.PaintBean?) {
                Glide.with(helper!!.getImageView(R.id.square_img))
                        .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(item!!.artwork_url))
                                .error(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                                .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                        )
                        .asBitmap()
                        .load(item.artwork_url)
                        .into(helper.getImageView(R.id.square_img))
                helper.getView(R.id.iv_user_type).visibility = if (item.user.identity_type == 0) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).isSelected = item.user.identity_type == 1
                helper.getTextView(R.id.tvCollection).text = if (TextUtils.isEmpty(item.collection_id)) "收藏" else "已收藏"
                helper.getTextView(R.id.tv_Action).text = if (TextUtils.isEmpty(item.topic_name)) "" else "#${item.topic_name}#"
                helper.getTextView(R.id.tvTime).text = TimeUtils.getInstance().paserFriends(this@UserPaintActivity, item.created_at)
                if (item.is_private == 1) {
                    Glide.with(helper.getImageView(R.id.roundImg))
                            .load(R.mipmap.icon_user_default)
                            .into(helper.getImageView(R.id.roundImg))
                    helper.getTextView(R.id.tv_UserName).text = resources.getString(R.string.string_anonymous_painter)+ "(" + resources.getString(R.string.string_just_self_visible_title) + ")"
                    helper.getView(R.id.iv_user_type).visibility = View.GONE
                    //匿名只能点赞看大图, 其他一切都不能操作 sb遗留问题
                    helper.getTextView(R.id.tv_Sub).visibility = View.GONE
                    helper.getView(R.id.tvCollection).visibility = View.GONE
                    helper.getView(R.id.lineaer_Recommend).isEnabled = false
                    helper.getView(R.id.lineaer_Recommend).visibility = View.INVISIBLE
                    helper.getView(R.id.square_img).isEnabled = false
                } else {
                    helper.getView(R.id.lineaer_Recommend).visibility = View.VISIBLE
                    helper.getTextView(R.id.tv_Sub).visibility = View.VISIBLE
                    helper.getView(R.id.tvCollection).visibility = View.VISIBLE
                    helper.getView(R.id.lineaer_Recommend).isEnabled = true
                    helper.getView(R.id.square_img).isEnabled = true
                    glideUtil.loadGlide(item.user.avatar_url, helper.getImageView(R.id.roundImg), R.mipmap.icon_user_default, glideUtil.getLastModified(item.user.avatar_url))
                    helper.getTextView(R.id.tv_UserName).text = item.user.nick_name
                    helper.getTextView(R.id.tv_Sub).visibility = if (userInfoData.data.user_id == item.user_id) View.VISIBLE else View.GONE
                    helper.getView(R.id.tvCollection).visibility = if (userInfoData.data.user_id == item.user_id) View.GONE else View.VISIBLE
                }
                helper.getTextView(R.id.tv_Recommend).text = if (item.graffiti_switch == 0) "禁止涂鸦" else {
                    if (item.user_id == userInfoData.data.user_id) {
                        "涂鸦对话 " + if (item.chat_num > 0) item.chat_num else ""
                    } else {
                        if (item.dialog_num > 0) "涂鸦对话 ${item.dialog_num}" else "给ta涂鸦"
                    }
                }
                helper.getView(R.id.ivMore).isSelected = item.user_id == userInfoData.data.user_id
                helper.getTextView(R.id.tv_Echo).text = "${resources.getString(R.string.string_recommend)} " + if (item.publicly_like_num > 0) " ${item.publicly_like_num}" else ""
                helper.getView(R.id.iv_Thumb).isSelected = item.like_type != 0
                helper.getView(R.id.square_img).setOnClickListener {
                    //去涂鸦
                    if (userInfoData.data.user_id == item.user_id) {
                        startActivity<CanvasShowActivity>("artworkUrl" to item.artwork_url, "isSend" to true, "resourceId" to item.id.toString())
                        overridePendingTransition(R.anim.act_enter_alpha, 0)
                    } else {
                        if (item.graffiti_switch != 0) {
                            startActivity(Intent(this@UserPaintActivity, CanvasLocalActivity::class.java)
                                    .putExtra("artworkUrl", item.artwork_url)
                                    .putExtra("artId", item.id)
                                    .putExtra("uid", item.user_id)
                                    .putExtra("topicName", item.topic_name))
                        } else {
                            startActivity<CanvasShowActivity>("artworkUrl" to item.artwork_url, "resourceId" to item.id.toString())
                            overridePendingTransition(R.anim.act_enter_alpha, 0)
                        }
                    }
                }
                helper.getView(R.id.tvCollection).setOnClickListener {
                    //创建收藏
                    if (TextUtils.isEmpty(item.collection_id)) {
                        addCollection(item)
                    } else {
                        deleteCollection(item)
                    }
                }
                helper.getTextView(R.id.tv_Action).setOnClickListener {
                    startActivity(Intent(this@UserPaintActivity, PaintTopicListActivity::class.java)
                            .putExtra("topicId", item.topic_id)
                            .putExtra("topicName", item.topic_name))
                }
                helper.getView(R.id.roundImg).setOnClickListener {
                    if (item.is_private != 1)
                        startActivity(Intent(this@UserPaintActivity, UserDetailsActivity::class.java).putExtra("id", item.user_id))
                }
                helper.getView(R.id.relativeEcho).setOnClickListener {
                    if (item.isLoading) {
                        return@setOnClickListener
                    }
                    item.isLoading = true
                    if (item.like_type == 2) {
                        unLike(item, helper.getView(R.id.iv_Thumb), helper.getTextView(R.id.tv_Echo))
                    } else
                        like(item, helper.getView(R.id.iv_Thumb), helper.getTextView(R.id.tv_Echo))
                }
                helper.getView(R.id.lineaer_Recommend).setOnClickListener {
                    //去涂鸦
                    if (item.user_id == userInfoData.data.user_id) {
                        if (item.chat_num == 0) {
                            showToast("还没有涂鸦")
                        } else {
                            startActivity<GraffitiListActivity>("resourceId" to item.id)
                        }
                    } else {
                        if (item.graffiti_switch != 0) {
                            if (item.dialog_num == 0) {
                                startActivity(Intent(this@UserPaintActivity, CanvasLocalActivity::class.java)
                                        .putExtra("uid", item.user_id)
                                        .putExtra("artworkUrl", item.artwork_url)
                                        .putExtra("artId", item.id)
                                        .putExtra("topicName", item.topic_name))
                            } else {
                                TalkGraffitiDetailsActivity.open(this@UserPaintActivity, Intent(this@UserPaintActivity, TalkGraffitiDetailsActivity::class.java)
                                        .putExtra("resourceId", item.id)
                                        .putExtra("uid", item.user_id)
                                )
                            }
                        } else {
                            showToast("对方设置了禁止涂鸦")
                        }
                    }
                }
                helper.getView(R.id.relativeShare).setOnClickListener {
                    if (item.user_id == userInfoData.data.user_id) {
                        DialogPainterMore(this@UserPaintActivity).setAnomouys(item.is_private).setForbidGraffiti(item.graffiti_switch).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_Delete -> {
                                    DialogDeleteConment(this@UserPaintActivity).setHintText(resources.getString(R.string.string_paint_delete_works)).setOnClickListener(View.OnClickListener {
                                        deleteItem(item)
                                    }).show()
                                }
                                R.id.tvBanGraffiti -> {//禁止涂鸦
                                    DialogGraffiti(this@UserPaintActivity).setTitle(if (item.graffiti_switch == 1) resources.getString(R.string.string_graffiti_ban_confirm) else resources.getString(R.string.string_graffiti_change_mode), if (item.graffiti_switch == 1) resources.getString(R.string.string_confirm) else resources.getString(R.string.string_graffiti_allow)).setOnClickListener(View.OnClickListener {
                                        operatorForbid(item)
                                    }).show()
                                }
                            }
                        }).show()
                    } else {
                        DialogPainterReport(this@UserPaintActivity).setReportTitle(resources.getString(R.string.string_report_normal)).setArtReport(true, item.is_private).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_admin_setPrivacy -> {
                                    dialogPwd = DialogCommitPwd(this@UserPaintActivity).setOperator("isHidden", "1").setOnResultListener(DialogAdminPwdListener { key, value, pwd ->
                                        adminOperator(FormBody.Builder().add(key, value)
                                                .add("confirmPasswd", pwd).build(), item)
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_Report -> {
                                    DialogNormalReport(this@UserPaintActivity).setOnClickListener(View.OnClickListener { report ->
                                        when (report.id) {
                                            R.id.tv_Attach -> {
                                                reportArt(item.id, "1")
                                            }
                                            R.id.tv_Porn -> {
                                                reportArt(item.id, "2")
                                            }
                                            R.id.tv_Junk -> {
                                                reportArt(item.id, "3")
                                            }
                                            R.id.tv_illegal -> {
                                                reportArt(item.id, "4")
                                            }
                                        }
                                    }).show()
                                }
                                R.id.tv_admin_delete -> {
                                    dialogPwd = DialogCommitPwd(this@UserPaintActivity).setOperator("deleteArt", "1").setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                        adminOperatorDelete(FormBody.Builder().add("confirmPasswd", pwd).build(), item)
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_admin_user_details -> {
                                    dialogPwd = DialogCommitPwd(this@UserPaintActivity).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                        loginAdmin(pwd, item.user_id)
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_Recommend -> {
                                    if (item.top_id != 0) {
                                        showToast("当前作品正被设为今日推荐")
                                    } else
                                        checkArtTopStatus(item)
                                }
                            }
                        }).show()
                    }
                }
                helper.getView(R.id.tv_Sub).setOnClickListener {
                    startActivity<SelectFriendActivity>("artworkUrl" to item.artwork_url, "resourceId" to item.id.toString())
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        loadmore = LayoutInflater.from(this).inflate(R.layout.view_loadmore_self_art, recyclerView, false)
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, loadmore)
        request(0)
        queryArtCount(0)
    }

    override fun initEvent() {
        loadmore.findViewById<View>(R.id.relative_Empty).setOnClickListener {
            startActivity<CanvasLocalActivity>()
        }
        btn_Back.setOnClickListener { finish() }
        headButton.setOnChildClickListener(object : GroupToggleView.OnChildClickListener {
            override fun onClick(position: Int, childView: View) {
                transLayout.showProgress()
                currentPage = position
                lastId = ""
                mData.clear()
                adapter.notifyDataSetChanged()
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (currentPage == 0) {
                    layoutEmptyArt.visibility = View.VISIBLE
                } else {
                    layoutEmptyArt.visibility = View.GONE
                }
                request(currentPage)
                queryArtCount(currentPage)
            }
        })
        swipeRefresh.setOnRefreshListener {
            lastId = ""
            request(currentPage)
        }
        adapter.setOnLoadListener {
            request(currentPage)
        }
        tvEmptyHint.setOnClickListener {
            if (isSelf) {
                startActivity<CanvasLocalActivity>()
            }
        }
    }

    private fun addCollection(bean: PaintData.PaintBean) {
        transLayout.showProgress()
        artAddCollection(bean) {
            it?.let { result ->
                if (result.code == 0) {
                    bean.collection_id = result.data.id.toString()
                    adapter.notifyItemChanged(mData.indexOf(bean))
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
                    mData.remove(bean)
                    adapter.notifyDataSetChanged()
                    if (mData.size == 0) {
                        transLayout.showEmpty()
                    }
                    artCount--
                    tvArtCount.text = if (artCount > 0) "共${artCount}幅" else resources.getString(R.string.string_40)
                } else {
                    showToast(result.msg)
                }
            }
            transLayout.showContent()
        }
    }

    private fun loginAdmin(pwd: String, userId: String) {
        OkClientHelper.post(this, "admin/users/login", FormBody.Builder().add("confirmPasswd", pwd).build(), AdminLoginData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as AdminLoginData
                if (result.code == 0) {
                    dialogPwd?.dismiss()
                    startActivity(Intent(this@UserPaintActivity, UserDetailsActivity::class.java).putExtra("id", userId))
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
     * 删除作品
     */
    private fun deleteItem(item: PaintData.PaintBean) {
        OkClientHelper.delete(this, "users/${item.user_id}/artwork/${item.id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    val indexOf = mData.indexOf(item)
                    EventBus.getDefault().post(ImpUpdatePaint(1, item.id))
                    mData.remove(item)
                    adapter.notifyItemRemoved(indexOf)
                    if (mData.size == 0) {
                        transLayout.showEmpty()
                    }
                    artCount--
                    tvArtCount.text = if (artCount > 0) "共${artCount}幅" else resources.getString(R.string.string_32)
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V3.6")
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
                    adapter.notifyItemChanged(mData.indexOf(item))
                    EventBus.getDefault().post(ImpUpdatePaint(if (item.graffiti_switch == 1) 6 else 5, item.id))
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
     * 超管删除
     */
    private fun adminOperatorDelete(formBody: FormBody, item: PaintData.PaintBean) {
        OkClientHelper.delete(this, "admin/artwork/${item.id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    val indexOf = mData.indexOf(item)
                    mData.remove(item)
                    adapter.notifyItemRemoved(indexOf)
                    dialogPwd?.dismiss()
                    EventBus.getDefault().post(ImpUpdatePaint(1, item.id))
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
                    val indexOf = mData.indexOf(item)
                    mData.remove(item)
                    adapter.notifyItemRemoved(indexOf)
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

    private fun reportArt(id: Int, type: String) {
        val formBody = FormBody.Builder()
                .add("reportType", type)
                .add("resourceType", "5")
                .add("resourceId", "$id")
                .build()
        OkClientHelper.post(this, "reports", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    DialogReportSuccess(this@UserPaintActivity).show()
                } else
                    showToast(result.msg)
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    private fun like(bean: PaintData.PaintBean, publicView: View, tv: TextView) {
        OkClientHelper.post(this, "users/${bean.user_id}/artworkLike/${bean.id}", FormBody.Builder().add("likeType", "2").build(), BaseRepData::class.java, object : OkResponse {
            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    bean.like_type = 2
                    SmallBang.attach2Window(this@UserPaintActivity).bang(publicView, 60f, null)
                    bean.publicly_like_num++
                    tv.text = resources.getString(R.string.string_recommend) + " ${bean.publicly_like_num}"
                    publicView.isSelected = true
                } else {
                    showToast(result.msg)
                }
                bean.isLoading = false
            }

            override fun onFailure(any: Any?) {
                bean.isLoading = false
            }
        }, "V3.6")
    }

    private fun unLike(bean: PaintData.PaintBean, publicView: View, tv: TextView) {
        OkClientHelper.delete(this, "users/${bean.user_id}/artworkLike/${bean.id}", FormBody.Builder().add("likeType", "2").build(), BaseRepData::class.java, object : OkResponse {
            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    SmallBang.attach2Window(this@UserPaintActivity).bang(publicView, 60f, null)
                    bean.publicly_like_num--
                    tv.text = resources.getString(R.string.string_recommend) + if (bean.publicly_like_num > 0) " ${bean.publicly_like_num}" else ""
                    publicView.isSelected = false
                    bean.like_type = 0
                } else {
                    showToast(result.msg)
                }
                bean.isLoading = false
            }

            override fun onFailure(any: Any?) {
                bean.isLoading = false
            }
        }, "V3.6")
    }

    private fun queryArtCount(type: Int) {
        when (type) {
            0 -> {
                OkClientHelper.get(this, "users/${userId}/artworksStatistics", IntegerRespData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {

                    }

                    @SuppressLint("SetTextI18n")
                    override fun success(result: Any?) {
                        result as IntegerRespData
                        if (result.code == 0) {
                            artCount = result.data.total
                            tvArtCount.text = if (result.data.total > 0) "共${result.data.total}幅" else resources.getString(R.string.string_32)
                        }
                    }
                }, "V4.3")
            }
            1 -> {
                OkClientHelper.get(this, "users/${userId}/artworksCollectionStatistics", IntegerRespData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {

                    }

                    override fun success(result: Any?) {
                        result as IntegerRespData
                        if (result.code == 0) {
                            artCount = result.data.total
                            tvArtCount.text = if (result.data.total > 0) "共${result.data.total}幅" else resources.getString(R.string.string_40)
                        }
                    }
                }, "V4.3")
            }
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "users/${userId}/artworks?lastId=$lastId", PaintData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {
                        swipeRefresh.isRefreshing = false
                    }

                    override fun success(result: Any?) {
                        transLayout.showContent()
                        swipeRefresh.isRefreshing = false
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        result as PaintData
                        if (result.data != null && result.data.size > 0) {
                            if (TextUtils.isEmpty(lastId)) {
                                mData.clear()
                                result.data.forEach {
                                    mData.add(it.checkUserInfo(userInfoData))
                                }
                                adapter.notifyDataSetChanged()
                                recyclerView.scrollToPosition(0)
                            } else {
                                for (bean in result.data) {
                                    mData.add(bean.checkUserInfo(userInfoData))
                                    adapter.notifyItemInserted(adapter.itemCount - 1)
                                }
                            }
                            if (result.data.size >= 10) {
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                            }
                            lastId = mData[mData.size - 1].id.toString()
                        } else {
                            if (TextUtils.isEmpty(lastId)) {
                                mData.clear()
                                adapter.notifyDataSetChanged()
                            }
                        }
                        if (result.data == null || result.data.size < 10) {
                            if (isSelf)
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                        }
                        if (mData.size == 0) {
                            transLayout.showEmpty()
                        }
                    }
                }, "V4.3")
            }
            1 -> {
                OkClientHelper.get(this, "users/${userInfoData.data.user_id}/artworkCollection?lastId=$lastId", ArtHotData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {
                        swipeRefresh.isRefreshing = false
                    }

                    override fun success(result: Any?) {
                        transLayout.showContent()
                        swipeRefresh.isRefreshing = false
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        result as ArtHotData
                        if (result.data != null && result.data.size > 0) {
                            if (TextUtils.isEmpty(lastId)) {
                                mData.clear()
                                mData.addAll(result.data.map {
                                    it.artwork.created_at = it.created_at
                                    it.artwork.user = BaseUserBean().apply {
                                        avatar_url = it.artwork.avatar_url
                                        nick_name = it.artwork.nick_name
                                        identity_type = it.artwork.identity_type
                                    }
                                    it.artwork
                                })
                                adapter.notifyDataSetChanged()
                                recyclerView.scrollToPosition(0)
                            } else {
                                for (bean in result.data) {
                                    bean.artwork.created_at = bean.created_at
                                    bean.artwork.user = BaseUserBean().apply {
                                        avatar_url = bean.artwork.avatar_url
                                        nick_name = bean.artwork.nick_name
                                        identity_type = bean.artwork.identity_type
                                    }
                                    mData.add(bean.artwork)
                                    adapter.notifyItemInserted(adapter.itemCount - 1)
                                }
                            }
                            if (result.data.size >= 10) {
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                            }
                            lastId = mData[mData.size - 1].collection_id
                        } else {
                            if (TextUtils.isEmpty(lastId)) {
                                mData.clear()
                                adapter.notifyDataSetChanged()
                            }
                        }
                        if (mData.size == 0) {
                            transLayout.showEmpty()
                        }
                    }
                }, "V4.3")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun msgReceive(event: OnArtNewMsgEvent) {
        if (TextUtils.isEmpty(event.bean.action)) {
            mData.loop {
                it.id == event.bean.data.resource_id
            }?.let {
                it.dialog_num++
                adapter.notifyItemChanged(mData.indexOf(it))
            }
        } else {//撤回消息
            mData.loop {
                it.id == event.bean.data.resource_id
            }?.let {
                it.dialog_num--
                adapter.notifyItemChanged(mData.indexOf(it))
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateCanvasEvent(event: ImpUpdatePaint) {
        if (event.type == 4 && isSelf) {
            if (currentPage == 0) {
                lastId = ""
                request(0)
            }
        }
    }

}