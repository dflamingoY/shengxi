package org.xiaoxingqi.shengxi.modules.listen.soulCanvas

import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.frag_recommend.view.recyclerView
import kotlinx.android.synthetic.main.frag_recommend.view.swipeRefresh
import kotlinx.android.synthetic.main.frag_recommend.view.transLayout
import kotlinx.android.synthetic.main.view_recommed_more.view.*
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
import org.xiaoxingqi.shengxi.dialog.DialogCommitPwd
import org.xiaoxingqi.shengxi.dialog.DialogLongPressArtwork
import org.xiaoxingqi.shengxi.impl.DialogAdminPwdListener
import org.xiaoxingqi.shengxi.impl.ITabClickCall
import org.xiaoxingqi.shengxi.impl.OnArtNewMsgEvent
import org.xiaoxingqi.shengxi.model.ArtHotData
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang
import skin.support.SkinCompatManager

class RecommendFrag : BaseFragment(), ITabClickCall {
    private lateinit var adapter: QuickAdapter<ArtHotData.ArtHotModel>
    private val mData by lazy { ArrayList<ArtHotData.ArtHotModel>() }
    private lateinit var loadMore: View
    private var isCurrentPage = true
    private var lastId: String = ""
    private lateinit var userInfo: UserInfoData

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
        return R.layout.frag_recommend
    }

    override fun initView(view: View?) {

    }

    override fun initData() {
        EventBus.getDefault().register(this)
        userInfo = PreferenceTools.getObj(activity, IConstant.USERCACHE, UserInfoData::class.java)
        adapter = object : QuickAdapter<ArtHotData.ArtHotModel>(activity, R.layout.item_recommend_canvas, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: ArtHotData.ArtHotModel?) {
                Glide.with(helper!!.getImageView(R.id.square_img))
                        .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(item!!.artwork.artwork_url))
                                .error(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                                .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                        )
                        .asBitmap()
                        .load(item.artwork.artwork_url)
                        .into(helper.getImageView(R.id.square_img))
                helper.getView(R.id.ivLikes).isSelected = item.artwork.like_type != 0
                helper.getTextView(R.id.tvLikes).text = if (item.artwork.publicly_like_num == 0) "" else "${item.artwork.publicly_like_num}"
                helper.getTextView(R.id.tvGraffiti).text = if (item.artwork.isSelf == 1) {
                    if (item.artwork.chat_num > 0) "${item.artwork.chat_num}" else ""
                } else {
                    if (item.artwork.dialog_num > 0) "${item.artwork.dialog_num}" else ""
                }
                helper.getView(R.id.ivCollection).isSelected = !TextUtils.isEmpty(item.artwork.collection_id)
                if (item.artwork.isSelf == 1) {
                    helper.getView(R.id.relativeUser).visibility = View.GONE
                    helper.getView(R.id.tvSelf).visibility = View.VISIBLE
                    helper.getView(R.id.relativeCollection).visibility = View.GONE
                } else {
                    helper.getView(R.id.relativeCollection).visibility = View.VISIBLE
                    helper.getView(R.id.relativeUser).visibility = View.VISIBLE
                    helper.getView(R.id.tvSelf).visibility = View.GONE
                    glideUtil.loadGlide(item.artwork.user.avatar_url, helper.getImageView(R.id.roundImg), 0, glideUtil.getLastModified(item.artwork.user.avatar_url))
                    helper.getView(R.id.iv_user_type).visibility = if (item.artwork.identity_type == 0) View.GONE else View.VISIBLE
                    helper.getView(R.id.iv_user_type).isSelected = item.artwork.identity_type == 1
                }
                helper.getView(R.id.cardView).setOnClickListener {
                    when (ArtUserRelationDelegate.getInstance().getRelationById(item.artwork.user_id)) {
                        -1 -> {
                            queryRelation(item) {
                                startActivity<UserDetailsActivity>("id" to item.artwork.user_id)
                            }
                        }
                        0 -> {
                            startActivity<UserDetailsActivity>("id" to item.artwork.user_id)
                        }
                        1 -> {
                            showToast("操作无法执行, 你们之间有人屏蔽了对方")
                        }
                    }
                }
                helper.getView(R.id.square_img).setOnClickListener {
                    if (item.artwork.isSelf == 1) {
                        startActivity<CanvasShowActivity>("artworkUrl" to item.artwork.artwork_url, "resourceId" to item.artwork_id.toString(), "isSend" to true)
                        activity?.overridePendingTransition(R.anim.act_enter_alpha, 0)
                    } else {
                        if (item.artwork.graffiti_switch == 0) {//禁止涂鸦 展示大图
                            startActivity<CanvasShowActivity>("artworkUrl" to item.artwork.artwork_url, "resourceId" to "")
                            activity?.overridePendingTransition(R.anim.act_enter_alpha, 0)
                        } else {//允许 跳转涂鸦
                            when (ArtUserRelationDelegate.getInstance().getRelationById(item.artwork.user_id)) {
                                -1 -> {
                                    queryRelation(item) {
                                        startActivity<CanvasLocalActivity>(
                                                ("artworkUrl" to item.artwork.artwork_url)
                                                , ("artId" to item.artwork.id)
                                                , ("uid" to item.artwork.user_id)
                                                , ("topicName" to item.artwork.topic_name))
                                    }
                                }
                                0 -> {
                                    startActivity<CanvasLocalActivity>(
                                            ("artworkUrl" to item.artwork.artwork_url)
                                            , ("artId" to item.artwork.id)
                                            , ("uid" to item.artwork.user_id)
                                            , ("topicName" to item.artwork.topic_name))
                                }
                                1 -> {
                                    showToast("操作无法执行, 你们之间有人屏蔽了对方")
                                }
                            }
                        }
                    }
                }
                helper.getView(R.id.relativeLikes).setOnClickListener {
                    when (ArtUserRelationDelegate.getInstance().getRelationById(item.artwork.user_id)) {
                        -1 -> {
                            queryRelation(item) {
                                if (item.artwork.like_type == 2) {
                                    unLike(item, helper.getView(R.id.ivLikes), helper.getTextView(R.id.tvLikes))
                                } else
                                    like(item, helper.getView(R.id.ivLikes), helper.getTextView(R.id.tvLikes))
                            }
                        }
                        0 -> {
                            if (item.artwork.like_type == 2) {
                                unLike(item, helper.getView(R.id.ivLikes), helper.getTextView(R.id.tvLikes))
                            } else
                                like(item, helper.getView(R.id.ivLikes), helper.getTextView(R.id.tvLikes))
                        }
                        1 -> {
                            showToast("操作无法执行, 你们之间有人屏蔽了对方")
                        }
                    }
                }
                helper.getView(R.id.relativeCollection).setOnClickListener {
                    when (ArtUserRelationDelegate.getInstance().getRelationById(item.artwork.user_id)) {
                        -1 -> {
                            queryRelation(item) {
                                if (TextUtils.isEmpty(item.artwork.collection_id)) {
                                    addCollection(item, helper.getView(R.id.ivCollection), helper.getTextView(R.id.tvCollections))
                                } else {
                                    deleteCollection(item, helper.getView(R.id.ivCollection), helper.getTextView(R.id.tvCollections))
                                }
                            }
                        }
                        0 -> {
                            if (TextUtils.isEmpty(item.artwork.collection_id)) {
                                addCollection(item, helper.getView(R.id.ivCollection), helper.getTextView(R.id.tvCollections))
                            } else {
                                deleteCollection(item, helper.getView(R.id.ivCollection), helper.getTextView(R.id.tvCollections))
                            }
                        }
                        1 -> {
                            showToast("操作无法执行, 你们之间有人屏蔽了对方")
                        }
                    }
                }
                helper.getView(R.id.linearGraffiti).setOnClickListener {
                    if (item.artwork.isSelf == 1) {
                        if (item.artwork.chat_num > 0) {
                            //展示列表作品的涂鸦对话列表
                            startActivity<GraffitiListActivity>("resourceId" to item.artwork_id)
                        } else {
                            startActivity<CanvasShowActivity>("artworkUrl" to item.artwork.artwork_url, "resourceId" to item.artwork_id.toString(), "isSend" to true)
                            activity?.overridePendingTransition(R.anim.act_enter_alpha, 0)
                        }
                    } else {
                        if (item.artwork.graffiti_switch == 0) {
                            showToast("对方设置了禁止涂鸦")
                        } else {
                            //查询拉黑关系
                            if (item.artwork.dialog_num > 0) {
                                //进入列表
                                TalkGraffitiDetailsActivity.open(activity!!, Intent(activity, TalkGraffitiDetailsActivity::class.java)
                                        .putExtra("resourceId", item.artwork_id)
                                        .putExtra("uid", item.artwork.user_id)
                                )
//                                startActivity<TalkGraffitiDetailsActivity>("resourceId" to item.artwork_id, "uid" to item.artwork.user_id)
                            } else
                                when (ArtUserRelationDelegate.getInstance().getRelationById(item.artwork.user_id)) {
                                    -1 -> {
                                        queryRelation(item) {
                                            startActivity<CanvasLocalActivity>(
                                                    ("artworkUrl" to item.artwork.artwork_url)
                                                    , ("artId" to item.artwork.id)
                                                    , ("uid" to item.artwork.user_id)
                                                    , ("topicName" to item.artwork.topic_name))
                                        }
                                    }
                                    0 -> {
                                        startActivity<CanvasLocalActivity>(
                                                ("artworkUrl" to item.artwork.artwork_url)
                                                , ("artId" to item.artwork.id)
                                                , ("uid" to item.artwork.user_id)
                                                , ("topicName" to item.artwork.topic_name))
                                    }
                                    1 -> {
                                        showToast("操作无法执行, 你们之间有人屏蔽了对方")
                                    }
                                }
                        }
                    }
                }
                if (IConstant.userAdminArray.contains(userInfo.data.user_id)) {
                    helper.getView(R.id.square_img).setOnLongClickListener {
                        DialogLongPressArtwork(activity!!).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tvOfficialTop -> {
                                    dialogPwd = DialogCommitPwd(activity!!).setOperator("topAt", null).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                        cancelArtTop(item, FormBody.Builder().add("confirmPasswd", pwd).add("topAt", "0").build())
                                    })
                                    dialogPwd?.show()
                                }
                            }

                        }).show()
                        false
                    }
                }
            }
        }
        mView!!.recyclerView.layoutManager = LinearLayoutManager(activity)
        mView!!.recyclerView.adapter = adapter
        loadMore = LayoutInflater.from(activity).inflate(R.layout.view_recommed_more, mView!!.recyclerView, false)
        adapter.setLoadMoreEnable(mView!!.recyclerView, mView!!.recyclerView.layoutManager, loadMore)
        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
        request(0)
    }

    override fun initEvent() {
        mView!!.swipeRefresh.setOnRefreshListener {
            lastId = ""
            request(0)
        }
        adapter.setOnLoadListener {
            request(0)
        }
        loadMore.relative_Empty.setOnClickListener {
            (activity as SoulCanvasDetailsActivity).selectPage(1, true)
        }
    }

    private fun cancelArtTop(bean: ArtHotData.ArtHotModel, formBody: FormBody) {
        OkClientHelper.patch(activity, "admin/artwork/${bean.artwork_id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
                dialogPwd?.setCallBack()
            }

            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    showToast("操作成功")
                    dialogPwd?.dismiss()
                    mData.remove(bean)
                    adapter.notifyDataSetChanged()
                } else {
                    dialogPwd?.setCallBack()
                    showToast(result.msg)
                }
            }
        })
    }

    private fun queryRelation(item: ArtHotData.ArtHotModel, function: () -> Unit) {
        activity!!.artRelation(item.artwork.user_id) {
            if (it?.let {
                        if (it.data.friend_status != 3 && it.data.friend_status != 4) {
                            function()
                        } else {
                            showToast("操作无法执行, 你们之间有人屏蔽了对方")
                        }
                        it
                    } == null) {
                showToast("网络异常, 请稍候再试")
            }
        }
    }

    private fun addCollection(bean: ArtHotData.ArtHotModel, iv: View, tv: TextView) {
        mView!!.transLayout.showProgress()
        activity?.artAddCollection(bean.artwork) {
            it?.let { result ->
                if (result.code == 0) {
                    bean.artwork.collection_id = result.data.id.toString()
                    iv.isSelected = true
                } else {
                    showToast(result.msg)
                }
            }
            mView!!.transLayout.showContent()
        }
    }

    private fun deleteCollection(bean: ArtHotData.ArtHotModel, iv: View, tv: TextView) {
        mView!!.transLayout.showProgress()
        activity?.artDeleteCollection(bean.artwork) {
            it?.let { result ->
                if (result.code == 0) {
                    bean.artwork.collection_id = ""
                    iv.isSelected = false
                } else {
                    showToast(result.msg)
                }
            }
            mView!!.transLayout.showContent()
        }
    }

    private fun like(bean: ArtHotData.ArtHotModel, publicView: View, tv: TextView) {
        mView!!.transLayout.showProgress()
        OkClientHelper.post(activity, "users/${bean.artwork.user_id}/artworkLike/${bean.artwork_id}", FormBody.Builder().add("likeType", "2").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                mView!!.transLayout.showContent()
                result as BaseRepData
                if (result.code == 0) {
                    bean.artwork.like_type = 2
                    SmallBang.attach2Window(activity).bang(publicView, 60f, null)
                    publicView.isSelected = true
                    bean.artwork.publicly_like_num++
                    tv.text = "${bean.artwork.publicly_like_num}"
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showContent()
            }
        }, "V3.6")
    }

    private fun unLike(bean: ArtHotData.ArtHotModel, publicView: View, tv: TextView) {
        mView!!.transLayout.showProgress()
        OkClientHelper.delete(activity, "users/${bean.artwork.user_id}/artworkLike/${bean.artwork_id}", FormBody.Builder().add("likeType", "2").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    SmallBang.attach2Window(activity).bang(publicView, 60f, null)
                    publicView.isSelected = false
                    bean.artwork.publicly_like_num--
                    tv.text = if (bean.artwork.publicly_like_num == 0) "" else "${bean.artwork.publicly_like_num}"
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


    override fun request(flag: Int) {
        OkClientHelper.get(activity, "artworks/top?lastId=$lastId", ArtHotData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                mView!!.swipeRefresh.isRefreshing = false
            }

            override fun success(result: Any?) {
                mView!!.swipeRefresh.isRefreshing = false
                result as ArtHotData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                result.data?.let {
                    if (TextUtils.isEmpty(lastId)) {
                        mData.clear()
                    }
                    it.forEach { bean ->
                        bean.artwork.checkUserInfo(userInfo)
                        mData.add(bean)
                    }
                    adapter.notifyDataSetChanged()
                    if (it.size >= 10) {
                        lastId = it[it.size - 1].id.toString()
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }
            }
        }, "V4.3")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun msgReceive(event: OnArtNewMsgEvent) {
        if (TextUtils.isEmpty(event.bean.action)) {
            mData.loop {
                it.artwork_id == event.bean.data.resource_id
            }?.let {
                it.artwork.dialog_num++
                adapter.notifyItemChanged(mData.indexOf(it))
            }
        } else {//撤回消息
            mData.loop {
                it.artwork_id == event.bean.data.resource_id
            }?.let {
                it.artwork.dialog_num--
                adapter.notifyItemChanged(mData.indexOf(it))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}