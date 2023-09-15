package org.xiaoxingqi.shengxi.modules.user.frag

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.frag_season_scenes.view.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogCheckSeasonAlbum
import org.xiaoxingqi.shengxi.dialog.DialogCreateVoice
import org.xiaoxingqi.shengxi.dialog.DialogLimitTalk
import org.xiaoxingqi.shengxi.dialog.DialogUserSet
import org.xiaoxingqi.shengxi.impl.INotifyFriendStatus
import org.xiaoxingqi.shengxi.impl.ITabClickCall
import org.xiaoxingqi.shengxi.impl.IUpdateAlbumEvent
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.RelationData
import org.xiaoxingqi.shengxi.model.VoiceAlbumData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.user.frag.addAlbum.AddAlbumActivity
import org.xiaoxingqi.shengxi.modules.user.frag.addAlbum.PreViewSeasonActivity
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.utils.SPUtils
import java.util.*
import kotlin.collections.ArrayList

/**
 * 四季界面
 */
class SeasonScenesFragment : BaseFragment(), ITabClickCall {

    private lateinit var publicAdapter: QuickAdapter<VoiceAlbumData.AlbumDataBean>
    private lateinit var friendAdapter: QuickAdapter<VoiceAlbumData.AlbumDataBean>
    private lateinit var privacyAdapter: QuickAdapter<VoiceAlbumData.AlbumDataBean>
    private lateinit var otherAdapter: QuickAdapter<VoiceAlbumData.AlbumDataBean>
    private val publicData by lazy { ArrayList<VoiceAlbumData.AlbumDataBean>() }
    private val friendData by lazy { ArrayList<VoiceAlbumData.AlbumDataBean>() }
    private val privacyData by lazy { ArrayList<VoiceAlbumData.AlbumDataBean>() }
    private val otherData by lazy { ArrayList<VoiceAlbumData.AlbumDataBean>() }
    private var lastIdPublic = ""
    private var lastIdFriend = ""
    private var lastIdPrivacy = ""
    private var userId: String? = null
    private var lastOther = ""
    override fun tabClick(isVisible: Boolean) {

    }

    override fun doubleClickRefresh() {

    }

    override fun getLayoutId(): Int {
        return R.layout.frag_season_scenes
    }

    override fun initView(view: View?) {
        view!!.recycler_public.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        view.recycler_friends.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        view.recycler_privacy.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        view.recycler_public.isNestedScrollingEnabled = false
        view.recycler_friends.isNestedScrollingEnabled = false
        view.recycler_privacy.isNestedScrollingEnabled = false
        view.recyclerView_other.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
    }

