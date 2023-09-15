package org.xiaoxingqi.shengxi.modules.user.frag

import android.content.Intent
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.activity_photo_recycler.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogCreateVoice
import org.xiaoxingqi.shengxi.dialog.DialogLimitTalk
import org.xiaoxingqi.shengxi.dialog.DialogUserSet
import org.xiaoxingqi.shengxi.impl.INotifyFriendStatus
import org.xiaoxingqi.shengxi.impl.OperatorVoiceListEvent
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.publicmoudle.RecordTransparentActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.SendAct
import org.xiaoxingqi.shengxi.utils.*
import java.util.ArrayList
import kotlin.math.ceil

class UserPhotoListActivity : BaseAct() {
    companion object {
        private const val REQUEST_PHOTO = 0x01
    }

    private val mData by lazy {
        ArrayList<BaseImg>()
    }
    private lateinit var adapter: QuickAdapter<BaseImg>
    private lateinit var loadMoreView: View
    private var lastId: String? = null
    private var userId: String? = null
    private var relation = 5//-1 表示自己   0陌生人 1 待验证 2 好友
    private var strangeView = 7//0=禁止，7=默认7天
    private var voiceTotalLength = 0

    override fun getLayoutId(): Int {
        return R.layout.activity_photo_recycler
    }

    override fun initView() {

    }

