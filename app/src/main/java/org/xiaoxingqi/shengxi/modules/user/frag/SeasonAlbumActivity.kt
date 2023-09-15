package org.xiaoxingqi.shengxi.modules.user.frag

import android.content.Intent
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.activity_season_album.*
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
import org.xiaoxingqi.shengxi.dialog.DialogCheckSeasonAlbum
import org.xiaoxingqi.shengxi.impl.IAlbumUpdate
import org.xiaoxingqi.shengxi.impl.IUpdateAlbumEvent
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.RelationData
import org.xiaoxingqi.shengxi.model.SeasonAlbumDetails
import org.xiaoxingqi.shengxi.model.VoiceAlbumData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.user.frag.addAlbum.AddAlbumActivity
import org.xiaoxingqi.shengxi.modules.user.frag.addAlbum.PreViewSeasonActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.math.floor

class SeasonAlbumActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<VoiceAlbumData.AlbumDataBean>
    private val mData by lazy { ArrayList<VoiceAlbumData.AlbumDataBean>() }
    private var lastId: String? = ""
    private var userId: String? = null
    private var isFriend = false
    private var isSelf = true
    override fun getLayoutId(): Int {
        return R.layout.activity_season_album
    }

    override fun initView() {

    }

    override fun initData() {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        isSelf = loginBean.user_id == userId
        userId = intent.getStringExtra("userId")
        adapter = object : QuickAdapter<VoiceAlbumData.AlbumDataBean>(this, R.layout.layout_album_cover_view, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: VoiceAlbumData.AlbumDataBean?) {
                if (!TextUtils.isEmpty(item!!.album_cover_url)) {
                    Glide.with(context)
                            .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(item.album_cover_url)))
                            .load(item.album_cover_url)
                            .into(helper!!.getImageView(R.id.ivAlbumCover))
                }
                helper!!.getView(R.id.ivAlbumType).visibility = if (item.album_type != 1) View.VISIBLE else View.GONE
                helper.getView(R.id.ivAlbumType).isSelected = item.album_type == 3
                helper.getTextView(R.id.tvAlbumLength).text = "${ceil(item.voice_total_len / 60f).toInt()} min"
                if (!TextUtils.isEmpty(item.album_name))
                    helper.getTextView(R.id.tvAlbumTitle).text = item.album_name
                if (!isSelf) {
                    helper.getTextView(R.id.tvAlbumTitle).text = when (item.album_type) {
                        2 -> {
                            if (isFriend) {
                                helper.getView(R.id.relativeOtherType).visibility = View.GONE
                                item.album_name
                            } else {
                                helper.getView(R.id.relativeOtherType).visibility = View.VISIBLE
                                resources.getString(R.string.string_visible_friends)
                            }
                        }
                        3 -> {
                            helper.getView(R.id.relativeOtherType).visibility = View.VISIBLE
                            resources.getString(R.string.string_default_empty_season_11)
                        }
                        else -> item.album_name
                    }
                    if (item.album_type == 2 || item.album_type == 3) {
                        helper.getView(R.id.iv_privacy_type).isSelected = item.album_type == 3
                    } else {
                        helper.getView(R.id.relativeOtherType).visibility = View.GONE
                    }
                }
            }
        }
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter
        transLayout.showProgress()
        if (loginBean.user_id == userId) {
            attachHelper()
            isSelf = true
            ivAdd.visibility = View.VISIBLE
            request(0)
        } else {
            tv_Title.text = "ta的${resources.getString(R.string.string_default_empty_season_2)}"
            relative_preview.visibility = View.GONE
            request(1)
        }
    }

    private fun attachHelper() {
        val helper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            private var endPosition = -1
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val position = viewHolder.layoutPosition
                if (adapter.isFootView(position)) {
                    return makeMovementFlags(0, 0)
                }
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT //允许上下左右的拖动
                ObjectAnimator.ofFloat(viewHolder.itemView, "scaleX", 1f, 0.7f, 1f).setDuration(320).start()
                ObjectAnimator.ofFloat(viewHolder.itemView, "scaleY", 1f, 0.7f, 1f).setDuration(320).start()
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                // 真实的Position：通过ViewHolder拿到的position都需要减掉HeadView的数量。
                val fromPosition = viewHolder.layoutPosition
                if (adapter.isFootView(fromPosition)) {
                    return false
                }
                val toPosition = target.layoutPosition
                if (adapter.isFootView(toPosition)) {
                    return false
                }
                if (fromPosition < toPosition)
                    for (i in fromPosition until toPosition)
                        Collections.swap(mData, i, i + 1)
                else
                    for (i in fromPosition downTo toPosition + 1)
                        Collections.swap(mData, i, i - 1)
                endPosition = toPosition
                adapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                adapter.notifyDataSetChanged()
                if (endPosition != -1) {
                    updateLocation(mData[endPosition], endPosition, if (endPosition != 0) mData[endPosition - 1] else null)
                }
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (viewHolder != null) {
                    endPosition = -1
                }
            }
        })
        helper.attachToRecyclerView(recyclerView)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        relative_preview.setOnClickListener {
            DialogCheckSeasonAlbum(this).setOnClickListener(View.OnClickListener {
                startActivity(Intent(this, PreViewSeasonActivity::class.java).putExtra("isFriend", when (it.id) {
                    R.id.tv_stranger ->
                        false
                    else ->
                        true
                }))
                overridePendingTransition(R.anim.act_enter_alpha, 0)
            }).show()
        }
        ivAdd.setOnClickListener {
            startActivity(Intent(this, AddAlbumActivity::class.java))
        }
        adapter.setOnItemClickListener { _, position ->
            if (isSelf || ((mData[position].album_type == 1 || (mData[position].album_type == 2 && isFriend)) && mData[position].voice_total_len > 0))
                startActivity(Intent(this, SeasonAlbumDetailsActivity::class.java).putExtra("data", mData[position]))
            else if (mData[position].voice_total_len == 0 && (mData[position].album_type == 1 || (mData[position].album_type == 2 && isFriend))) {
                showToast("专辑为空")
            }
        }
    }

    private fun updateLocation(bean: VoiceAlbumData.AlbumDataBean, index: Int, preBean: VoiceAlbumData.AlbumDataBean?) {
        transLayout.showProgress()
        OkClientHelper.patch(this, "user/${bean.user_id}/voiceAlbum/${bean.id}", FormBody.Builder().add("preAlbumId", if (index == 0) "0" else preBean?.id).add("allAlbum", "1").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                transLayout.showContent()
                if (result.code != 0)
                    showToast(result.msg)
                else {
                    //修改成功
                    EventBus.getDefault().post(IAlbumUpdate())
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.8")
    }

    override fun request(flag: Int) {
        when (flag) {
            0 ->
                OkClientHelper.get(this, "user/$userId/voiceAlbum?orderByField=albumSort&orderByValue=$lastId", VoiceAlbumData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VoiceAlbumData
                        if (result.code == 0 && result.data != null) {
                            for (bean in result.data) {
                                mData.add(bean)
                                adapter.notifyItemInserted(adapter.itemCount - 1)
                            }
                            lastId = result.data[result.data.size - 1].album_sort.toString()
                            if (result.data.size >= 10) {
                                request(0)//全部加载完
                            } else {
                                transLayout.showContent()
                            }
                        } else {
                            transLayout.showContent()
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showEmpty()
                    }
                }, "V3.8")
            1 -> {
                OkClientHelper.get(this, "relations/$userId", RelationData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as RelationData
                        if (result.code == 0) {
                            if (result.data.friend_status == 2) {
                                isFriend = true
                            }
                            request(2)
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                })
            }
            2 -> {//查询别人的列表
                OkClientHelper.get(this, "user/$userId/voiceAlbum?orderByField=albumSort&orderByValue=$lastId", VoiceAlbumData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VoiceAlbumData
                        if (result.code == 0) {
                            result.data?.let {
                                if (TextUtils.isEmpty(lastId)) {
                                    mData.clear()
                                    adapter.notifyDataSetChanged()
                                    mData.addAll(it)
                                    adapter.notifyDataSetChanged()
                                } else {
                                    it.forEach {
                                        mData.add(it)
                                        adapter.notifyItemInserted(adapter.itemCount - 1)
                                    }
                                }
                                lastId = result.data[result.data.size - 1].album_sort.toString()
                                if (result.data.size >= 10) {
                                    request(2)
                                }
                            }
                            if (result.data == null || result.data.size < 10) {
                                transLayout.showContent()
                            }
                        } else {
                            transLayout.showContent()
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showEmpty()
                    }
                }, "V4.2")
            }
        }
    }

    /**
     * 查询新的专辑详情
     */
    private fun query(id: String) {
        transLayout.showProgress()
        OkClientHelper.get(this, "user/$userId/voiceAlbum/$id", SeasonAlbumDetails::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as SeasonAlbumDetails
                if (result.code == 0 && result.data != null) {
                    mData.add(result.data)
                    adapter.notifyItemChanged(adapter.itemCount - 1)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showEmpty()
            }
        }, "V3.8")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAlbumUpdate(event: IUpdateAlbumEvent) {
        when (event.type) {
            1 -> {//删除专辑
                mData.loop {
                    event.id == it.id
                }?.let {
                    mData.remove(it)
                    adapter.notifyDataSetChanged()
                }
            }
            2 -> {//编辑状态
                mData.loop {
                    event.id == it.id
                }?.let {
                    //操作可见性, 封面,名字
                    if (!TextUtils.isEmpty(event.cover)) {
                        it.album_cover_url = event.cover
                    }
                    if (!TextUtils.isEmpty(event.name)) {
                        it.album_name = event.name
                    }
                    it.album_type = event.visibleType
                    adapter.notifyItemChanged(mData.indexOf(it))
                }
            }
            else -> {//新增item
                query(event.id)
                //添加最新的一条记录
            }
        }
    }
}