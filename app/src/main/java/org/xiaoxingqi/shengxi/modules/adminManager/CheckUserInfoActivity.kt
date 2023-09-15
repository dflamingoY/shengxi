package org.xiaoxingqi.shengxi.modules.adminManager

import android.annotation.SuppressLint
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.activity_check_chat.*
import kotlinx.android.synthetic.main.activity_check_user_info.*
import kotlinx.android.synthetic.main.activity_check_user_info.btn_Back
import kotlinx.android.synthetic.main.activity_check_user_info.transLayout
import kotlinx.android.synthetic.main.activity_check_user_info.tvIgnore
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.impl.AdminCheckOperatorEvent
import org.xiaoxingqi.shengxi.model.AdminCheckListData
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.CheckUserCoverData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.SPUtils
import org.xiaoxingqi.shengxi.utils.TimeUtils

class CheckUserInfoActivity : BaseAct() {
    private lateinit var data: AdminCheckListData.AdminCheckBean
    private var userInfoData: UserInfoData? = null
    private var coverBean: CheckUserCoverData.CheckUserCoverBean? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_check_user_info
    }

    override fun initView() {

    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        data = intent.getParcelableExtra("data")
        glideUtil.loadGlide(data.user.avatar_url, roundImg, 0, glideUtil.getLastModified(data.user.avatar_url))
        Glide.with(this)
                .applyDefaultRequestOptions(RequestOptions().centerCrop().error(R.drawable.drawable_default_tmpry).signature(ObjectKey(data.image_url)))
                .load(data.image_url)
                .into(iv_chat_content)
        tv_UserName.text = data.user.nick_name
        tvTime.text = TimeUtils.getInstance().paserLong(data.created_at.toLong()) /*+ when (data.data_from) {
            34 -> "上传心情簿封面"
            35 -> "上传心情簿日历封面"
            36 -> "上传心情专辑封面"
            38 -> "上传词条封面"
            39 -> "上传时光机封面"
            40 -> "上传密码封面"
            else -> ""
        }*/
        tvType.text = when (data.data_from) {
            34 -> "上传心情簿封面"
            35 -> "上传心情簿日历封面"
            36 -> "上传心情专辑封面"
            38 -> "上传词条封面"
            39 -> "上传时光机封面"
            40 -> "上传密码封面"
            31 -> "上传头像"
            32 -> "上传心情簿图片"
            else -> ""
        }
        request(0)
        request(1)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        tvIgnore.setOnClickListener {
            ignore(2)
        }
        tvReplace.setOnClickListener {
            replace()
//            coverBean?.let {
//            }
        }
        tvUserFlag.setOnClickListener {
            userInfoData?.let {
                adminUpdate(1, if (it.data.flag == "1") 0 else 1, FormBody.Builder().add("flag", if (it.data.flag == "1") "0" else "1").add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build())
            }
        }
        tvUserStatus.setOnClickListener {
            userInfoData?.let {
                adminUpdate(2, if (it.data.user_status == "3") 0 else 1, FormBody.Builder().add("userStatus", if (it.data.user_status == "3") "1" else "3").add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, "")).build())
            }
        }
    }

    /**
     * @param type 2 忽略 3 删除  5 封号
     */
    private fun ignore(type: Int) {
        OkClientHelper.patch(this, "admin/suspiciousDatas/${data.id}", FormBody.Builder()
                .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, ""))
                .add("operationType", "$type").build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    EventBus.getDefault().post(AdminCheckOperatorEvent(data.id))
                    if (type == 2) {
                        tvIgnore.text = "已忽略"
                    } else {
                        tvIgnore.text = "已处理"
                    }
                }
            }
        })
    }

    private fun replace() {
        transLayout.showProgress()
        /* OkClientHelper.patch(this, "admin/suspiciousDatas/${data.id}/replace", FormBody.Builder()
                 .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, ""))
                 .build(), BaseRepData::class.java, object : OkResponse {
             override fun onFailure(any: Any?) {
                 transLayout.showContent()
             }

             override fun success(result: Any?) {
                 result as BaseRepData
                 if (result.code == 0) {
                     tvReplace.isSelected = true
                     ignore(2)
 //                    request(0)
                 }
                 transLayout.showContent()
             }
         })*/
        OkClientHelper.patch(this, "admin/suspiciousDatas/${data.id}", FormBody.Builder()
                .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, ""))
                .add("operationType", "6").build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {

            }

            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    EventBus.getDefault().post(AdminCheckOperatorEvent(data.id))
                    tvIgnore.text = "已处理"
                }
            }
        })
    }

    /**
     * @param status 设置为 0 正常 1 异常
     */
    private fun adminUpdate(type: Int, status: Int, formBody: FormBody) {
        transLayout.showProgress()
        OkClientHelper.patch(this, "admin/users/${data.user.id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    if (type == 1) {
                        tvUserFlag.text = if (status == 0) "设置为仙人掌" else "已设置为仙人掌"
                        tvUserFlag.isSelected = status != 0
                    } else {
                        tvUserStatus.text = if (status == 0) "封号" else "已封号"
                        tvUserStatus.isSelected = (status == 1)
                        ignore(5)
                    }
                    showToast("操作已执行")
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "admin/covers/${data.data_id}?token=${SPUtils.getString(this, IConstant.ADMINTOKEN, "")}", CheckUserCoverData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {

                    }

                    override fun success(result: Any?) {
                        result as CheckUserCoverData
                        result.data?.let {
                            coverBean = it
                            /*Glide.with(this@CheckUserInfoActivity)
                                    .applyDefaultRequestOptions(RequestOptions().centerCrop().error(R.drawable.drawable_default_tmpry).signature(ObjectKey(it.cover_url)))
                                    .load(it.cover_url)
                                    .into(iv_chat_content)*/
                        }
                    }
                })
            }
            1 -> {//查询用户信息, 是否封号 仙人掌
                OkClientHelper.get(this, "users/${data.user.id}", UserInfoData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {

                    }

                    override fun success(result: Any?) {
                        result as UserInfoData
                        result.data?.let {
                            userInfoData = result
                            tvUserFlag.text = if (it.flag == "1") "已设置为仙人掌" else "设为仙人掌"
                            tvUserFlag.isSelected = it.flag == "1"
                            tvUserStatus.isSelected = it.user_status == "3"
                            tvUserStatus.text = if (it.user_status == "3") "已封号" else "封号"
                        }
                    }
                })
            }
        }
    }

}