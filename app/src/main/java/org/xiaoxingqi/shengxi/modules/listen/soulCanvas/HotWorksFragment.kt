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
import org.xiaoxingqi.shengxi.model.AdminLoginData
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.PaintData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang
import skin.support.SkinCompatManager

/**
 * 热门列表 不存在匿名之类的
 */
class HotWorksFragment : BaseFragment(), ITabClickCall {
    private lateinit var adapter: QuickAdapter<PaintData.PaintBean>
    private val mData by lazy { ArrayList<PaintData.PaintBean>() }
    private var lastId = 1
    private lateinit var loginBean: UserInfoData
    private var isCurrentPage = false
    private lateinit var loadMore: View
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

    override fun getLayoutId(): Int {
        return R.layout.frag_local_painter_list
    }

    override fun initView(view: View?) {
        loginBean = PreferenceTools.getObj(activity, IConstant.USERCACHE, UserInfoData::class.java)
    }

    override fun initData() {
        EventBus.getDefault().register(this)
        adapter = object : QuickAdapter<PaintData.PaintBean>(activity, R.layout.item_user_canvas, mData) {
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
                glideUtil.loadGlide(item.user.avatar_url, helper.getImageView(R.id.roundImg), R.mipmap.icon_user_default, glideUtil.getLastModified(item.user.avatar_url))
                helper.getTextView(R.id.tv_UserName).text = item.user.nick_name
                helper.getTextView(R.id.tv_Action).text = if (TextUtils.isEmpty(item.topic_name)) "" else "#${item.topic_name}#"
                helper.getView(R.id.linearTimes).visibility = if (TextUtils.isEmpty(item.topic_name)) View.GONE else View.VISIBLE
                helper.getTextView(R.id.tvTime).visibility = View.GONE
                helper.getTextView(R.id.tv_Sub).visibility = if (item.isSelf == 1) View.VISIBLE else View.GONE
                helper.getView(R.id.tvCollection).visibility = if (item.isSelf == 1) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_Thumb).isSelected = item.like_type != 0
                helper.getTextView(R.id.tvCollection).text = if (TextUtils.isEmpty(item.collection_id)) "收藏" else "已收藏"
                helper.getTextView(R.id.tv_Echo).text = "喜欢" + if (item.publicly_like_num > 0) " ${item.publicly_like_num}" else ""
                helper.getView(R.id.ivOfficial).visibility = if (item.user_id == "1") View.VISIBLE else View.GONE
                helper.getTextView(R.id.tv_Recommend).text = if (item.graffiti_switch == 0) "禁止涂鸦" else {
                    if (item.isSelf == 1) {
                        "涂鸦对话" + if (item.chat_num > 0) " ${item.chat_num}" else ""
                    } else {
                        if (item.dialog_num > 0) "涂鸦对话 ${item.dialog_num}" else "给ta涂鸦"
                    }
                }
                helper.getView(R.id.square_img).setOnClickListener {
                    if (item.isSelf == 1) {
                        startActivity<CanvasShowActivity>("artworkUrl" to item.artwork_url, "isSend" to true, "resourceId" to item.id.toString())
                        activity?.overridePendingTransition(R.anim.act_enter_alpha, 0)
                    } else {
                        if (item.graffiti_switch != 0) {
                            startActivity(Intent(activity, CanvasLocalActivity::class.java)
                                    .putExtra("artworkUrl", item.artwork_url)
                                    .putExtra("artId", item.id)
                                    .putExtra("uid", item.user_id)
                                    .putExtra("topicName", item.topic_name))
                        } else {
                            startActivity<CanvasShowActivity>("artworkUrl" to item.artwork_url, "resourceId" to item.id.toString())
                            activity?.overridePendingTransition(R.anim.act_enter_alpha, 0)
                        }
                    }
                }
                helper.getView(R.id.relativeShare).setOnClickListener {
                    if (item.isSelf == 1) {
                        DialogPainterMore(activity!!).setForbidGraffiti(item.graffiti_switch).setOnClickListener(View.OnClickListener {
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
                    } else {
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
                helper.getView(R.id.tvCollection).setOnClickListener {
                    if (TextUtils.isEmpty(item.collection_id)) {
                        addCollection(item)
                    } else {
                        deleteCollection(item)
                    }
                }
                helper.getTextView(R.id.tv_Action).setOnClickListener {
                    startActivity(Intent(activity, PaintTopicListActivity::class.java)
                            .putExtra("topicId", item.topic_id)
                            .putExtra("topicName", item.topic_name))
                }
                helper.getView(R.id.roundImg).setOnClickListener {
                    startActivity(Intent(activity, UserDetailsActivity::class.java).putExtra("id", item.user_id))
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
                    if (item.isSelf == 1) {
                        if (item.chat_num == 0) {
                            showToast("还没有涂鸦")
                        } else {
                            startActivity<GraffitiListActivity>("resourceId" to item.id)
                        }
                    } else {
                        if (item.graffiti_switch != 0) {
                            if (item.dialog_num == 0) {
                                startActivity(Intent(activity, CanvasLocalActivity::class.java)
                                        .putExtra("uid", item.user_id)
                                        .putExtra("artworkUrl", item.artwork_url)
                                        .putExtra("artId", item.id)
                                        .putExtra("topicName", item.topic_name))
                            } else {
//                                startActivity<TalkGraffitiDetailsActivity>("resourceId" to item.id, "uid" to item.user_id)
                                TalkGraffitiDetailsActivity.open(activity!!, Intent(activity, TalkGraffitiDetailsActivity::class.java)
                                        .putExtra("resourceId", item.id)
                                        .putExtra("uid", item.user_id)
                                )
                            }
                        } else {
                            showToast("对方设置了禁止涂鸦")
                        }
                    }
                }
                helper.getView(R.id.tv_Sub).setOnClickListener {
                    startActivity<SelectFriendActivity>("artworkUrl" to item.artwork_url, "resourceId" to item.id.toString())
                }
            }
        }
        mView!!.recyclerView.layoutManager = LinearLayoutManager(activity)
        mView!!.recyclerView.adapter = adapter
        loadMore = LayoutInflater.from(activity).inflate(R.layout.view_loadmore_self_art, mView!!.recyclerView, false)
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, loadMore)
        loadMore.relative_Empty.text = resources.getString(R.string.string_31)
        request(0)
    }

    override fun initEvent() {
        mView!!.swipeRefresh.setOnRefreshListener {
            lastId = 1
            request(0)
        }
        adapter.setOnLoadListener {
            request(0)
        }
        loadMore.relative_Empty.setOnClickListener {
            (activity as SoulCanvasDetailsActivity).selectPage(2, true)
        }
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
                    bean.collection_id = ""
                    adapter.notifyItemChanged(mData.indexOf(bean))
                } else {
                    showToast(result.msg)
                }
            }
            mView!!.transLayout.showContent()
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
                    publicView.isSelected = true
                    bean.publicly_like_num++
                    tv.text = "喜欢 ${bean.publicly_like_num}"
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

