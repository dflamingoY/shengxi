package org.xiaoxingqi.shengxi.modules.user.frag.addAlbum

import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.activity_preview_season.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseNormalActivity
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.VoiceAlbumData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.DataHelper
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import kotlin.math.ceil

class PreViewSeasonActivity : BaseNormalActivity() {
    private lateinit var adapter: QuickAdapter<VoiceAlbumData.AlbumDataBean>
    private val mData by lazy { ArrayList<VoiceAlbumData.AlbumDataBean>() }
    private var lastId: String = ""

    override fun getLayoutId(): Int {
        return R.layout.activity_preview_season
    }

    override fun initView() {
        linear_top.setPadding(0, AppTools.getStatusBarHeight(this), 0, 0)
    }

    override fun initData() {
        val isFriend = intent.getBooleanExtra("isFriend", false)
        if (isFriend) {
            tv_hint.text = resources.getText(R.string.string_preview_season_top_2)
        }
        request(0)
        adapter = object : QuickAdapter<VoiceAlbumData.AlbumDataBean>(this, R.layout.layout_album_cover_view, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: VoiceAlbumData.AlbumDataBean?) {
                Glide.with(context)
                        .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(item!!.album_cover_url)))
                        .load(item.album_cover_url)
                        .into(helper!!.getImageView(R.id.ivAlbumCover))
                helper.getView(R.id.ivAlbumType).visibility = if (item.album_type != 1) View.VISIBLE else View.GONE
                helper.getTextView(R.id.tvAlbumLength).text = "${ceil(item.voice_total_len / 60f).toInt()} min"
                helper.getTextView(R.id.tvAlbumTitle).text = when (item.album_type) {
                    1 -> {
                        item.album_name
                    }
                    2 -> {
                        if (isFriend) {
                            helper.getView(R.id.relativeOtherType).visibility = View.GONE
                            item.album_name
                        } else
                            helper.getView(R.id.relativeOtherType).visibility = View.VISIBLE
                        resources.getString(R.string.string_visible_friends)
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
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter
    }

    override fun initEvent() {
        tv_out_preview.setOnClickListener {
            finish()
        }
    }

    override fun request(flag: Int) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        transLayout.showProgress()
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "user/${loginBean.user_id}/voiceAlbum?orderByField=albumSort&orderByValue=$lastId", VoiceAlbumData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VoiceAlbumData
                        if (result.code == 0 && result.data != null) {
                            for (bean in result.data) {
                                mData.add(bean)
                                adapter.notifyItemInserted(adapter.itemCount - 1)
                            }
                            lastId = result.data[result.data.size - 1].album_sort.toString()
                            if (result.data.size >= 10) {
                                request(0)
                            } else {
                                lastId = ""
                                transLayout.showContent()
                            }
                        } else {
                            lastId = ""
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showEmpty()
                    }
                }, "V3.8")
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.act_exit_alpha)
    }

    override fun onDestroy() {
        super.onDestroy()
        DataHelper.getInstance().clearData()
    }
}