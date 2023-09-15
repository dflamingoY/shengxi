package org.xiaoxingqi.shengxi.modules.adminManager

import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_check_origin_paint.*
import kotlinx.android.synthetic.main.layout_admin_bottom_operator.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.AdminCheckOperatorEvent
import org.xiaoxingqi.shengxi.model.AdminCheckListData
import org.xiaoxingqi.shengxi.model.AdminReportGraffitiData
import org.xiaoxingqi.shengxi.model.PaintData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.modules.publicmoudle.ShowPicActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.SPUtils
import org.xiaoxingqi.shengxi.utils.TimeUtils

class CheckOriginPaintActivity : BaseAct() {
    private lateinit var data: AdminCheckListData.AdminCheckBean
    private var userInfo: UserInfoData.UserBean? = null
    private var artBean: PaintData.PaintBean? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_check_origin_paint
    }

    override fun initView() {
        tvReplace.text = "删除画"
    }

    override fun initData() {
        data = intent.getParcelableExtra("data")
        glideUtil.loadGlide(data.user.avatar_url, roundImg, 0, glideUtil.getLastModified(data.user.avatar_url))
        tv_UserName.text = data.user.nick_name
        Glide.with(this)
                .load(data.image_url)
                .into(ivContent)
        tvTime.text = TimeUtils.getInstance().paserFriends(this, data.created_at)
        request(0)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        tvHide.setOnClickListener {
            artBean?.let {
                transLayout.showProgress()
                operatorVoice("admin/artwork/${it.id}", FormBody.Builder().add("isHidden", if (it.hide_at > 0) "0" else "1")
                        .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build()
                ) { result ->
                    transLayout.showContent()
                    result?.let { _ ->
                        if (result.code == 0) {
                            it.hide_at = if (it.hide_at > 0) 0 else (System.currentTimeMillis() / 1000).toInt()
                            tvHide.text = if (it.hide_at > 0) "已隐藏" else "隐藏"
                        }
                    }
                }
            }
        }
        tvIgnore.setOnClickListener {
            transLayout.showProgress()
            operator("${data.id}", 2) {
                EventBus.getDefault().post(AdminCheckOperatorEvent(data.id))
                transLayout.showContent()
                tvIgnore.text = "已忽略"
            }
        }
        tvReplace.setOnClickListener {
            artBean?.let {
                transLayout.showProgress()
                if (it.artwork_status == 1) {
                    deleteVoice("admin/artwork/${it.id}", FormBody.Builder()
                            .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build()
                    ) { result ->
                        transLayout.showContent()
                        result?.let { _ ->
                            if (result.code == 0) {
                                it.artwork_status = if (it.artwork_status == 1) 3 else 1
                                tvReplace.text = "已删除"
                            }
                        }
                    }
                } else {
                    operatorVoice("aadmin/artwork/${it.id}", FormBody.Builder().add("voiceStatus", "1")
                            .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build()
                    ) { result ->
                        transLayout.showContent()
                        result?.let { _ ->
                            if (result.code == 0) {
                                it.artwork_status = 1
                                tvReplace.text = "删除"
                            }
                        }
                    }
                }
            }
        }
        tvUserFlag.setOnClickListener {
            userInfo?.let {
                transLayout.showProgress()
                operatorUser(it.user_id, FormBody.Builder().add("flag", if (it.flag == "1") "0" else "1").add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build()) {
                    transLayout.showContent()
                    userInfo!!.flag = if (userInfo!!.flag == "1") "0" else "1"
                    tvUserFlag.text = if (userInfo!!.flag == "1") "已设为仙人掌" else "设为仙人掌"
                }
            }
        }
        tvUserStatus.setOnClickListener {
            userInfo?.let {
                transLayout.showProgress()
                operatorUser(it.user_id, FormBody.Builder().add("userStatus", if (it.user_status == "3") "1" else "3").add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build()) {
                    transLayout.showContent()
                    userInfo!!.flag = if (userInfo!!.user_status == "3") "1" else "3"
                    tvUserFlag.text = if (userInfo!!.user_status == "1") "封号" else "已封号"
                }
            }
        }
        ivContent.setOnClickListener {
            artBean?.let {
                startActivity<ShowPicActivity>("path" to it.artwork_url)
                overridePendingTransition(R.anim.dialog_alpha_enter, 0)
            }
        }
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "artworks/${data.data_id}", AdminReportGraffitiData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {

                    }

                    override fun success(result: Any?) {
                        result as AdminReportGraffitiData
                        result.data?.let {
                            artBean = it
                            tvHide.text = if (it.hide_at > 0) "已隐藏" else "隐藏"
                            tvReplace.text = if (it.artwork_status == 1) "删除" else "已删除"
                        }
                    }
                }, "V4.2")
            }
            1 -> {
                getUserInfo(data.user.id) {
                    userInfo = it.data
                    tvUserFlag.text = if (it.data.flag == "1") "已设为仙人掌" else "设为仙人掌"
                    tvUserFlag.isSelected = it.data.flag == "1"
                    tvUserStatus.isSelected = it.data.user_status == "3"
                    tvUserStatus.text = if (it.data.user_status == "3") "已封号" else "封号"
                }
            }
        }
    }
}