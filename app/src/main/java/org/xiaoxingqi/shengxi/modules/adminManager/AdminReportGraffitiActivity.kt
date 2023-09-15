package org.xiaoxingqi.shengxi.modules.adminManager

import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.activity_report_graffiti.*
import kotlinx.android.synthetic.main.item_user_canvas.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.AdminReportGraffitiData
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.PaintData
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.SPUtils
import org.xiaoxingqi.shengxi.utils.TimeUtils
import skin.support.SkinCompatManager

/**
 * 举报的涂鸦列表
 */
class AdminReportGraffitiActivity : BaseAct() {
    private var graffitiId = 0
    private lateinit var graffitiBean: PaintData.PaintBean

    override fun getLayoutId(): Int {
        return R.layout.activity_report_graffiti
    }

    override fun initView() {
        linearOperate.visibility = View.GONE
        tvCollection.visibility = View.GONE
    }

    override fun initData() {
        graffitiId = intent.getIntExtra("id", 0)
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            finish()
        }
        tvDelete.setOnClickListener {
            if (graffitiBean.graffiti_status == 1) {//删除
                delete()
            } else if (graffitiBean.graffiti_status == 4) {//恢复
                restoreGraffiti()
            }
        }
        roundImg.setOnClickListener {
            try {
                startActivity(Intent(this, UserDetailsActivity::class.java).putExtra("id", graffitiBean.user_id))
            } catch (e: Exception) {
            }
        }
    }

    override fun request(flag: Int) {
        transLayout.showProgress()
        OkClientHelper.get(this, "graffitis/$graffitiId", AdminReportGraffitiData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as AdminReportGraffitiData
                if (result.code == 0) {
                    graffitiBean = result.data
                    Glide.with(square_img)
                            .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(result.data.graffiti_url))
                                    .error(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                                    .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night))
                            .asBitmap()
                            .load(result.data.graffiti_url)
                            .into(square_img)
                    glideUtil.loadGlide(result.data.avatar_url, roundImg, R.mipmap.icon_user_default, glideUtil.getLastModified(result.data.avatar_url))
                    tvTime.text = TimeUtils.getInstance().paserFriends(this@AdminReportGraffitiActivity, result.data.created_at)
                    tv_UserName.text = result.data.nick_name
                    tv_Action.text = if (TextUtils.isEmpty(result.data.topic_name)) "" else "#${result.data.topic_name}#"
                    tvDelete.text = when (result.data.graffiti_status) {
                        1 -> "删除"
                        2 -> "用户删除"
                        3 -> "作品整体删除"
                        4 -> "系统删除(恢复)"
                        else -> "删除"
                    }
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V4.1")
    }

    private fun delete() {
        transLayout.showProgress()
        OkClientHelper.delete(this, "admin/graffitis/$graffitiId",
                FormBody.Builder().add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    graffitiBean.graffiti_status = 4
                    tvDelete.text = "系统删除(恢复)"
                    showToast("操作成功")
                } else
                    showToast(result.msg)
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    private fun restoreGraffiti() {
        transLayout.showProgress()
        OkClientHelper.patch(this, "admin/graffitis/$graffitiId",
                FormBody.Builder().add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).add("graffitiStatus", "1").build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    graffitiBean.graffiti_status = 1
                    tvDelete.text = "删除"
                    showToast("操作成功")
                } else
                    showToast(result.msg)
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }
}