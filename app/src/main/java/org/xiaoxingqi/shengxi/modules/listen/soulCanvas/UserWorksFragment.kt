package org.xiaoxingqi.shengxi.modules.listen.soulCanvas

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.frag_local_painter_list.view.*
import kotlinx.android.synthetic.main.view_empty_art.view.*
import kotlinx.android.synthetic.main.view_loadmore_self_art.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.*
import org.xiaoxingqi.shengxi.impl.DialogAdminPwdListener
import org.xiaoxingqi.shengxi.impl.ITabClickCall
import org.xiaoxingqi.shengxi.impl.ImpUpdatePaint
import org.xiaoxingqi.shengxi.impl.OnArtNewMsgEvent
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.TimeUtils
import org.xiaoxingqi.shengxi.wedgit.GroupToggleView
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang
import skin.support.SkinCompatManager

/**
 * 自己的艺术作品列表
 */
class UserWorksFragment : BaseFragment(), ITabClickCall {
    override fun tabClick(isVisible: Boolean) {
        try {
            if (mView!!.swipeRefresh.isEnabled != isVisible) {
                mView!!.swipeRefresh.isEnabled = isVisible
                mView!!.swipeRefresh.isRefreshing = false
            }
        } catch (e: Exception) {
        }
    }

    override fun doubleClickRefresh() {
        isCurrentPage = !isCurrentPage
    }

    private lateinit var adapter: QuickAdapter<PaintData.PaintBean>
    private val mData by lazy { ArrayList<PaintData.PaintBean>() }
    private var lastId: String = ""
    private var isCurrentPage = false
    private lateinit var userInfoData: UserInfoData
    private var currentPage = 0//默认展示我的界面
    private lateinit var loadMore: View
    private var artCount = 0
    override fun getLayoutId(): Int {
        return R.layout.frag_local_painter_list
    }

    override fun initView(view: View?) {
        view!!.relativeTop.visibility = View.VISIBLE
    }

