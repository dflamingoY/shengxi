package org.xiaoxingqi.shengxi.modules.user.frag

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.TextUtils
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
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.IAlbumUpdate
import org.xiaoxingqi.shengxi.impl.IUpdateTalkAlbumEvent
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.SeasonAlbumDetails
import org.xiaoxingqi.shengxi.model.VoiceAlbumData
import org.xiaoxingqi.shengxi.modules.listen.alarm.loop
import org.xiaoxingqi.shengxi.modules.user.frag.talkAlbum.TalkCreateAlbumActivity
import java.util.*
import kotlin.collections.ArrayList

class TalkAlbumActivity : BaseAct() {
    private lateinit var adapter: QuickAdapter<VoiceAlbumData.AlbumDataBean>
    private val mData by lazy { ArrayList<VoiceAlbumData.AlbumDataBean>() }
    private var lastId: String? = ""
    override fun getLayoutId(): Int {
        return R.layout.activity_season_album
    }

    override fun initView() {
        ivAdd.visibility = View.VISIBLE
        relative_preview.visibility = View.GONE
    }

    override fun initData() {
        adapter = object : QuickAdapter<VoiceAlbumData.AlbumDataBean>(this, R.layout.layout_album_cover_view, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: VoiceAlbumData.AlbumDataBean?) {
                Glide.with(context)
                        .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(item!!.album_cover_url)))
                        .load(item.album_cover_url)
                        .into(helper!!.getImageView(R.id.ivAlbumCover))
                helper.getView(R.id.ivAlbumType).visibility = View.GONE
                helper.getTextView(R.id.tvAlbumLength).text = "${item.dialog_num} 条"
                helper.getTextView(R.id.tvAlbumTitle).text = item.album_name
            }
        }
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter
        request(0)
    }

    override fun initEvent() {
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
        ivAdd.setOnClickListener {
            startActivity<TalkCreateAlbumActivity>()
        }
        btn_Back.setOnClickListener { finish() }
        adapter.setOnItemClickListener { _, position ->
            startActivity<TalkAlbumDetailsActivity>("data" to mData[position])
        }
    }

    private fun updateLocation(bean: VoiceAlbumData.AlbumDataBean, index: Int, preBean: VoiceAlbumData.AlbumDataBean?) {
        transLayout.showProgress()
        OkClientHelper.patch(this, "dialogAlbums/${bean.id}", FormBody.Builder().add("preAlbumId", if (index == 0) "0" else preBean?.id).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                transLayout.showContent()
                if (result.code != 0)
                    showToast(result.msg)
                else {
                    //修改成功
                    EventBus.getDefault().post(IAlbumUpdate(1))
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V4.3")
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "dialogAlbums?orderByField=albumSort&orderByValue=$lastId", VoiceAlbumData::class.java, object : OkResponse {
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
        }, "V4.3")
    }

    /**
     * 查询新的专辑详情
     */
    private fun query(id: String) {
        transLayout.showProgress()
        OkClientHelper.get(this, "dialogAlbums/$id", SeasonAlbumDetails::class.java, object : OkResponse {
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
        }, "V4.3")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun albumEvent(event: IUpdateTalkAlbumEvent) {
        if (event.type == 1) {
            mData.loop {
                it.id == event.id
            }?.let {
                mData.remove(it)
                adapter.notifyDataSetChanged()
            }
        } else if (event.type == 2) {
            mData.loop {
                it.id == event.id
            }?.let {
                if (!TextUtils.isEmpty(event.name)) {
                    it.album_name = event.name
                }
                if (!TextUtils.isEmpty(event.cover)) {
                    it.album_cover_url = event.cover
                }
                adapter.notifyDataSetChanged()
            }
        } else if (event.type == 3) {
            query(event.id)
        }
    }


}