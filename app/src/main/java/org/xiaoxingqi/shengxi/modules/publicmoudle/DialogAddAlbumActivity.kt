package org.xiaoxingqi.shengxi.modules.publicmoudle

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_add_album_dialog.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseDialogAct
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.BaseQuickAdapter
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.VoiceAlbumData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.user.frag.addAlbum.AddAlbumActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools

/**
 * 弹出添加到专辑的弹窗
 */
class DialogAddAlbumActivity : BaseDialogAct() {
    private var lastId = ""
    private lateinit var adapter: QuickAdapter<VoiceAlbumData.AlbumDataBean>
    private val mData by lazy { ArrayList<VoiceAlbumData.AlbumDataBean>() }
    private var voiceId: String? = null

    companion object {
        var instance: DialogAddAlbumActivity? = null
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_add_album_dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.BOTTOM)
//        setFinishOnTouchOutside(false)
        recycler_dialog.layoutManager = LinearLayoutManager(this)
    }

    override fun initData() {
        instance = this
        voiceId = intent.getStringExtra("voiceId")
        adapter = object : QuickAdapter<VoiceAlbumData.AlbumDataBean>(this, R.layout.item_dialog_album, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: VoiceAlbumData.AlbumDataBean?) {
                Glide.with(this@DialogAddAlbumActivity)
                        .load(item!!.album_cover_url)
                        .into(helper!!.getImageView(R.id.img))
                helper.getTextView(R.id.tv_album_name).text = item.album_name
                helper.getTextView(R.id.tv_album_type).text = when (item.album_type) {
                    1 -> resources.getString(R.string.string_privacy_album_5)
                    2 -> resources.getString(R.string.string_privacy_album_4)
                    else -> resources.getString(R.string.string_privacy_album_3)
                }
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
            addVoice2Album(mData[position].id)
        }
        tv_add_album.setOnClickListener {
            startActivity(Intent(this, AddAlbumActivity::class.java).putExtra("voiceId", voiceId))
        }
    }

    private fun addVoice2Album(id: String) {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "user/${loginBean.user_id}/albumVoice/$id", FormBody.Builder().add("voiceId", voiceId).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                transLayout.showContent()
                result as BaseRepData
                if (result.code == 0) {
                    showToast("已加入专辑")
                    finish()
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.8")
    }

    override fun request(flag: Int) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(this, "user/${loginBean.user_id}/voiceAlbum?orderByField=updatedAt&orderByValue=$lastId", VoiceAlbumData::class.java, object : OkResponse {
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

            override fun onFailure(any: Any?) {
                adapter.setLoadStatue(BaseQuickAdapter.ELoadState.EMPTY)
            }
        }, "V3.8")
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.operate_exit)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}