    override fun initData() {
        userInfoData = PreferenceTools.getObj(activity, IConstant.USERCACHE, UserInfoData::class.java)
        glideUtil.loadGlide(userInfoData.data.avatar_url, mView!!.layoutEmptyArt.ivUserLogo, 0, glideUtil.getLastModified(userInfoData.data.avatar_url))
        mView!!.layoutEmptyArt.tvUserName.text = userInfoData.data.nick_name
        mView!!.layoutEmptyArt.ivUserType.visibility = if (userInfoData.data.identity_type == 0) View.GONE else View.VISIBLE
        mView!!.layoutEmptyArt.ivUserType.isSelected = userInfoData.data.identity_type == 1
        EventBus.getDefault().register(this)
        adapter = object : QuickAdapter<PaintData.PaintBean>(activity, R.layout.item_user_canvas, mData) {
            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: PaintData.PaintBean?) {
                Glide.with(helper!!.getImageView(R.id.square_img))
                        .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(item!!.artwork_url))
                                .error(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                                .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                        )
                        .load(item.artwork_url)
                        .into(helper.getImageView(R.id.square_img))
                helper.getView(R.id.iv_user_type).visibility = if (item.identity_type == 0) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_user_type).isSelected = item.identity_type == 1
                helper.getTextView(R.id.tvCollection).text = if (TextUtils.isEmpty(item.collection_id)) "收藏" else "已收藏"
                if (item.is_private == 1) {
                    helper.getView(R.id.iv_user_type).visibility = View.GONE
                    Glide.with(helper.getImageView(R.id.roundImg))
                            .load(R.mipmap.icon_user_default)
                            .into(helper.getImageView(R.id.roundImg))
                    helper.getTextView(R.id.tv_UserName).text = resources.getString(R.string.string_anonymous_painter) + "(" + resources.getString(R.string.string_just_self_visible_title) + ")"
                    //匿名只能点赞看大图, 其他一切都不能操作 遗留问题
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
                    glideUtil.loadGlide(item.avatar_url, helper.getImageView(R.id.roundImg), R.mipmap.icon_user_default, glideUtil.getLastModified(item.avatar_url))
                    helper.getTextView(R.id.tv_UserName).text = item.nick_name
                    if (item.isSelf == 1) {
                        helper.getView(R.id.tv_Sub).visibility = View.VISIBLE
                        helper.getView(R.id.tvCollection).visibility = View.GONE
                    } else {
                        helper.getView(R.id.tv_Sub).visibility = View.GONE
                        helper.getView(R.id.tvCollection).visibility = View.VISIBLE
                    }
                }
                helper.getTextView(R.id.tv_Action).text = if (TextUtils.isEmpty(item.topic_name)) "" else "#${item.topic_name}#"
                helper.getTextView(R.id.tvTime).text = TimeUtils.getInstance().paserFriends(activity, item.created_at)
                helper.getTextView(R.id.tv_Echo).text = resources.getString(R.string.string_recommend) + if (item.publicly_like_num == 0) "" else " ${item.publicly_like_num}"
                helper.getView(R.id.iv_Thumb).isSelected = item.like_type != 0
                helper.getView(R.id.ivOfficial).visibility = if (item.user_id == "1") View.VISIBLE else View.GONE
                if (item.graffiti_switch == 0) {//禁止涂鸦
                    helper.getTextView(R.id.tv_Recommend).text = "禁止涂鸦"
                } else {
                    helper.getTextView(R.id.tv_Recommend).text = if (1 == item.isSelf) {//自己
//                        helper.getView(R.id.tv_Sub).visibility = View.VISIBLE
                        helper.getView(R.id.tvCollection).visibility = View.GONE
                        "涂鸦对话" + if (item.chat_num > 0) " ${item.chat_num}" else ""
                    } else {
                        helper.getView(R.id.tv_Sub).visibility = View.GONE
                        helper.getView(R.id.tvCollection).visibility = View.VISIBLE
                        if (item.dialog_num > 0) "涂鸦对话 ${item.dialog_num}" else "给ta涂鸦"
                    }
                }
                helper.getView(R.id.ivMore).isSelected = item.isSelf == 1
                helper.getView(R.id.square_img).setOnClickListener {
                    if (item.isSelf == 1) {
                        startActivity<CanvasShowActivity>("artworkUrl" to item.artwork_url, "isSend" to (item.isSelf == 1),
                                "resourceId" to item.id.toString())
                        activity?.overridePendingTransition(R.anim.act_enter_alpha, 0)
                    } else {
                        if (item.graffiti_switch == 0) {
                            startActivity<CanvasShowActivity>("artworkUrl" to item.artwork_url, "isSend" to false,
                                    "resourceId" to item.id.toString())
                            activity?.overridePendingTransition(R.anim.act_enter_alpha, 0)
                        } else {
                            startActivity<CanvasLocalActivity>("uid" to item.user_id
                                    , "artworkUrl" to item.artwork_url
                                    , "artId" to item.id
                                    , "topicName" to item.topic_name)
                        }
                    }
                }
                helper.getTextView(R.id.tv_Action).setOnClickListener {
                    startActivity(Intent(activity, PaintTopicListActivity::class.java)
                            .putExtra("topicId", item.topic_id)
                            .putExtra("topicName", item.topic_name))
                }
                helper.getView(R.id.roundImg).setOnClickListener {
                    if (item.is_private != 1)
                        startActivity(Intent(activity, UserDetailsActivity::class.java).putExtra("id", item.user_id))
                }
                helper.getView(R.id.relativeShare).setOnClickListener {
                    if (item.isSelf == 1) {
                        DialogPainterMore(activity!!).setAnomouys(item.is_private).setForbidGraffiti(item.graffiti_switch).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_Delete -> {
                                    DialogDeleteConment(activity!!).setHintText(resources.getString(R.string.string_paint_delete_works)).setOnClickListener(View.OnClickListener {
                                        deleteItem(item)
                                    }).show()
                                }
                                R.id.tvBanGraffiti -> {//禁止涂鸦
                                    DialogGraffiti(activity!!).setTitle(if (item.graffiti_switch == 1) resources.getString(R.string.string_graffiti_ban_confirm) else resources.getString(R.string.string_graffiti_change_mode), if (item.graffiti_switch == 1) resources.getString(R.string.string_confirm) else resources.getString(R.string.string_graffiti_allow)).setOnClickListener(View.OnClickListener {
                                        operatorForbid(item)
                                    }).show()
                                }
                            }
                        }).show()
                    } else {//举报
                        DialogPainterReport(activity!!).setReportTitle(resources.getString(R.string.string_report_normal)).setArtReport(true, item.is_private).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_admin_setPrivacy -> {
                                    dialogPwd = DialogCommitPwd(activity!!).setOperator("isHidden", "1").setOnResultListener(DialogAdminPwdListener { key, value, pwd ->
                                        adminOperator(FormBody.Builder().add(key, value)
                                                .add("confirmPasswd", pwd).build(), item)
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_Report -> {
                                    DialogNormalReport(activity!!).setOnClickListener(View.OnClickListener { report ->
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
                                    dialogPwd = DialogCommitPwd(activity!!).setOperator("deleteArt", "1").setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                        adminOperatorDelete(FormBody.Builder().add("confirmPasswd", pwd).build(), item)
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_admin_user_details -> {
                                    dialogPwd = DialogCommitPwd(activity!!).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
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
                helper.getView(R.id.tv_Sub).setOnClickListener {//送给好友
                    startActivity<SelectFriendActivity>("artworkUrl" to item.artwork_url, "resourceId" to item.id.toString())
                }
                helper.getView(R.id.tvCollection).setOnClickListener {//collection
                    //创建收藏
                    if (TextUtils.isEmpty(item.collection_id)) {
                        addCollection(item)
                    } else {
                        deleteCollection(item)
                    }
                }
                helper.getView(R.id.relativeEcho).setOnClickListener {//喜欢
                    if (item.like_type == 2) {
                        unLike(item, helper.getView(R.id.iv_Thumb), helper.getTextView(R.id.tv_Echo))
                    } else
                        like(item, helper.getView(R.id.iv_Thumb), helper.getTextView(R.id.tv_Echo))
                }
                helper.getView(R.id.lineaer_Recommend).setOnClickListener {//涂鸦
                    if (item.graffiti_switch == 0) {
                        showToast("设置了禁止涂鸦")
                        return@setOnClickListener
                    }
                    if (item.isSelf == 1) {
                        if (item.chat_num > 0) {
                            startActivity<GraffitiListActivity>("resourceId" to item.id)
                        } else {
                            showToast("还没有涂鸦对话")
                        }
                    } else {//收藏
                        if (item.dialog_num > 0) {
                            TalkGraffitiDetailsActivity.open(activity!!, Intent(activity, TalkGraffitiDetailsActivity::class.java)
                                    .putExtra("resourceId", item.id)
                                    .putExtra("uid", item.user_id)
                            )
//                            startActivity<TalkGraffitiDetailsActivity>("resourceId" to item.id, "uid" to item.user_id)
                        } else {
                            startActivity<CanvasLocalActivity>("uid" to item.user_id
                                    , "artworkUrl" to item.artwork_url
                                    , "artId" to item.id
                                    , "topicName" to item.topic_name)
                        }
                    }
                }
            }
        }
        mView!!.recyclerView.layoutManager = LinearLayoutManager(activity)
        loadMore = LayoutInflater.from(activity).inflate(R.layout.view_loadmore_self_art, mView!!.recyclerView, false)
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, loadMore)
        mView!!.recyclerView.adapter = adapter
        request(currentPage)
        queryArtCount(0)
    }

    override fun initEvent() {
        mView!!.swipeRefresh.setOnRefreshListener {
            lastId = ""
            request(currentPage)
        }
        adapter.setOnLoadListener {
            request(currentPage)
        }
        mView!!.headButton.setOnChildClickListener(object : GroupToggleView.OnChildClickListener {
            override fun onClick(position: Int, childView: View) {
                mView!!.transLayout.showProgress()
                currentPage = position
                lastId = ""
                mData.clear()
                adapter.notifyDataSetChanged()
                mView!!.layoutEmptyArt.visibility = View.GONE
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                request(currentPage)
                queryArtCount(currentPage)
                mView!!.layoutEmptyArt.relativeMy.visibility = if (currentPage == 0) View.VISIBLE else View.GONE
                mView!!.layoutEmptyArt.tvEmptyHint.text = resources.getString(if (currentPage == 0) R.string.string_29 else R.string.string_30)
                loadMore.relative_Empty.text = resources.getString(if (currentPage == 0) R.string.string_29 else R.string.string_22)
            }
        })
        loadMore.relative_Empty.setOnClickListener {
            //界面跳转
            if (currentPage == 0) {
                startActivity<CanvasLocalActivity>()
            } else {
                (activity as SoulCanvasDetailsActivity).selectPage(1, false)
            }
        }
        mView!!.layoutEmptyArt.tvEmptyHint.setOnClickListener {
            if (currentPage == 0) {
                startActivity<CanvasLocalActivity>()
            } else {
                (activity as SoulCanvasDetailsActivity).selectPage(1, false)
            }
        }
    }

    private fun like(bean: PaintData.PaintBean, publicView: View, tv: TextView) {
        mView!!.transLayout.showProgress()
        OkClientHelper.post(activity, "users/${bean.user_id}/artworkLike/${bean.id}", FormBody.Builder().add("likeType", "2").build(), BaseRepData::class.java, object : OkResponse {
            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    bean.like_type = 2
                    SmallBang.attach2Window(activity).bang(publicView, 60f, null)
                    bean.publicly_like_num++
                    tv.text = resources.getString(R.string.string_recommend) + if (bean.publicly_like_num > 0) " ${bean.publicly_like_num}" else ""
                    publicView.isSelected = true
                } else {
                    showToast(result.msg)
                }
                mView!!.transLayout.showContent()
                bean.isLoading = false
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showContent()
                bean.isLoading = false
            }
        }, "V3.6")
    }

    private fun unLike(bean: PaintData.PaintBean, publicView: View, tv: TextView) {
        mView!!.transLayout.showProgress()
        OkClientHelper.delete(activity, "users/${bean.user_id}/artworkLike/${bean.id}", FormBody.Builder().add("likeType", "2").build(), BaseRepData::class.java, object : OkResponse {
            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    SmallBang.attach2Window(activity).bang(publicView, 60f, null)
                    bean.publicly_like_num--
                    tv.text = resources.getString(R.string.string_recommend) + if (bean.publicly_like_num > 0) " ${bean.publicly_like_num}" else ""
                    publicView.isSelected = false
                    bean.like_type = 0
                } else {
                    showToast(result.msg)
                }
                bean.isLoading = false
                mView!!.transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                bean.isLoading = false
                mView!!.transLayout.showContent()
            }
        }, "V3.6")
    }

    private fun reportArt(id: Int, type: String) {
        val formBody = FormBody.Builder()
                .add("reportType", type)
                .add("resourceType", "5")
                .add("resourceId", "$id")
                .build()
        OkClientHelper.post(activity, "reports", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    activity?.let { DialogReportSuccess(it).show() }
                } else
                    showToast(result.msg)
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    private fun loginAdmin(pwd: String, userId: String) {
        OkClientHelper.post(activity, "admin/users/login", FormBody.Builder().add("confirmPasswd", pwd).build(), AdminLoginData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as AdminLoginData
                if (result.code == 0) {
                    dialogPwd?.dismiss()
                    startActivity(Intent(activity, UserDetailsActivity::class.java).putExtra("id", userId))
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
        OkClientHelper.delete(activity, "admin/artwork/${item.id}", formBody, BaseRepData::class.java, object : OkResponse {
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
        OkClientHelper.patch(activity, "admin/artwork/${item.id}", formBody, BaseRepData::class.java, object : OkResponse {
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

    private fun addCollection(bean: PaintData.PaintBean) {
        mView!!.transLayout.showProgress()
        activity?.artAddCollection(bean) {
            it?.let { result ->
                if (result.code == 0) {
                    bean.collection_id = result.data.id.toString()
                    adapter.notifyItemChanged(mData.indexOf(bean))
                } else {
                    showToast(result.msg)
                }
            }
            mView!!.transLayout.showContent()
        }
    }

    private fun deleteCollection(bean: PaintData.PaintBean) {
        mView!!.transLayout.showProgress()
        activity?.artDeleteCollection(bean) {
            it?.let { result ->
                if (result.code == 0) {
                    mData.remove(bean)
                    adapter.notifyDataSetChanged()
                    if (mData.size == 0) {
                        mView!!.layoutEmptyArt.visibility = View.VISIBLE
                    }
                    artCount--
                    mView!!.tvArtCount.text = if (artCount > 0) "共${artCount}幅" else resources.getString(R.string.string_40)
                } else {
                    showToast(result.msg)
                }
            }
            mView!!.transLayout.showContent()
        }
    }

    /**
     * 删除作品
     */
    private fun deleteItem(item: PaintData.PaintBean) {
        OkClientHelper.delete(activity, "users/${item.user_id}/artwork/${item.id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    val indexOf = mData.indexOf(item)
                    mData.remove(item)
                    adapter.notifyItemRemoved(indexOf)
                    EventBus.getDefault().post(ImpUpdatePaint(1, item.id))
                    if (mData.size == 0) {
                        mView!!.layoutEmptyArt.visibility = View.VISIBLE
                    }
                    artCount--
                    mView!!.tvArtCount.text = if (artCount > 0) "共${artCount}幅" else resources.getString(R.string.string_32)
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
        mView!!.transLayout.showProgress()
        OkClientHelper.patch(activity, "users/${item.user_id}/artwork/${item.id}", FormBody.Builder().add("graffitiSwitch", if (item.graffiti_switch == 0) "1" else "0").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    item.graffiti_switch = if (item.graffiti_switch == 0) 1 else 0
                    EventBus.getDefault().post(ImpUpdatePaint(if (item.graffiti_switch == 1) 6 else 5, item.id))
                    adapter.notifyItemChanged(mData.indexOf(item))
                } else {
                    showToast(result.msg)
                }
                mView!!.transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showContent()
            }
        }, "V3.6")
    }

    private fun queryArtCount(type: Int) {
        when (type) {
            0 -> {
                OkClientHelper.get(activity, "users/${userInfoData.data.user_id}/artworksStatistics", IntegerRespData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {

                    }

                    @SuppressLint("SetTextI18n")
                    override fun success(result: Any?) {
                        result as IntegerRespData
                        if (result.code == 0) {
                            artCount = result.data.total
                            mView!!.tvArtCount.text = if (result.data.total > 0) "共${result.data.total}幅" else resources.getString(R.string.string_32)
                        }
                    }
                }, "V4.3")
            }
            1 -> {
                OkClientHelper.get(activity, "users/${userInfoData.data.user_id}/artworksCollectionStatistics", IntegerRespData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {

                    }

                    override fun success(result: Any?) {
                        result as IntegerRespData
                        if (result.code == 0) {
                            artCount = result.data.total
                            mView!!.tvArtCount.text = if (result.data.total > 0) "共${result.data.total}幅" else resources.getString(R.string.string_40)
                        }
                    }
                }, "V4.3")
            }
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {//查询自己的数据
                OkClientHelper.get(activity, "users/${userInfoData.data.user_id}/artworks?lastId=$lastId", PaintData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        mView!!.transLayout.showContent()
                        mView!!.swipeRefresh.isRefreshing = false
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        result as PaintData
                        if (result.data != null && result.data.size > 0) {
                            if (TextUtils.isEmpty(lastId)) {
                                mData.clear()
                                result.data.forEach {
                                    it.avatar_url = userInfoData.data.avatar_url
                                    it.identity_type = userInfoData.data.identity_type
                                    it.nick_name = userInfoData.data.nick_name
                                    it.isSelf = 1
                                    mData.add(it)
                                }
                                adapter.notifyDataSetChanged()
                                mView!!.recyclerView.scrollToPosition(0)
                            } else {
                                for (bean in result.data) {
                                    bean.avatar_url = userInfoData.data.avatar_url
                                    bean.identity_type = userInfoData.data.identity_type
                                    bean.nick_name = userInfoData.data.nick_name
                                    bean.isSelf = 1
                                    mData.add(bean)
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
                        if (result.data == null || result.data.size < 10)
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                        if (mData.size == 0) {
                            mView!!.layoutEmptyArt.visibility = View.VISIBLE
                        } else {
                            mView!!.layoutEmptyArt.visibility = View.GONE
                        }
                    }

                    override fun onFailure(any: Any?) {
                        mView!!.swipeRefresh.isRefreshing = false
                    }
                }, "V4.3")
            }
            1 -> {
                OkClientHelper.get(activity, "users/${userInfoData.data.user_id}/artworkCollection?lastId=$lastId", ArtHotData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        mView!!.transLayout.showContent()
                        mView!!.swipeRefresh.isRefreshing = false
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        result as ArtHotData
                        if (result.data != null && result.data.size > 0) {
                            if (TextUtils.isEmpty(lastId)) {
                                mData.clear()
                                mData.addAll(result.data.map {
                                    it.artwork.created_at = it.created_at
                                    it.artwork
                                })
                                adapter.notifyDataSetChanged()
                                mView!!.recyclerView.scrollToPosition(0)
                            } else {
                                for (bean in result.data) {
                                    bean.artwork.created_at = bean.created_at
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
                        if (result.data == null || result.data.size < 10)
                            adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                        if (mData.size == 0) {
                            mView!!.layoutEmptyArt.visibility = View.VISIBLE
                        } else {
                            mView!!.layoutEmptyArt.visibility = View.GONE
                        }
                    }

                    override fun onFailure(any: Any?) {
                        mView!!.swipeRefresh.isRefreshing = false
                    }
                }, "V4.3")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updatePaint(event: ImpUpdatePaint) {
        if (!isCurrentPage || !isResumed)
            try {
                if (event.type == 4) {
                    lastId = ""
                    request(currentPage)
                } else
                    when (event.type) {
                        1 -> {//删除
                            if (mData.size > 0) {
                                var deleteBean: PaintData.PaintBean? = null
                                for (bean in mData) {
                                    if (event.id == bean.id) {
                                        deleteBean = bean
                                        break
                                    }
                                }
                                deleteBean?.let {
                                    mData.remove(deleteBean)
                                    adapter.notifyDataSetChanged()
                                }
                                if (mData.size == 0) {
                                    adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                                }
                            }
                        }
                        2 -> {//设为隐藏
                            findById(event.id)?.let {
                                it.is_private = 1
                                adapter.notifyItemChanged(mData.indexOf(it))
                            }
                        }
                        3 -> {//设为显示
                            findById(event.id)?.let {
                                it.is_private = 0
                                adapter.notifyItemChanged(mData.indexOf(it))
                            }
                        }
                        5 -> {//禁止涂鸦
                            findById(event.id)?.let {
                                it.graffiti_switch = 0
                                adapter.notifyItemChanged(mData.indexOf(it))
                            }
                        }
                        6 -> {//允许涂鸦
                            findById(event.id)?.let {
                                it.graffiti_switch = 1
                                adapter.notifyItemChanged(mData.indexOf(it))
                            }
                        }
                        7 -> {
                            findById(event.id)?.let {
                                it.being_graffiti = 1
                                it.graffiti_num++
                                adapter.notifyItemChanged(mData.indexOf(it))
                            }
                        }
                        8 -> {
                            findById(event.id)?.let {
                                it.being_graffiti = 0
                                it.graffiti_num--
                                adapter.notifyItemChanged(mData.indexOf(it))
                            }
                        }
                    }
            } catch (e: Exception) {
            }
    }

    private fun findById(artId: Int): PaintData.PaintBean? {
        if (mData.size > 0) {
            for (bean in mData) {
                if (artId == bean.id) {
                    return bean
                }
            }
        }
        return null
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

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}