    private fun unLike(bean: PaintData.PaintBean, publicView: View, tv: TextView) {
        mView!!.transLayout.showProgress()
        OkClientHelper.delete(activity, "users/${bean.user_id}/artworkLike/${bean.id}", FormBody.Builder().add("likeType", "2").build(), BaseRepData::class.java, object : OkResponse {
            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    SmallBang.attach2Window(activity).bang(publicView, 60f, null)
                    publicView.isSelected = false
                    bean.like_type = 0
                    bean.publicly_like_num--
                    tv.text = "喜欢" + if (bean.publicly_like_num > 0) " ${bean.publicly_like_num}" else ""
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

    /**
     * 删除作品
     */
    private fun deleteItem(item: PaintData.PaintBean) {
        OkClientHelper.delete(activity, "users/${item.user_id}/artwork/${item.id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    val indexOf = mData.indexOf(item)
                    EventBus.getDefault().post(ImpUpdatePaint(1, item.id))
                    mData.remove(item)
                    adapter.notifyItemRemoved(indexOf)
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
                    adapter.notifyItemChanged(mData.indexOf(item))
                    EventBus.getDefault().post(ImpUpdatePaint(if (item.graffiti_switch == 1) 6 else 5, item.id))
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

    override fun request(flag: Int) {
        OkClientHelper.get(activity, "artworks/hot?pageNo=$lastId", PaintData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                mView!!.swipeRefresh.isRefreshing = false
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                result as PaintData
                if (result.code == 0) {
                    if (lastId == 1) {
                        mData.clear()
                        result.data?.let {
                            it.forEach { bean ->
                                mData.add(bean.checkUserInfo(loginBean))
                            }
                        }
                        adapter.notifyDataSetChanged()
                    } else {
                        result.data?.forEach {
                            mData.add(it.checkUserInfo(loginBean))
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                        }
                    }
                    if (result.data != null && result.data.size >= 10) {
                        lastId++
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    } else {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                    }
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                mView!!.swipeRefresh.isRefreshing = false
            }
        }, "V4.3")
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
    fun updatePaint(event: ImpUpdatePaint) {
        if (!isCurrentPage || !isResumed)
            try {
                when (event.type) {
                    7 -> {
                        findById(event.id)?.let {
                            it.graffiti_num++
                            adapter.notifyItemChanged(mData.indexOf(it))
                        }
                        if (mData.size == 0) {
                            lastId = 1
                            request(0)
                        }
                    }
                    8 -> {
                        findById(event.id)?.let {
                            it.graffiti_num--
                            mData.remove(it)
                            adapter.notifyDataSetChanged()
                            if (mData.size == 0) {
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                            }
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

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}