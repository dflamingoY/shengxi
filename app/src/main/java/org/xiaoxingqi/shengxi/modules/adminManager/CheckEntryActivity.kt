package org.xiaoxingqi.shengxi.modules.adminManager

import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_check_user_info.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.impl.AdminCheckOperatorEvent
import org.xiaoxingqi.shengxi.model.AdminCheckListData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.modules.listen.book.OneBookDetailsActivity
import org.xiaoxingqi.shengxi.modules.listen.movies.OneMovieDetailsActivity
import org.xiaoxingqi.shengxi.modules.listen.music.OneMusicDetailsActivity
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.SPUtils
import org.xiaoxingqi.shengxi.utils.TimeUtils

//书音影词条
class CheckEntryActivity : BaseAct() {
    private lateinit var data: AdminCheckListData.AdminCheckBean
    private var userInfo: UserInfoData.UserBean? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_check_user_info
    }

    override fun initView() {
        tvReplace.text = "替换图片"
        tv_Sub.text = "查看词条"
    }

    override fun initData() {
        data = intent.getParcelableExtra("data")
        glideUtil.loadGlide(data.user.avatar_url, roundImg, 0, glideUtil.getLastModified(data.user.avatar_url))
        tv_UserName.text = data.user.nick_name
        tv_Title.text = when (data.entry_type) {
            1 -> "审核电影词条"
            2 -> "审核书籍词条"
            else -> "审核心情词条"
        }
        tvTime.text = TimeUtils.getInstance().paserLong(data.created_at.toLong())
        Glide.with(this)
                .applyDefaultRequestOptions(RequestOptions().centerCrop())
                .asBitmap()
                .load(data.image_url)
                .into(iv_chat_content)
    }

    override fun initEvent() {
        roundImg.setOnClickListener {
            startActivity<UserDetailsActivity>("id" to data.user.id)
        }
        tv_Sub.setOnClickListener {
            startActivity(Intent(this, when (data.entry_type) {
                1 -> OneMovieDetailsActivity::class.java
                2 -> OneBookDetailsActivity::class.java
                else -> OneMusicDetailsActivity::class.java
            }).putExtra("id", when (data.entry_type) {
                1 -> data.movie_id
                2 -> data.book_id
                else -> data.song_id
            }).putExtra("token", true))
        }
        btn_Back.setOnClickListener { finish() }
        tvIgnore.setOnClickListener {
            transLayout.showProgress()
            operator("${data.id}", 2) {
                transLayout.showContent()
                tvIgnore.text = "已忽略"
                EventBus.getDefault().post(AdminCheckOperatorEvent(data.id))
            }
        }
        tvReplace.setOnClickListener {
            //替换图片
            transLayout.showProgress()
            operator(data.id.toString(), 6) {
                transLayout.showContent()
                tvReplace.text = "已替换"
                EventBus.getDefault().post(AdminCheckOperatorEvent(data.id))
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
    }

    override fun request(flag: Int) {
        getUserInfo(data.user.id) {
            userInfo = it.data
            tvUserFlag.text = if (it.data.flag == "1") "已设为仙人掌" else "设为仙人掌"
            tvUserFlag.isSelected = it.data.flag == "1"
            tvUserStatus.isSelected = it.data.user_status == "3"
            tvUserStatus.text = if (it.data.user_status == "3") "已封号" else "封号"
        }
    }
}