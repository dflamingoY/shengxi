package org.xiaoxingqi.shengxi.modules.publicmoudle

import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_add_talk_album.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseDialogAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.OnTalkAddAlbumEvent
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.VoiceAlbumData
import org.xiaoxingqi.shengxi.modules.user.frag.talkAlbum.TalkCreateAlbumActivity

class DialogAddTalkAlbumActivity : BaseDialogAct() {
    companion object {
        var instances: DialogAddTalkAlbumActivity? = null
    }

    private var lastId = ""
    private lateinit var adapter: QuickAdapter<VoiceAlbumData.AlbumDataBean>
    private val mData by lazy { ArrayList<VoiceAlbumData.AlbumDataBean>() }
    private var talkId: String? = null
    private var originalAlbumId: String? = null//数据库中的id
    private var albumId: String? = null//专辑的id
    override fun getLayoutId(): Int {
        return R.layout.activity_add_talk_album
    }

    override fun initView() {
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.BOTTOM)
        recycler_dialog.layoutManager = LinearLayoutManager(this)
    }

    override fun initData() {
        instances = this
        talkId = intent.getStringExtra("talkId")
        originalAlbumId = intent.getStringExtra("originalAlbumId")
        albumId = intent.getStringExtra("albumId")
        adapter = object : QuickAdapter<VoiceAlbumData.AlbumDataBean>(this, R.layout.item_dialog_album, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: VoiceAlbumData.AlbumDataBean?) {
                Glide.with(this@DialogAddTalkAlbumActivity)
                        .load(item!!.album_cover_url)
                        .into(helper!!.getImageView(R.id.img))
                helper.getTextView(R.id.tv_album_name).text = item.album_name
            }
        }
        recycler_dialog.adapter = adapter
        adapter.setLoadMoreEnable(recycler_dialog, recycler_dialog.layoutManager, LayoutInflater.from(this).inflate(R.layout.view_loadmore_little_height_grey, recycler_dialog, false))
        request(0)
    }

    override fun initEvent() {
        tv_cancel.setOnClickListener { finish() }
        adapter.setOnLoadListener {
            request(0)
        }
        adapter.setOnItemClickListener { _, position ->
            if (originalAlbumId == null) {
                addAlbum(mData[position].id)
            } else {
                if (mData[position].id == albumId) {
                    showToast("已在此专辑中")
                } else
                    removeAlbum(mData[position].id)
            }
        }
        tv_add_album.setOnClickListener {
            startActivity<TalkCreateAlbumActivity>("talkId" to talkId, "originalAlbumId" to originalAlbumId)
        }
    }

    private fun removeAlbum(id: String) {
        transLayout.showProgress()
        OkClientHelper.delete(this, "albumDialogs/${originalAlbumId}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }

            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    addAlbum(id)
                } else {
                    transLayout.showContent()
                }
            }
        }, "V4.3")
    }

    private fun addAlbum(id: String) {
        transLayout.showProgress()
        OkClientHelper.post(this, "albumDialogs/$id", FormBody.Builder().add("dialogId", talkId).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                transLayout.showContent()
                result as BaseRepData
                if (result.code == 0) {
                    showToast("已加入专辑")
                    if (originalAlbumId != null)
                        EventBus.getDefault().post(OnTalkAddAlbumEvent(originalAlbumId!!))
                    finish()
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V4.3")
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "dialogAlbums?orderByField=updatedAt&orderByValue=$lastId", VoiceAlbumData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                result as VoiceAlbumData
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
                if (result.code == 0 && result.data != null) {
                    mData.addAll(result.data)
                    adapter.notifyDataSetChanged()
                    if (result.data.size >= 10) {
                        adapter.setLoadStatue(BaseQuickAdapter.ELoadState.READY)
                    }
                    lastId = mData[mData.size - 1].updated_at.toString()
                }
                if (mData.size == 0) {
                    relative_empty.visibility = View.VISIBLE
                }
            }
        }, "V4.3")
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.operate_exit)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        instances = null
    }
}