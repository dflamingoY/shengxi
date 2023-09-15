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
import kotlinx.android.synthetic.main.activity_painter_topic.*
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
import org.xiaoxingqi.shengxi.model.AdminLoginData
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.PaintData
import org.xiaoxingqi.shengxi.model.TopicDetailsData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.TimeUtils
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang

/**
 * 灵魂绘图的话题展示列表
 */
class PaintTopicListActivity : BaseAct() {
    private var topicId: String? = null
    private var topicName: String? = ""
    private var lastId: String = ""
    private lateinit var adapter: QuickAdapter<PaintData.PaintBean>
    private val mData by lazy { ArrayList<PaintData.PaintBean>() }
    private lateinit var loginBean: LoginData.LoginBean
    override fun getLayoutId(): Int {
        return R.layout.activity_painter_topic
    }

    override fun initView() {
        ivRecord.setImageResource(R.mipmap.icon_open_canvas)
    }

    override fun initData() {
        loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        topicId = intent.getStringExtra("topicId")
        topicName = intent.getStringExtra("topicName")//此处出现空指针异常,未查明原因,返回的值为空
        if (null == topicName)
            topicName = ""
        tv_Content.text = topicName
        adapter = object : QuickAdapter<PaintData.PaintBean>(this, R.layout.item_user_canvas, mData) {
            @SuppressLint("SetTextI18n")
            override fun convert(helper: BaseAdapterHelper?, item: PaintData.PaintBean?) {
                Glide.with(helper!!.getImageView(R.id.square_img))
                        .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(item!!.artwork_url))
                                .error(R.drawable.drawable_default_tmpry)
                                .placeholder(R.drawable.drawable_default_tmpry))
                        .asBitmap()
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
                    helper.getTextView(R.id.tv_UserName).text = resources.getString(R.string.string_anonymous_painter)
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
                    glideUtil.loadGlide(item.avatar_url, helper.getImageView(R.id.roundImg), R.mipmap.icon_user_default, glideUtil.getLastModified(item.avatar_url))
                    helper.getTextView(R.id.tv_UserName).text = item.nick_name
                    helper.getTextView(R.id.tv_Sub).visibility = if (loginBean.user_id == item.user_id) View.VISIBLE else View.GONE
                    helper.getView(R.id.tvCollection).visibility = if (loginBean.user_id == item.user_id) View.GONE else View.VISIBLE
                }
                helper.getTextView(R.id.tv_Action).text = if (TextUtils.isEmpty(item.topic_name)) "" else "#${item.topic_name}#"
                helper.getTextView(R.id.tvTime).text = TimeUtils.getInstance().paserFriends(this@PaintTopicListActivity, item.created_at)
                helper.getTextView(R.id.tv_Echo).text = resources.getString(R.string.string_recommend) + if (item.publicly_like_num == 0) "" else " " + item.publicly_like_num
//                helper.getTextView(R.id.tv_Sub).visibility = if (loginBean.user_id == item.user_id) View.VISIBLE else View.GONE
//                helper.getView(R.id.tvCollection).visibility = if (loginBean.user_id == item.user_id) View.GONE else View.VISIBLE
                helper.getView(R.id.iv_Thumb).isSelected = item.like_type != 0
                helper.getView(R.id.ivOfficial).visibility = if (item.user_id == "1") View.VISIBLE else View.GONE
                helper.getTextView(R.id.tv_Recommend).text = if (item.graffiti_switch == 0) "禁止涂鸦" else {
                    if (item.user_id == loginBean.user_id) {
                        "涂鸦对话 " + if (item.chat_num > 0) item.chat_num else ""
                    } else {
                        if (item.dialog_num > 0) "涂鸦对话 ${item.dialog_num}" else "给ta涂鸦"
                    }
                }
                helper.getView(R.id.tvCollection).setOnClickListener {
                    if (TextUtils.isEmpty(item.collection_id)) {
                        addCollection(item)
                    } else {
                        deleteCollection(item)
                    }
                }
                helper.getView(R.id.square_img).setOnClickListener {
                    if (loginBean.user_id == item.user_id) {
                        startActivity<CanvasShowActivity>("artworkUrl" to item.artwork_url, "isSend" to true, "resourceId" to item.id.toString())
                        overridePendingTransition(R.anim.act_enter_alpha, 0)
                    } else {
                        if (item.graffiti_switch != 0) {
                            startActivity(Intent(this@PaintTopicListActivity, CanvasLocalActivity::class.java)
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
                helper.getView(R.id.roundImg).setOnClickListener {
                    if (item.is_private != 1)
                        startActivity(Intent(this@PaintTopicListActivity, UserDetailsActivity::class.java).putExtra("id", item.user_id))
                }
                helper.getView(R.id.relativeEcho).setOnClickListener {
                    if (item.like_type == 2) {
                        unLike(item, helper.getView(R.id.iv_Thumb), helper.getTextView(R.id.tv_Echo))
                    } else
                        like(item, helper.getView(R.id.iv_Thumb), helper.getTextView(R.id.tv_Echo))
                }
                helper.getView(R.id.lineaer_Recommend).setOnClickListener {
                    if (item.user_id == loginBean.user_id) {
                        if (item.dialog_num == 0) {
                            showToast("还没有涂鸦")
                        } else {
                            startActivity<GraffitiListActivity>("resourceId" to item.id)
                        }
                    } else {
                        if (item.graffiti_switch != 0) {
                            if (item.dialog_num == 0) {
                                startActivity(Intent(this@PaintTopicListActivity, CanvasLocalActivity::class.java)
                                        .putExtra("uid", item.user_id)
                                        .putExtra("artworkUrl", item.artwork_url)
                                        .putExtra("artId", item.id)
                                        .putExtra("topicName", item.topic_name))
                            } else {
//                                startActivity<TalkGraffitiDetailsActivity>("resourceId" to item.id, "uid" to item.user_id)
                                TalkGraffitiDetailsActivity.open(this@PaintTopicListActivity, Intent(this@PaintTopicListActivity, TalkGraffitiDetailsActivity::class.java)
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
                    if (item.user_id == loginBean.user_id) {
                        DialogPainterMore(this@PaintTopicListActivity).setAnomouys(item.is_private).setForbidGraffiti(item.graffiti_switch).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_Delete -> {
                                    DialogDeleteConment(this@PaintTopicListActivity).setHintText(resources.getString(R.string.string_paint_delete_works)).setOnClickListener(View.OnClickListener {
                                        deleteItem(item)
                                    }).show()
                                }
                                R.id.tvBanGraffiti -> {//禁止涂鸦
                                    DialogGraffiti(this@PaintTopicListActivity).setTitle(if (item.graffiti_switch == 1) resources.getString(R.string.string_graffiti_ban_confirm) else resources.getString(R.string.string_graffiti_change_mode), if (item.graffiti_switch == 1) resources.getString(R.string.string_confirm) else resources.getString(R.string.string_graffiti_allow)).setOnClickListener(View.OnClickListener {
                                        operatorForbid(item)
                                    }).show()
                                }
                            }
                        }).show()
                    } else {
                        DialogPainterReport(this@PaintTopicListActivity).setReportTitle(resources.getString(R.string.string_report_normal)).setArtReport(true, item.is_private).setOnClickListener(View.OnClickListener {
                            when (it.id) {
                                R.id.tv_admin_setPrivacy -> {
                                    dialogPwd = DialogCommitPwd(this@PaintTopicListActivity).setOperator("isHidden", "1").setOnResultListener(DialogAdminPwdListener { key, value, pwd ->
                                        adminOperator(FormBody.Builder().add(key, value).add("confirmPasswd", pwd).build(), item)
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_Report -> {
                                    DialogNormalReport(this@PaintTopicListActivity).setOnClickListener(View.OnClickListener { report ->
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
                                    dialogPwd = DialogCommitPwd(this@PaintTopicListActivity).setOperator("deleteArt", "1").setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
                                        adminOperatorDelete(FormBody.Builder().add("confirmPasswd", pwd).build(), item)
                                    })
                                    dialogPwd?.show()
                                }
                                R.id.tv_admin_user_details -> {//进入匿名
                                    dialogPwd = DialogCommitPwd(this@PaintTopicListActivity).setOnResultListener(DialogAdminPwdListener { _, _, pwd ->
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
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore_painter_list, recyclerView, false))
        request(0)
        request(1)
    }


    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        ivRecord.setOnClickListener {
            startActivity(Intent(this, CanvasLocalActivity::class.java)
                    .putExtra("topicName", topicName)
                    .putExtra("topicId", topicId))
        }

        swipeRefresh.setOnRefreshListener {
            lastId = ""
            request(0)
            request(1)
        }
        linearSearch.setOnClickListener { finish() }
        adapter.setOnLoadListener {
            request(1)
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
                    bean.collection_id = ""
                    adapter.notifyItemChanged(mData.indexOf(bean))
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
                    startActivity(Intent(this@PaintTopicListActivity, UserDetailsActivity::class.java).putExtra("id", userId))
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

    private fun unLike(bean: PaintData.PaintBean, publicView: View, tv: TextView) {
        transLayout.showProgress()
        OkClientHelper.delete(this, "users/${bean.user_id}/artworkLike/${bean.id}", FormBody.Builder().add("likeType", "2").build(), BaseRepData::class.java, object : OkResponse {
            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    SmallBang.attach2Window(this@PaintTopicListActivity).bang(publicView, 60f, null)
                    publicView.isSelected = false
                    bean.like_type = 0
                    bean.publicly_like_num--
                    tv.text = resources.getString(R.string.string_recommend) + if (bean.publicly_like_num > 0) " ${bean.publicly_like_num}" else ""
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


    private fun like(bean: PaintData.PaintBean, publicView: View, tv: TextView) {
        transLayout.showProgress()
        OkClientHelper.post(this, "users/${bean.user_id}/artworkLike/${bean.id}", FormBody.Builder().add("likeType", "2").build(), BaseRepData::class.java, object : OkResponse {
            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    bean.like_type = 2
                    SmallBang.attach2Window(this@PaintTopicListActivity).bang(publicView, 60f, null)
                    publicView.isSelected = true
                    bean.publicly_like_num++
                    tv.text = resources.getString(R.string.string_recommend) + " ${bean.publicly_like_num}"
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
        OkClientHelper.post(this, "reports", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    DialogReportSuccess(this@PaintTopicListActivity).show()
                } else
                    showToast(result.msg)
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "topics/$topicId?topicName=$topicName&topicType=1", TopicDetailsData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as TopicDetailsData
                        if (result.code == 0) {
                            tv_Count.text = String.format(resources.getString(R.string.string_paint_topic_count), result.data.artwork_num)
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            else -> {
                OkClientHelper.get(this, "users/0/artwork?topicName=$topicName&lastId=$lastId", PaintData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        swipeRefresh.isRefreshing = false
                        result as PaintData
                        if (result.code == 0) {
                            if (TextUtils.isEmpty(lastId)) {
                                mData.clear()
                                if (result.data != null && result.data.size > 0) {
                                    mData.addAll(result.data)
                                    adapter.notifyDataSetChanged()
                                }
                            } else {
                                for (bean in result.data) {
                                    mData.add(bean)
                                    adapter.notifyItemInserted(adapter.itemCount - 1)
                                }
                            }
                            if (mData.size > 0) {
                                lastId = mData[mData.size - 1].id.toString()
                            }
                            result.data.let {
                                if (it.size >= 10) {
                                    adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                                }
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                        swipeRefresh.isRefreshing = false
                    }
                }, "V3.6")
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
    fun updatePaint(event: ImpUpdatePaint) {
        try {
            if (event.type == 4) {
                lastId = ""
                request(1)
            } else {
                when (event.type) {
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

}