    override fun initData() {
        userId = intent.getStringExtra("userId")
        voiceTotalLength = intent.getIntExtra("voiceLength", 0)
        adapter = object : QuickAdapter<BaseImg>(this, R.layout.item_photo_img, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: BaseImg?) {
                val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                val position = helper!!.itemView.tag as Int
                if (position.rem(3) != 0) {
                    params.setMargins(AppTools.dp2px(this@UserPhotoListActivity, 1), 0, 0, AppTools.dp2px(this@UserPhotoListActivity, 1))
                } else {
                    params.setMargins(0, 0, 0, AppTools.dp2px(this@UserPhotoListActivity, 1))
                }
                helper.getView(R.id.cardLogo).layoutParams = params
                var url = item!!.img_url
                if (url.contains(".gif")) {
                    if (url.contains("?")) {
                        url = url.substring(0, url.indexOf("?"))
                    }
                }
                Glide.with(this@UserPhotoListActivity)
                        .applyDefaultRequestOptions(RequestOptions().error(R.drawable.drawable_default_tmpry)
                                .signature(ObjectKey(url))
                                .placeholder(R.drawable.drawable_default_tmpry))
                        .asBitmap()
                        .load(url)
                        .error(Glide.with(this@UserPhotoListActivity)
                                .applyDefaultRequestOptions(RequestOptions()
                                        .signature(ObjectKey(if (url.contains("?")) url.substring(0, url.indexOf("?")) else url)))
                                .asBitmap()
                                .load(if (url.contains("?")) url.substring(0, url.indexOf("?")) else url))
                        .into(helper.getImageView(R.id.iv_img))
            }
        }
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter
        loadMoreView = LayoutInflater.from(this).inflate(R.layout.layout_photo_loadmore, recyclerView, false)
        adapter.setLoadMoreEnable(recyclerView, recyclerView.layoutManager, loadMoreView)
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val character = SPUtils.getString(this, IConstant.USERCHARACTERTYPE + loginBean.user_id, "I")
       /* if (IConstant.USER_EXTROVERT.equals(character, true)) {
            transLayout.findViewById<TextView>(R.id.tv_UploadPhoto).text = resources.getString(R.string.string_moodList_photo_e_2)
        }*/
        transLayout.findViewById<View>(R.id.tv_Photo_Hint).isSelected = AppTools.getLanguage(this) == IConstant.HK || AppTools.getLanguage(this) == IConstant.TW
        if (loginBean.user_id == userId) {
            request(2)
        } else {
            tv_Title.text = resources.getString(R.string.string_title_4)
            if (voiceTotalLength == 0) {
                transLayout.showEmpty()
                transLayout.findViewById<View>(R.id.linearPhotoOther).visibility = View.VISIBLE
                transLayout.findViewById<View>(R.id.tv_CreateFriend).visibility = View.GONE
                transLayout.findViewById<TextView>(R.id.tv_OtherHint).text = resources.getString(R.string.string_empty_photo_1)
            } else {
                relative_Bottom.visibility = View.GONE
                request(0)
            }
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            finish()
        }
        adapter.setOnLoadListener {
            request(2)
        }
        tv_AddFriends.setOnClickListener {
            if (!it.isSelected) {
                requestFriends()
            }
        }
        transLayout.findViewById<View>(R.id.tv_CreateFriend).setOnClickListener {
            if (!it.isSelected) {
                requestFriends()
            }
        }
        adapter.setOnItemClickListener { _, position ->
            DataHelper.getInstance().addData(mData)
            startActivityForResult(Intent(this, PhotoDetailsActivity::class.java)
                    .putExtra("current", position)
                    .putExtra("uid", userId)
                    .putExtra("lastId", lastId)
                    , REQUEST_PHOTO)
        }
        transLayout.findViewById<View>(R.id.tv_UploadPhoto).setOnClickListener {
            if (!RecordTransparentActivity.isOnCreate) {
                startActivity(Intent(this, RecordTransparentActivity::class.java)
                        .putExtra("type", 1))
                overridePendingTransition(0, 0)
            }
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "relations/$userId", RelationData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        /**
                         * 查询用户关系, 是否是好友
                         */
                        result as RelationData
                        if (result.code == 0) {
                            relation = result.data.friend_status
                            if (result.data != null) {
                                if (result.data.friend_status == 2) {//好友
                                    relative_Bottom.visibility = View.GONE
                                    loadMoreView.findViewById<TextView>(R.id.tv_empty_desc).text = resources.getString(R.string.string_empty_photo_4)
                                    request(2)
                                } else {
                                    if (result.data.friend_status == 1) {//请求已发送待验证
                                        tv_AddFriends.text = resources.getString(R.string.string_pending)
                                        tv_AddFriends.isSelected = true
                                        transLayout.findViewById<TextView>(R.id.tv_CreateFriend).isSelected = true
                                        transLayout.findViewById<TextView>(R.id.tv_CreateFriend).text = resources.getString(R.string.string_pending)
                                    }
                                    if (result.data.whitelist != null) {//白名单 可以查看全部的心情
                                        relative_Bottom.visibility = View.VISIBLE
                                        tv_empty_photo.text = String.format(resources.getString(R.string.string_add_white_list_voice), ceil((result.data.whitelist.released_at - System.currentTimeMillis() / 1000) / 60.0 / 60 / 24).toInt())
                                        request(2)
                                    } else {//非白名单
                                        request(1)
                                    }
                                }
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showOffline()
                    }
                })
            }
            1 -> {
                OkClientHelper.get(this, "users/$userId/settings?settingName=voice_visible_days&settingTag=moodbook", NewVersionSetSingleData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as NewVersionSetSingleData
                        if (result.code == 0) {
                            result.data?.let {
                                result.data?.let {
                                    if (it.setting_name == "voice_visible_days") {
                                        strangeView = it.setting_value
                                    }
                                }
                            }
                            if (strangeView == 0) {//禁止其他人浏览
                                transLayout.showEmpty()
                                transLayout.findViewById<View>(R.id.linearPhotoOther).visibility = View.VISIBLE
                                transLayout.findViewById<TextView>(R.id.tv_OtherHint).text = resources.getString(R.string.string_empty_photo_2)
                            } else {
                                tv_empty_photo.text = String.format(resources.getString(R.string.string_empty_photo_3), when (strangeView) {
                                    7 -> {
                                        relative_Bottom.visibility = View.VISIBLE
                                        loadMoreView.findViewById<TextView>(R.id.tv_empty_desc).text = resources.getString(R.string.string_empty_photo_1)
                                        "七"
                                    }
                                    30 -> {
                                        relative_Bottom.visibility = View.VISIBLE
                                        loadMoreView.findViewById<TextView>(R.id.tv_empty_desc).text = resources.getString(R.string.string_empty_photo_1)
                                        "三十"
                                    }
                                    else -> {//all
                                        loadMoreView.findViewById<TextView>(R.id.tv_empty_desc).text = resources.getString(R.string.string_empty_photo_4)
                                        "全部"
                                    }
                                })
                                request(2)
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V4.2")
            }
            2 -> {
                OkClientHelper.get(this, "users/$userId/voices?moduleId=3&lastId=$lastId", VoiceData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VoiceData
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        if (result.data != null && result.data.size > 0) {
                            for (item in result.data) {
                                if (item.img_list != null && item.img_list.size > 0) {
                                    for (bean in item.img_list) {
                                        val baseImg = BaseImg()
                                        baseImg.img_url = bean
                                        baseImg.voiceBean = item
                                        baseImg.title = "${item.img_list.indexOf(bean) + 1}/${item.img_list.size}"
                                        mData.add(baseImg)
                                        adapter.notifyItemInserted(adapter.itemCount - 1)
                                    }
                                }
                            }
                            lastId = result.data[result.data.size - 1].voice_id
                            if (result.data.size >= 10) {
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                            }
                        }
                        if (mData.size > 0) {//有数据
                            transLayout.showContent()
                        } else {//无数据
                            if (relation == 5) {//自己
                                transLayout.showEmpty()
                                transLayout.findViewById<View>(R.id.linearPhotoSelf).visibility = View.VISIBLE
                            } else if (relation == 0 || relation == 1) {//非好友
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                            } else {//好友
                                transLayout.findViewById<TextView>(R.id.tv_OtherHint).text = resources.getString(R.string.string_empty_voice_list_11)
                                transLayout.findViewById<View>(R.id.tv_CreateFriend).visibility = View.GONE
                                transLayout.findViewById<View>(R.id.linearPhotoOther).visibility = View.VISIBLE
                                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.NULLDATA)
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
        }
    }

    private fun requestFriends() {
        transLayout.findViewById<View>(R.id.tv_CreateFriend).isSelected = true
        tv_AddFriends.isSelected = true
        val formBody = FormBody.Builder()
                .add("toUserId", userId)
                .build()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "users/${loginBean.user_id}/friendslog", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    transLayout.findViewById<View>(R.id.tv_CreateFriend).isSelected = true
                    tv_AddFriends.isSelected = true
                    tv_AddFriends.text = resources.getString(R.string.string_pending)
                    transLayout.findViewById<TextView>(R.id.tv_CreateFriend).text = resources.getString(R.string.string_pending)
                    EventBus.getDefault().post(INotifyFriendStatus(1, userId))
                    if (SPUtils.getInt(this@UserPhotoListActivity, IConstant.TOTALLENGTH + loginBean.user_id, 0) == 0) {
                        DialogCreateVoice(this@UserPhotoListActivity).show()
                    } else if (SPUtils.getBoolean(this@UserPhotoListActivity, IConstant.STRANGEVIEW + loginBean.user_id, false)) {
                        DialogUserSet(this@UserPhotoListActivity).setOnClickListener(View.OnClickListener {
                            userId?.let { it1 -> addWhiteList(it1) }
                        }).show()
                    }
                } else {
                    transLayout.findViewById<View>(R.id.tv_CreateFriend).isSelected = false
                    tv_AddFriends.isSelected = false
                    if (SPUtils.getLong(this@UserPhotoListActivity, IConstant.USERLIMITTIME, 0L) > System.currentTimeMillis() / 1000) {
                        DialogLimitTalk(this@UserPhotoListActivity).show()
                    } else
                        showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PHOTO) {
            data?.let {
                lastId = it.getStringExtra("lastId")
                //同步在单页请求到的数据
                if (mData.size > 0) {
                    if (DataHelper.getInstance().imgBeans.size > mData.size) {
                        try {
                            val temp = DataHelper.getInstance().imgBeans as ArrayList<BaseImg>
                            mData.addAll(temp.subList(mData.size, temp.size))
                            adapter.notifyDataSetChanged()
                        } catch (e: Exception) {
                        }
                    }
                }
            }
        }
    }

    /**
     * 更新好友请求的状态,
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun friendsChange(event: INotifyFriendStatus) {
        if (event.status == 1) {
            try {
                transLayout.findViewById<View>(R.id.tv_CreateFriend).isSelected = true
                tv_AddFriends.isSelected = true
                tv_AddFriends.text = resources.getString(R.string.string_pending)
                transLayout.findViewById<TextView>(R.id.tv_CreateFriend).text = resources.getString(R.string.string_pending)
            } catch (e: Exception) {
            }
        } else if (event.status == 2 || event.status == 0) {
            try {
                if (event.userId == userId) {
                    lastId = null
                    request(0)
                }
            } catch (e: Exception) {

            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun operatorEvent(event: OperatorVoiceListEvent) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (loginBean.user_id == userId) {
            if (event.type == 2) {
                lastId = null
                request(2)
            } else if (event.type == 3) {//删除

                /**
                 * 删除集合中的数据
                 */
                var indicator = -1
                for (index in mData.indices) {
                    if (mData[index].voiceBean.voice_id == event.voice_id) {
                        //记录当前的角标和数量  移除列表
                        indicator = index
                        break
                    }
                }
                if (indicator != -1) {
                    for (count in 1..mData[indicator].voiceBean.img_list.size) {
                        mData.removeAt(indicator)
                    }
                    adapter.notifyDataSetChanged()
                    if (mData.size == 0) {
                        transLayout.showEmpty()
                        if (relation == 5) {//自己
                            transLayout.findViewById<View>(R.id.linearPhotoSelf).visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

}