    override fun initData() {
        EventBus.getDefault().register(this)
//        (activity as UserHomeActivity).let {
//            userId = it.uid
//        }
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (loginBean.user_id == userId) {//自己
            val seasonFriend = SPUtils.getString(activity, IConstant.SEASON_FRIEND + userId, "1,2,3")
            if (seasonFriend.contains("3")) {
                SPUtils.setString(activity, IConstant.SEASON_FRIEND + userId, seasonFriend.replace("3", ""))
            }
            val seasonPrivacy = SPUtils.getString(activity, IConstant.SEASON_PRIVACY + userId, "1,2,3")
            if (seasonPrivacy.contains("2") || seasonPrivacy.contains("3")) {
                SPUtils.setString(activity, IConstant.SEASON_PRIVACY + userId, seasonPrivacy.replace("2", "").replace("3", ""))
            }
            parseEmptyData()
            mView!!.relative_other.visibility = View.GONE
        } else {//other
            mView!!.tv_add_album.visibility = View.GONE
            mView!!.relative_other.visibility = View.VISIBLE
            mView!!.nested_self.visibility = View.GONE
        }
        publicAdapter = object : QuickAdapter<VoiceAlbumData.AlbumDataBean>(activity, R.layout.item_album_cover, publicData) {
            val width = ((AppTools.getWindowsWidth(context) - AppTools.dp2px(context, 29)) / 2.9f + 0.5f).toInt()

            override fun convert(helper: BaseAdapterHelper?, item: VoiceAlbumData.AlbumDataBean?) {
                val params = RelativeLayout.LayoutParams(width, width)
                when (helper!!.itemView.tag as Int) {
                    0 -> {
                        params.setMargins(AppTools.dp2px(context, 15), 0, 0, 0)
                    }
                    else -> {
                        params.setMargins(AppTools.dp2px(context, 7), 0, 0, 0)
                    }
                }
                helper.getView(R.id.cardLayout).layoutParams = params
                Glide.with(context)
                        .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(item!!.album_cover_url)))
                        .load(item.album_cover_url)
                        .into(helper.getImageView(R.id.iv_album_cover))
                if (TextUtils.isEmpty(item.user_id)) {
                    helper.getTextView(R.id.tv_album_empty_name).text = item.album_name
                    helper.getTextView(R.id.tv_album_name).text = ""
                } else {
                    helper.getTextView(R.id.tv_album_empty_name).text = ""
                    helper.getTextView(R.id.tv_album_name).text = item.album_name
                }
            }
        }
        mView!!.recycler_public.adapter = publicAdapter
        publicAdapter.setLoadMoreEnable(mView!!.recycler_public, mView!!.recycler_public.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore_horizontal, mView!!.recycler_public, false))
        friendAdapter = object : QuickAdapter<VoiceAlbumData.AlbumDataBean>(activity, R.layout.item_album_cover, friendData) {
            val width = ((AppTools.getWindowsWidth(context) - AppTools.dp2px(context, 29)) / 2.9f + 0.5f).toInt()

            override fun convert(helper: BaseAdapterHelper?, item: VoiceAlbumData.AlbumDataBean?) {
                val params = RelativeLayout.LayoutParams(width, width)
                when (helper!!.itemView.tag as Int) {
                    0 -> {
                        params.setMargins(AppTools.dp2px(context, 15), 0, 0, 0)
                    }
                    else -> {
                        params.setMargins(AppTools.dp2px(context, 7), 0, 0, 0)
                    }
                }
                helper.getView(R.id.cardLayout).layoutParams = params
                Glide.with(context)
                        .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(item!!.album_cover_url)))
                        .load(item.album_cover_url)
                        .into(helper.getImageView(R.id.iv_album_cover))
                if (TextUtils.isEmpty(item.user_id)) {
                    helper.getTextView(R.id.tv_album_empty_name).text = item.album_name
                    helper.getTextView(R.id.tv_album_name).text = ""
                } else {
                    helper.getTextView(R.id.tv_album_empty_name).text = ""
                    helper.getTextView(R.id.tv_album_name).text = item.album_name
                }
            }
        }
        mView!!.recycler_friends.adapter = friendAdapter
        friendAdapter.setLoadMoreEnable(mView!!.recycler_friends, mView!!.recycler_friends.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore_horizontal, mView!!.recycler_friends, false))
        privacyAdapter = object : QuickAdapter<VoiceAlbumData.AlbumDataBean>(activity, R.layout.item_album_cover, privacyData) {
            val width = ((AppTools.getWindowsWidth(context) - AppTools.dp2px(context, 29)) / 2.9f + 0.5f).toInt()
            override fun convert(helper: BaseAdapterHelper?, item: VoiceAlbumData.AlbumDataBean?) {
                val params = RelativeLayout.LayoutParams(width, width)
                when (helper!!.itemView.tag as Int) {
                    0 -> {
                        params.setMargins(AppTools.dp2px(context, 15), 0, 0, 0)
                    }
                    else -> {
                        params.setMargins(AppTools.dp2px(context, 7), 0, 0, 0)
                    }
                }
                helper.getView(R.id.cardLayout).layoutParams = params
                Glide.with(context)
                        .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(item!!.album_cover_url)))
                        .load(item.album_cover_url)
                        .into(helper.getImageView(R.id.iv_album_cover))
                if (TextUtils.isEmpty(item.user_id)) {
                    helper.getTextView(R.id.tv_album_empty_name).text = item.album_name
                    helper.getTextView(R.id.tv_album_name).text = ""
                } else {
                    helper.getTextView(R.id.tv_album_empty_name).text = ""
                    helper.getTextView(R.id.tv_album_name).text = item.album_name
                }
            }
        }
        mView!!.recycler_privacy.adapter = privacyAdapter
        privacyAdapter.setLoadMoreEnable(mView!!.recycler_privacy, mView!!.recycler_privacy.layoutManager, LayoutInflater.from(activity).inflate(R.layout.view_loadmore_horizontal, mView!!.recycler_privacy, false))
        otherAdapter = object : QuickAdapter<VoiceAlbumData.AlbumDataBean>(activity, R.layout.item_album_cover, otherData) {
            val width = ((AppTools.getWindowsWidth(context) - AppTools.dp2px(context, 23)) / 1.03f + 0.5f).toInt()
            override fun convert(helper: BaseAdapterHelper?, item: VoiceAlbumData.AlbumDataBean?) {
                val params = RelativeLayout.LayoutParams(width, width)
                when (helper!!.itemView.tag as Int) {
                    0 -> {
                        params.setMargins(AppTools.dp2px(context, 15), 0, 0, 0)
                    }
                    otherData.size - 1 -> {
                        params.setMargins(AppTools.dp2px(context, 7), 0, AppTools.dp2px(context, 15), 0)
                    }
                    else -> {
                        params.setMargins(AppTools.dp2px(context, 7), 0, 0, 0)
                    }
                }
                helper.getView(R.id.cardLayout).layoutParams = params
                Glide.with(context)
                        .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(item!!.album_cover_url)))
                        .load(item.album_cover_url)
                        .into(helper.getImageView(R.id.iv_album_cover))
                helper.getTextView(R.id.tv_album_name).text = item.album_name
            }
        }
        mView!!.recyclerView_other.adapter = otherAdapter
        if (loginBean.user_id == userId) {//自己
            request(0)
            request(1)
            request(2)
        } else {
            request(4)
            isFriendLoading = true
        }
    }

    /**
     * 解析可能本地存在移行换位的数据
     * 检测本地是否有缓存数据
     */
    private fun parseEmptyData() {
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val cachePublic = PreferenceTools.getObj(activity, IConstant.SEASON_CACHE_PUBLIC + loginBean.user_id, VoiceAlbumData::class.java)
        val cacheFriend = PreferenceTools.getObj(activity, IConstant.SEASON_CACHE_Friend + loginBean.user_id, VoiceAlbumData::class.java)
        val cachePrivacy = PreferenceTools.getObj(activity, IConstant.SEASON_CACHE_PRIVACY + loginBean.user_id, VoiceAlbumData::class.java)
        if (cachePublic != null && cachePublic.data.size > 0) {
            publicData.addAll(cachePublic.data)
        } else {
            parseOpen(loginBean.user_id)
        }
        if (cacheFriend != null && cacheFriend.data.size > 0) {
            friendData.addAll(cacheFriend.data)
        } else {
            parseFriend(loginBean.user_id)
        }
        if (cachePrivacy != null && cachePrivacy.data.size > 0) {
            privacyData.addAll(cachePrivacy.data)
        } else {
            parsePrivacy(loginBean.user_id)
        }
    }

    private fun parseOpen(userId: String) {
        if (publicData.size > 0) {
            publicData.clear()
        }
        isClearPublic = false
        val seasonPublic = SPUtils.getString(activity, IConstant.SEASON_PUBLIC + userId, "1,2,3")
        for (index in seasonPublic.split(",")) {
            val bean = VoiceAlbumData.AlbumDataBean()
//            bean.album_cover_url = when (index) {
//                "1" -> R.mipmap.icon_open_album_1_copy
//                "2" -> R.mipmap.icon_open_album_2_copy
//                else -> R.mipmap.icon_open_album_3_copy
//            }
            bean.id = index
            bean.album_name = when (index) {
                "1" -> "把心情"
                "2" -> resources.getString(R.string.string_default_empty_season_1)
                else -> resources.getString(R.string.string_default_empty_season_2)
            }
            publicData.add(bean)
        }
    }

    private fun parseFriend(userId: String) {
        if (friendData.size > 0) {
            friendData.clear()
        }
        isClearFriend = false
        val seasonFriend = SPUtils.getString(activity, IConstant.SEASON_FRIEND + userId, "1,2")
        for (index in seasonFriend.split(",")) {
            if (TextUtils.isEmpty(index)) {
                continue
            }
            val bean = VoiceAlbumData.AlbumDataBean()
//            bean.album_cover_url = when (index) {
//                "1" -> R.mipmap.icon_only_friends_album_1_copy
//                "2" -> R.mipmap.icon_only_friends_album_2_copy
//                else -> R.mipmap.icon_only_friends_album_3
//            }
            bean.id = index
            bean.album_name = when (index) {
                "1" -> resources.getString(R.string.string_default_empty_season_3)
                "2" -> resources.getString(R.string.string_default_empty_season_4)
                else -> "故事"
            }
            friendData.add(bean)
        }
    }

    private fun parsePrivacy(userId: String) {
        if (privacyData.size > 0) {
            privacyData.clear()
        }
        isClearPrivacy = false
        val seasonPrivacy = SPUtils.getString(activity, IConstant.SEASON_PRIVACY + userId, "1")
        for (index in seasonPrivacy.split(",")) {
            if (TextUtils.isEmpty(index)) {
                continue
            }
            val bean = VoiceAlbumData.AlbumDataBean()
//            bean.album_cover_url = when (index) {
//                "1" -> R.mipmap.icon_privacy_album_1_copy
//                "2" -> R.mipmap.icon_privacy_album_2
//                else -> R.mipmap.icon_privacy_album_3
//            }
            bean.id = index
            bean.album_name = when (index) {
                "1" -> resources.getString(R.string.string_default_empty_season_5)
                "2" -> resources.getString(R.string.string_default_empty_season_6)
                else -> resources.getString(R.string.string_default_empty_season_7)
            }
            privacyData.add(bean)
        }
    }

    private fun parseSortResult(array: ArrayList<VoiceAlbumData.AlbumDataBean>): String {
        var sortResult = ""
        for ((index, value) in array.withIndex()) {
            sortResult += value.id
            if (index != array.size - 1)
                sortResult += ","
        }
        return sortResult
    }

    override fun initEvent() {
        val helper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            private var endPosition = -1
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val position = viewHolder.layoutPosition
                if (publicAdapter.isFootView(position)) {
                    return makeMovementFlags(0, 0)
                }
                val dragFlags = /*ItemTouchHelper.UP or ItemTouchHelper.DOWN or*/ ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT //允许上下左右的拖动
                ObjectAnimator.ofFloat(viewHolder.itemView, "scaleX", 1f, 0.7f, 1f).setDuration(320).start()
                ObjectAnimator.ofFloat(viewHolder.itemView, "scaleY", 1f, 0.7f, 1f).setDuration(320).start()
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                // 真实的Position：通过ViewHolder拿到的position都需要减掉HeadView的数量。
                val fromPosition = viewHolder.layoutPosition
                if (publicAdapter.isFootView(fromPosition)) {
                    return false
                }
                val toPosition = target.layoutPosition
                if (publicAdapter.isFootView(toPosition)) {
                    return false
                }
                if (fromPosition < toPosition)
                    for (i in fromPosition until toPosition)
                        Collections.swap(publicData, i, i + 1)
                else
                    for (i in fromPosition downTo toPosition + 1)
                        Collections.swap(publicData, i, i - 1)
                endPosition = toPosition
                publicAdapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                /**
                 * 默认数据 添加到本地缓存， 如果是真实数据 ,则保存到后台
                 */
                publicAdapter.notifyDataSetChanged()
                if (!isClearPublic) {
                    SPUtils.setString(activity, IConstant.SEASON_PUBLIC + userId, parseSortResult(publicData))
                } else {
                    /**
                     * 修改当前操作的角标
                     */
                    if (endPosition != -1) {
                        updateLocation(publicData[endPosition], endPosition, if (endPosition != 0) publicData[endPosition - 1] else null)
                    }
                }
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (viewHolder != null) {
                    endPosition = -1
                }
            }

        })
        helper.attachToRecyclerView(mView!!.recycler_public)
        val helperFriend = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            private var endPosition = -1
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val position = viewHolder.layoutPosition
                if (friendAdapter.isFootView(position)) {
                    return makeMovementFlags(0, 0)
                }
                val dragFlags =/* ItemTouchHelper.UP or ItemTouchHelper.DOWN or*/ ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT //允许上下左右的拖动
                ObjectAnimator.ofFloat(viewHolder.itemView, "scaleX", 1f, 0.7f, 1f).setDuration(320).start()
                ObjectAnimator.ofFloat(viewHolder.itemView, "scaleY", 1f, 0.7f, 1f).setDuration(320).start()
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                // 真实的Position：通过ViewHolder拿到的position都需要减掉HeadView的数量。
                val fromPosition = viewHolder.layoutPosition
                if (friendAdapter.isFootView(fromPosition)) {
                    return false
                }
                val toPosition = target.layoutPosition
                if (friendAdapter.isFootView(toPosition)) {
                    return false
                }
                if (fromPosition < toPosition)
                    for (i in fromPosition until toPosition)
                        Collections.swap(friendData, i, i + 1)
                else
                    for (i in fromPosition downTo toPosition + 1)
                        Collections.swap(friendData, i, i - 1)
                endPosition = toPosition
                friendAdapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                if (!isClearFriend) {
                    SPUtils.setString(activity, IConstant.SEASON_FRIEND + userId, parseSortResult(friendData))
                } else {
                    /**
                     * 修改当前操作的角标
                     */
                    if (endPosition != -1) {
                        updateLocation(friendData[endPosition], endPosition, if (endPosition != 0) friendData[endPosition - 1] else null)
                    }
                }
                friendAdapter.notifyDataSetChanged()
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (viewHolder != null) {
                    endPosition = -1
                }
            }
        })
        helperFriend.attachToRecyclerView(mView!!.recycler_friends)
        val helperPrivacy = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            private var endPosition = -1
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val position = viewHolder.layoutPosition
                if (privacyAdapter.isFootView(position)) {
                    return makeMovementFlags(0, 0)
                }
                val dragFlags = /*ItemTouchHelper.UP or ItemTouchHelper.DOWN or*/ ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT //允许上下左右的拖动
                ObjectAnimator.ofFloat(viewHolder.itemView, "scaleX", 1f, 0.7f, 1f).setDuration(320).start()
                ObjectAnimator.ofFloat(viewHolder.itemView, "scaleY", 1f, 0.7f, 1f).setDuration(320).start()
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPosition = viewHolder.layoutPosition
                if (privacyAdapter.isFootView(fromPosition)) {
                    return false
                }
                val toPosition = target.layoutPosition
                if (privacyAdapter.isFootView(toPosition)) {
                    return false
                }
                if (fromPosition < toPosition)
                    for (i in fromPosition until toPosition)
                        Collections.swap(privacyData, i, i + 1)
                else
                    for (i in fromPosition downTo toPosition + 1)
                        Collections.swap(privacyData, i, i - 1)
                endPosition = toPosition
                privacyAdapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                if (!isClearPrivacy)
                    SPUtils.setString(activity, IConstant.SEASON_PRIVACY + userId, parseSortResult(privacyData))
                else {
                    /**
                     * 修改当前操作的角标
                     */
                    if (endPosition != -1) {
                        updateLocation(privacyData[endPosition], endPosition, if (endPosition != 0) privacyData[endPosition - 1] else null)
                    }
                }
                privacyAdapter.notifyDataSetChanged()
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (viewHolder != null) {
                    endPosition = -1
                }
            }
        })
        helperPrivacy.attachToRecyclerView(mView!!.recycler_privacy)
        mView!!.tv_add_album.setOnClickListener {
            startActivity(Intent(activity, AddAlbumActivity::class.java))
        }
        mView!!.relative_preview.setOnClickListener {
            DialogCheckSeasonAlbum(activity!!).setOnClickListener(View.OnClickListener {
                startActivity(Intent(activity, PreViewSeasonActivity::class.java).putExtra("isFriend", when (it.id) {
                    R.id.tv_stranger ->
                        false
                    else ->
                        true
                }))
                activity?.overridePendingTransition(R.anim.act_enter_alpha, 0)
            }).show()

        }
        publicAdapter.setOnItemClickListener { _, position ->
            if (isClearPublic || !TextUtils.isEmpty(publicData[position].user_id)) {//默認文案 创建新的专辑
                startActivity(Intent(activity, SeasonAlbumDetailsActivity::class.java).putExtra("data", publicData[position]))
            } else {
                startActivity(Intent(activity, AddAlbumActivity::class.java).putExtra("privacy", "1"))
            }
        }
        friendAdapter.setOnItemClickListener { _, position ->
            if (isClearFriend || !TextUtils.isEmpty(friendData[position].user_id)) {//默認文案 创建新的专辑
                startActivity(Intent(activity, SeasonAlbumDetailsActivity::class.java).putExtra("data", friendData[position]))
            } else {
                startActivity(Intent(activity, AddAlbumActivity::class.java).putExtra("privacy", "2"))
            }
        }
        privacyAdapter.setOnItemClickListener { _, position ->
            if (isClearPrivacy || !TextUtils.isEmpty(privacyData[position].user_id)) {//默認文案 创建新的专辑
                startActivity(Intent(activity, SeasonAlbumDetailsActivity::class.java).putExtra("data", privacyData[position]))
            } else {
                startActivity(Intent(activity, AddAlbumActivity::class.java).putExtra("privacy", "3"))
            }
        }
        otherAdapter.setOnItemClickListener { _, position ->
            startActivity(Intent(activity, SeasonAlbumDetailsActivity::class.java).putExtra("data", otherData[position]))
        }
        publicAdapter.setOnLoadListener {
            request(0)
        }
        friendAdapter.setOnLoadListener {
            request(1)
        }
        privacyAdapter.setOnLoadListener {
            request(2)
        }
        mView!!.tv_Friend.setOnClickListener {
            if (!it.isSelected) {
                mView!!.tv_Friend.isSelected = true
                addFriends()
            }
        }
    }

    @Volatile
    private var isClearPublic = false
    @Volatile
    private var isClearFriend = false
    @Volatile
    private var isClearPrivacy = false

    private fun addFriends() {
        val formBody = FormBody.Builder()
                .add("toUserId", userId)
                .build()
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(activity, "users/${loginBean.user_id}/friendslog", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as BaseRepData).code == 0) {
                    mView!!.tv_Friend.isSelected = true
                    mView!!.tv_Friend.text = resources.getString(R.string.string_pending)
                    EventBus.getDefault().post(INotifyFriendStatus(1, userId))
                    if (SPUtils.getInt(activity, IConstant.TOTALLENGTH + loginBean.user_id, 0) == 0) {
                        activity?.let { DialogCreateVoice(it).show() }
                    } else if (SPUtils.getBoolean(activity, IConstant.STRANGEVIEW + loginBean.user_id, false)) {
                        activity?.let {
                            DialogUserSet(it).setOnClickListener(View.OnClickListener {
                                userId?.let { it1 -> addWhiteBlack(it1) }
                            }).show()
                        }
                    }
                } else {
                    if (SPUtils.getLong(activity, IConstant.USERLIMITTIME, 0L) > System.currentTimeMillis() / 1000) {
                        DialogLimitTalk(activity!!).show()
                    } else {
                        showToast(result.msg)
                    }
                    mView!!.tv_Friend.isSelected = false
                }
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showContent()
            }
        })
    }

    private fun updateLocation(bean: VoiceAlbumData.AlbumDataBean, index: Int, preBean: VoiceAlbumData.AlbumDataBean?) {
        mView!!.transLayout.showProgress()
        OkClientHelper.patch(activity, "user/${bean.user_id}/voiceAlbum/${bean.id}", FormBody.Builder().add("preAlbumId", if (index == 0) "0" else preBean?.id).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                mView!!.transLayout.showContent()
                if (result.code == 0) {

                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                mView!!.transLayout.showContent()
            }
        }, "V3.8")
    }

    private var isFriendLoading = false

    @SuppressLint("SetTextI18n")
    override fun request(flag: Int) {
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        when (flag) {
            0 -> {
                OkClientHelper.get(activity, "user/${loginBean.user_id}/voiceAlbum?orderByField=albumSort&albumType=1&orderByValue=$lastIdPublic", VoiceAlbumData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VoiceAlbumData
                        publicAdapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        if (result.code == 0 && result.data != null) {
                            if (!isClearPublic) {
                                isClearPublic = true
                                publicData.clear()
                            }
                            if (TextUtils.isEmpty(lastIdPublic)) {
                                PreferenceTools.saveObj(activity, IConstant.SEASON_CACHE_PUBLIC + userId, result)
                                publicData.addAll(result.data)
                                publicAdapter.notifyDataSetChanged()
                            } else
                                for (bean in result.data) {
                                    publicData.add(bean)
                                    publicAdapter.notifyItemInserted(publicAdapter.itemCount - 1)
                                }
                            if (result.data != null && result.data.size > 0) {
                                if (result.data.size >= 10) {
                                    publicAdapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                                }
                                lastIdPublic = result.data[result.data.size - 1].album_sort.toString()
                            }
                        } else {
                            if (TextUtils.isEmpty(lastIdPublic)) {
                                if (loginBean.user_id == userId) {
                                    PreferenceTools.clear(activity, IConstant.SEASON_CACHE_PUBLIC + userId)
                                    parseOpen(loginBean.user_id)
                                    publicAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                        publicAdapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }, "V3.8")
            }
            1 -> {
                OkClientHelper.get(activity, "user/${loginBean.user_id}/voiceAlbum?orderByField=albumSort&albumType=2&orderByValue=$lastIdFriend", VoiceAlbumData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VoiceAlbumData
                        friendAdapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        if (result.code == 0 && result.data != null) {
                            if (!isClearFriend) {
                                isClearFriend = true
                                friendData.clear()
                            }
                            if (TextUtils.isEmpty(lastIdFriend)) {
                                PreferenceTools.saveObj(activity, IConstant.SEASON_CACHE_Friend + userId, result)
                                friendData.addAll(result.data)
                                friendAdapter.notifyDataSetChanged()
                            } else
                                for (bean in result.data) {
                                    friendData.add(bean)
                                    friendAdapter.notifyItemInserted(friendAdapter.itemCount - 1)
                                }
                            if (result.data != null && result.data.size > 0) {
                                if (result.data.size >= 10) {
                                    friendAdapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                                }
                                lastIdFriend = result.data[result.data.size - 1].album_sort.toString()
                            }
                        } else {
                            if (TextUtils.isEmpty(lastIdFriend)) {
                                if (loginBean.user_id == userId) {
                                    parseFriend(loginBean.user_id)
                                    friendAdapter.notifyDataSetChanged()
                                    PreferenceTools.clear(activity, IConstant.SEASON_CACHE_Friend + userId)
                                } else {

                                }
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                        friendAdapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                }, "V3.8")
            }
            2 -> {
                OkClientHelper.get(activity, "user/${loginBean.user_id}/voiceAlbum?orderByField=albumSort&albumType=3&orderByValue=$lastIdPrivacy", VoiceAlbumData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VoiceAlbumData
                        privacyAdapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                        if (result.code == 0 && result.data != null) {
                            if (!isClearPrivacy) {
                                isClearPrivacy = true
                                privacyData.clear()
                            }
                            if (TextUtils.isEmpty(lastIdPrivacy)) {
                                PreferenceTools.saveObj(activity, IConstant.SEASON_CACHE_PRIVACY + userId, result)
                                privacyData.addAll(result.data)
                                privacyAdapter.notifyDataSetChanged()
                            } else
                                for (bean in result.data) {
                                    privacyData.add(bean)
                                    privacyAdapter.notifyItemInserted(privacyAdapter.itemCount - 1)
                                }
                            if (result.data != null && result.data.size > 0) {
                                if (result.data.size >= 10) {
                                    privacyAdapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                                }
                                lastIdPrivacy = result.data[result.data.size - 1].album_sort.toString()
                            }
                        } else {
                            if (TextUtils.isEmpty(lastIdPrivacy)) {
                                if (loginBean.user_id == userId) {
                                    PreferenceTools.clear(activity, IConstant.SEASON_CACHE_PRIVACY + userId)
                                    parsePrivacy(loginBean.user_id)
                                    privacyAdapter.notifyDataSetChanged()
                                } else {

                                }
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                        privacyAdapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                    }
                }, "V3.8")
            }
            3 -> {
                OkClientHelper.get(activity, "user/$userId/voiceAlbum?orderByField=albumSort&orderByValue=$lastOther&albumType=2", VoiceAlbumData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VoiceAlbumData
                        if (result.code == 0 && result.data != null) {
                            if (TextUtils.isEmpty(lastOther)) {
                                otherData.clear()
                                for (bean in result.data) {
                                    if (bean.voice_num == 0) {
                                        continue
                                    }
                                    otherData.add(bean)
                                }
                                otherAdapter.notifyDataSetChanged()
                            } else
                                for (bean in result.data) {
                                    if (bean.voice_num == 0) {
                                        continue
                                    }
                                    otherData.add(bean)
                                    otherAdapter.notifyItemInserted(otherAdapter.itemCount - 1)
                                }
                            lastOther = result.data[result.data.size - 1].album_sort.toString()
                            if (result.data != null && result.data.size > 0) {
                                if (result.data.size >= 10) {
                                    request(3)
                                } else {
                                    lastOther = ""
                                    request(5)
                                }
                            }
                            mView!!.tv_other_count.text = "${otherData.size}${resources.getString(R.string.string_preview_album_count)}"
                        } else {
                            lastOther = ""
                            request(5)
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V3.8")
            }
            4 -> {
                OkClientHelper.get(activity, "relations/$userId", RelationData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        /**
                         * 查询用户关系, 是否是好友
                         */
                        result as RelationData
                        if (result.code == 0) {
                            when {
                                result.data.friend_status == 2 -> {//好友
                                    mView!!.relative_stranger.visibility = View.GONE
                                }
                                result.data.friend_status == 1 -> {//待验证
                                    mView!!.relative_stranger.visibility = View.VISIBLE
                                    mView!!.tv_Friend.isSelected = true
                                    mView!!.tv_Friend.text = resources.getString(R.string.string_pending)
                                }
                                result.data.friend_status == 0 -> {
                                    mView!!.relative_stranger.visibility = View.VISIBLE

                                }
                            }
                            if (result.data.friend_status == 2) {
                                request(3)
                            } else {
                                request(5)
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            5 -> {
                OkClientHelper.get(activity, "user/$userId/voiceAlbum?orderByField=albumSort&orderByValue=$lastOther&albumType=1", VoiceAlbumData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VoiceAlbumData
                        if (result.code == 0 && result.data != null) {
                            for (bean in result.data) {
                                if (bean.voice_num == 0) {
                                    continue
                                }
                                otherData.add(bean)
                                otherAdapter.notifyItemInserted(otherAdapter.itemCount - 1)
                            }
                            lastOther = result.data[result.data.size - 1].album_sort.toString()
                            if (result.data != null && result.data.size > 0) {
                                if (result.data.size >= 10) {
                                    request(5)
                                } else {
                                    isFriendLoading = false
                                }
                            }
                            mView!!.tv_other_count.text = "${otherData.size}${resources.getString(R.string.string_preview_album_count)}"
                        } else {
                            isFriendLoading = false
                        }
                        if (otherData.size == 0) {
                            mView!!.layout_empty.visibility = View.VISIBLE
                        } else {
                            mView!!.layout_empty.visibility = View.GONE
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V3.8")
            }
        }
    }

    //请求最新的一条数据, 不做数据的安全校验
    private fun queryFirstData() {
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(activity, "user/${loginBean.user_id}/voiceAlbum?orderByField=albumSort&orderByValue=", VoiceAlbumData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as VoiceAlbumData
                if (result.code == 0 && result.data.size > 0) {
                    when (result.data[0].album_type) {
                        1 -> {
                            if (!isClearPublic) {
                                publicData.clear()
                                isClearPublic = true
                            }
                            publicData.add(0, result.data[0])
                            publicAdapter.notifyDataSetChanged()
                        }
                        2 -> {
                            if (!isClearFriend) {
                                friendData.clear()
                                isClearFriend = true
                            }
                            friendData.add(0, result.data[0])
                            friendAdapter.notifyDataSetChanged()
                        }
                        3 -> {
                            if (!isClearPrivacy) {
                                privacyData.clear()
                                isClearPrivacy = true
                            }
                            privacyData.add(0, result.data[0])
                            privacyAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V3.8")

    }

    private fun getBean(array: ArrayList<VoiceAlbumData.AlbumDataBean>, id: String): VoiceAlbumData.AlbumDataBean? {
        var tempBean: VoiceAlbumData.AlbumDataBean? = null
        for (bean in array) {
            if (bean.id == id) {
                tempBean = bean
                break
            }
        }
        return tempBean
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateEvent(event: IUpdateAlbumEvent) {
        if (event.type == 1) {
            when (event.originSort) {
                1 -> {
                    getBean(publicData, event.id)?.let {
                        publicData.remove(it)
                        publicAdapter.notifyDataSetChanged()
                        if (publicData.size == 0) {
                            parseOpen(PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id)
                        }
                    }
                }
                2 -> {
                    getBean(friendData, event.id)?.let {
                        friendData.remove(it)
                        friendAdapter.notifyDataSetChanged()
                        if (friendData.size == 0) {
                            parseFriend(PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id)
                        }
                    }
                }
                3 -> {
                    getBean(privacyData, event.id)?.let {
                        privacyData.remove(it)
                        privacyAdapter.notifyDataSetChanged()
                        if (privacyData.size == 0) {
                            parsePrivacy(PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id)
                        }
                    }
                }
            }
        } else if (event.type == 2) {
            if (event.originSort != event.visibleType) {//操作为不同类型, 改变item位置
                getBean(when (event.originSort) {
                    1 -> publicData
                    2 -> friendData
                    else -> privacyData
                }, event.id)?.let {
                    if (!TextUtils.isEmpty(event.cover)) {
                        it.album_cover_url = event.cover
                    }
                    if (!TextUtils.isEmpty(event.name)) {
                        it.album_name = event.name
                    }
                    when (event.originSort) {
                        1 -> {
                            if (publicData.contains(it)) {
                                publicData.remove(it)
                                publicAdapter.notifyDataSetChanged()
                                if (publicData.size == 0) {
                                    parseOpen(PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id)
                                }
                            }
                        }
                        2 -> {
                            if (friendData.contains(it)) {
                                friendData.remove(it)
                                friendAdapter.notifyDataSetChanged()
                                if (friendData.size == 0) {
                                    parseFriend(PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id)
                                }
                            }
                        }
                        else -> {
                            if (privacyData.contains(it)) {
                                privacyData.remove(it)
                                privacyAdapter.notifyDataSetChanged()
                                if (privacyData.size == 0) {
                                    parsePrivacy(PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java).user_id)
                                }
                            }
                        }
                    }
                    when (event.visibleType) {
                        1 -> {
                            if (!isClearPublic) {
                                isClearPublic = true
                                publicData.clear()
                            }
                            if (!publicData.contains(it)) {
                                it.album_type = 1
                                publicData.add(0, it)
                                publicAdapter.notifyDataSetChanged()
                                updateLocation(it, 0, null)
                            }
                        }
                        2 -> {
                            if (!isClearFriend) {
                                isClearFriend = true
                                friendData.clear()
                            }
                            if (!friendData.contains(it)) {
                                it.album_type = 2
                                friendData.add(0, it)
                                friendAdapter.notifyDataSetChanged()
                                updateLocation(it, 0, null)
                            }
                        }
                        else -> {
                            if (!isClearPrivacy) {
                                isClearPrivacy = true
                                privacyData.clear()
                            }
                            if (!privacyData.contains(it)) {
                                it.album_type = 3
                                privacyData.add(0, it)
                                privacyAdapter.notifyDataSetChanged()
                                updateLocation(it, 0, null)
                            }
                        }
                    }
                }
            } else {//原类型中操作
                getBean(when (event.originSort) {
                    1 -> publicData
                    2 -> friendData
                    else -> privacyData
                }, event.id)?.let {
                    if (!TextUtils.isEmpty(event.cover)) {
                        it.album_cover_url = event.cover
                    }
                    if (!TextUtils.isEmpty(event.name)) {
                        it.album_name = event.name
                    }
                    when (event.originSort) {
                        1 -> publicAdapter.notifyDataSetChanged()
                        2 -> friendAdapter.notifyDataSetChanged()
                        else -> privacyAdapter.notifyDataSetChanged()
                    }

                }
            }
        } else if (event.type == 3) {
            queryFirstData()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun friendsChange(event: INotifyFriendStatus) {
        if (event.status == 1) {
            try {
                mView!!.tv_Friend.isSelected = true
                mView!!.tv_Friend.text = resources.getString(R.string.string_pending)
            } catch (e: Exception) {

            }
        } else if (event.status == 2 || event.status == 0) {
            try {
                if (event.userId == userId) {
                    lastOther = ""
                    mView!!.relative_stranger.visibility = View.GONE
                    if (!isFriendLoading)
                        request(3)
                }
            } catch (e: Exception) {

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}