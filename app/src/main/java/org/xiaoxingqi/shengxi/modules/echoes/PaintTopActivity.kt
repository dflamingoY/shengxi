package org.xiaoxingqi.shengxi.modules.echoes

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.activity_notify_paint_top.*
import kotlinx.android.synthetic.main.item_recommend_canvas.view.*
import okhttp3.FormBody
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogCancelEditVoice
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.modules.listen.soulCanvas.CanvasShowActivity
import org.xiaoxingqi.shengxi.modules.listen.soulCanvas.GraffitiListActivity
import org.xiaoxingqi.shengxi.utils.TimeUtils
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang
import skin.support.SkinCompatManager

class PaintTopActivity : BaseAct() {
    private lateinit var id: String
    private var artworkBean: PaintData.PaintBean? = null
    private var createAt: Int = 0
    private var topId = -1
    override fun getLayoutId(): Int {
        return R.layout.activity_notify_paint_top
    }

    override fun initView() {
        layoutPaint.relativeCollection.visibility = View.GONE
    }

    override fun initData() {
        id = intent.getStringExtra("id")
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        tvRevoke.setOnClickListener {
            if (topId != -1)
            //取消展示
                DialogCancelEditVoice(this).setTitle(resources.getString(R.string.string_36), resources.getString(R.string.string_37)).setOnClickListener(View.OnClickListener {
                    deleteTop()
                }).show()
        }
        layoutPaint.square_img.setOnClickListener {
            artworkBean?.let {
                startActivity<CanvasShowActivity>("artworkUrl" to it.artwork_url, "resourceId" to id, "isSend" to true)
                overridePendingTransition(R.anim.act_enter_alpha, 0)
            }
        }
        layoutPaint.relativeLikes.setOnClickListener {
            artworkBean?.let {
                if (it.like_type == 2) {
                    unLike()
                } else
                    like()
            }
        }
        layoutPaint.linearGraffiti.setOnClickListener {
            artworkBean?.let {
                if (it.chat_num > 0) {
                    startActivity<GraffitiListActivity>("resourceId" to id)
                } else {
                    startActivity<CanvasShowActivity>("artworkUrl" to it.artwork_url, "resourceId" to id, "isSend" to true)
                    overridePendingTransition(R.anim.act_enter_alpha, 0)
                }
            }
        }
    }

    private fun like() {
        transLayout.showProgress()
        OkClientHelper.post(this, "users/${artworkBean!!.user_id}/artworkLike/${id}", FormBody.Builder().add("likeType", "2").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                transLayout.showContent()
                result as BaseRepData
                if (result.code == 0) {
                    artworkBean!!.like_type = 2
                    SmallBang.attach2Window(this@PaintTopActivity).bang(layoutPaint.ivLikes, 60f, null)
                    layoutPaint.ivLikes.isSelected = true
                    artworkBean!!.publicly_like_num++
                    layoutPaint.tvLikes.text = "${artworkBean!!.publicly_like_num}"
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.6")
    }

    private fun unLike() {
        transLayout.showProgress()
        OkClientHelper.delete(this, "users/${artworkBean!!.user_id}/artworkLike/${id}", FormBody.Builder().add("likeType", "2").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    SmallBang.attach2Window(this@PaintTopActivity).bang(layoutPaint.ivLikes, 60f, null)
                    layoutPaint.ivLikes.isSelected = false
                    artworkBean!!.publicly_like_num--
                    layoutPaint.tvLikes.text = if (artworkBean!!.publicly_like_num == 0) "" else "${artworkBean!!.publicly_like_num}"
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

    private fun deleteTop() {
        OkClientHelper.delete(this, "artworkTop/$topId", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    showToast("已按照您的要求取消展示")
//                    tvRecommend.text = "作品曾在" + TimeUtils.parse(createAt) + "展示"
                    tvRecommend.text = "已按照您的要求取消展示"
                    tvRevoke.visibility = View.GONE
                }
            }
        }, "V4.3")
    }

    override fun request(flag: Int) {
        OkClientHelper.get(this, "artworks/$id/pick", IntegerRespData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as IntegerRespData
                result.data?.let {
                    if (result.data.top_status == 3) {
                        createAt = result.data.created_at
                        tvRecommend.text = "作品曾在" + TimeUtils.parse(result.data.created_at) + "展示"
                    } else if (result.data.top_status == 1) {
                        tvRecommend.text = resources.getString(R.string.string_35)
                        tvRevoke.visibility = View.VISIBLE
                    } else if (result.data.top_status == 2) {
                        tvRecommend.text = "已按照您的要求取消展示"
                    }
                }
            }
        }, "V4.3")
        OkClientHelper.get(this, "artworks/$id", AdminReportGraffitiData::class.java, object : OkResponse {
            @SuppressLint("SetTextI18n")
            override fun success(result: Any?) {
                result as AdminReportGraffitiData
                if (result.code == 0) {
                    result.data?.let {
                        artworkBean = it
                        topId = it.top_id
                        Glide.with(layoutPaint.square_img)
                                .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(it.artwork_url))
                                        .error(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                                        .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                                )
                                .asBitmap()
                                .load(it.artwork_url)
                                .into(layoutPaint.square_img)
                        layoutPaint.iv_user_type.visibility = if (it.identity_type == 0) View.GONE else View.VISIBLE
                        layoutPaint.iv_user_type.isSelected = it.identity_type == 1
                        layoutPaint.tvGraffiti.text = if (result.data.chat_num > 0) "${result.data.chat_num}" else ""
                        layoutPaint.ivLikes.isSelected = it.like_type != 0
                        layoutPaint.tvLikes.text = if (result.data.publicly_like_num > 0) "${result.data.publicly_like_num}" else ""
                    }
                } else {
                    transLayout.showOffline()
                }
            }

            override fun onFailure(any: Any?) {

            }
        }, "V4.2")
    }

}