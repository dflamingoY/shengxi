package org.xiaoxingqi.shengxi.modules.user.set

import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import cn.jpush.android.api.JPushInterface
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.AuthService
import com.nineoldandroids.animation.ObjectAnimator
import com.xiaomi.mipush.sdk.MiPushClient
import kotlinx.android.synthetic.main.activity_pwd.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseThemeNoSwipeActivity
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogSelectPhoto
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.IntegerRespData
import org.xiaoxingqi.shengxi.model.NewVersionCoverData
import org.xiaoxingqi.shengxi.model.NewVersionSetSingleData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.MainActivity
import org.xiaoxingqi.shengxi.modules.echoes.ChatActivity
import org.xiaoxingqi.shengxi.modules.login.LoginActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.AlbumActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.CropBgActivity
import org.xiaoxingqi.shengxi.service.SocketServer
import org.xiaoxingqi.shengxi.service.SocketThread
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.discretescrollview.DSVOrientation
import org.xiaoxingqi.shengxi.wedgit.discretescrollview.DiscreteScrollView
import org.xiaoxingqi.shengxi.wedgit.discretescrollview.InfiniteScrollAdapter
import org.xiaoxingqi.shengxi.wedgit.discretescrollview.transform.ScaleTransformer
import java.io.File

/*
    1.设置密码
    2.登录验证
 */
class PwdActivity : BaseThemeNoSwipeActivity() {
    private val mData = arrayListOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
    private lateinit var scrollAdapter1: InfiniteScrollAdapter<BaseAdapterHelper>
    private lateinit var scrollAdapter2: InfiniteScrollAdapter<BaseAdapterHelper>
    private lateinit var scrollAdapter3: InfiniteScrollAdapter<BaseAdapterHelper>
    private var preStatus: Int = 2//默认未开启
    private var preCode = ""
    private var isSet = true//是否从设置跳转该界面
    private var pathCover: String? = null
    private var isOpenPwd = 0//1 开启
    private var isLoadingCover = false
    override fun getLayoutId(): Int {
        return R.layout.activity_pwd
    }

    override fun initView() {
        initRecyclerView(discreteView1)
        initRecyclerView(discreteView2)
        initRecyclerView(discreteView3)
    }

    override fun initData() {
        isOpenPwd = intent.getIntExtra("isOpen", 0)
        val path = SPUtils.getString(this@PwdActivity, IConstant.PWD_DEFAULT_COVER, "")
        if (!TextUtils.isEmpty(path)) {
            if (!isSet || isOpenPwd == 1)
                Glide.with(this)
                        .load(path)
                        .into(ivPwdBg)
            ivClickHint.visibility = View.GONE
        }
        isSet = intent.getBooleanExtra("isVerify", true)
        if (!isSet) {
            tvHelp.visibility = View.VISIBLE
            btn_Back.text = resources.getString(R.string.string_setting_8)
            ivClickHint.visibility = View.GONE
            ivConfirm.isSelected = true
            tvTitle.text = resources.getString(R.string.string_single_pwd_6)
            toggle_Button.visibility = View.GONE
        }
        val adapter1 = object : QuickAdapter<String>(this, R.layout.item_pwd_text, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: String?) {
                helper!!.getTextView(R.id.tvPwd).text = item!!
            }
        }
        scrollAdapter1 = InfiniteScrollAdapter.wrap(adapter1)
        discreteView1.adapter = scrollAdapter1
        val adapter2 = object : QuickAdapter<String>(this, R.layout.item_pwd_text, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: String?) {
                helper!!.getTextView(R.id.tvPwd).text = item!!
            }
        }
        scrollAdapter2 = InfiniteScrollAdapter.wrap(adapter2)
        discreteView2.adapter = scrollAdapter2
        val adapter3 = object : QuickAdapter<String>(this, R.layout.item_pwd_text, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: String?) {
                helper!!.getTextView(R.id.tvPwd).text = item!!
            }
        }
        scrollAdapter3 = InfiniteScrollAdapter.wrap(adapter3)
        discreteView3.adapter = scrollAdapter3
        adapter1.setOnItemClickListener { _, position ->
            val current = discreteView1.currentItem
            if (position != current) {
                discreteView1.smoothScrollToPosition(scrollAdapter1.getClosestPosition(position))
            }
        }
        adapter2.setOnItemClickListener { _, position ->
            val current = discreteView2.currentItem
            if (position != current) {
                discreteView2.smoothScrollToPosition(scrollAdapter2.getClosestPosition(position))
            }
        }
        adapter3.setOnItemClickListener { _, position ->
            val current = discreteView3.currentItem
            if (position != current) {
                discreteView3.smoothScrollToPosition(scrollAdapter3.getClosestPosition(position))
            }
        }
        if (isSet) {
            if (isOpenPwd != 1) {
                ObjectAnimator.ofFloat(linearPwdMenu, "translationX", AppTools.dp2px(this, 90).toFloat()).setDuration(0).start()
            } else
                request(0)
        } else {
            ObjectAnimator.ofFloat(linearPwdMenu, "translationX", AppTools.dp2px(this, 90).toFloat(), 0f).setDuration(520).start()
        }
        if (isOpenPwd == 1 || !isSet)
            request(3)
    }

    @SuppressLint("SetTextI18n")
    override fun initEvent() {
        btn_Back.setOnClickListener {
            //保存设置
            if (isSet) {
                checkFinish()
            } else {
                //回到登录界面 退出MainActivity
                if (OsUtil.isMIUI() || OsUtil.isHw()) {
                    request(4)
                } else {
                    request(5)
                }
            }
        }
        discreteView1.addOnItemChangedListener { _, _ ->
            tvCurrentContent.text = if (isSet) resources.getString(R.string.string_single_pwd_7) else {
                resources.getString(R.string.string_single_pwd_8)
            } + changeNumber()
        }
        discreteView2.addOnItemChangedListener { _, _ ->
            tvCurrentContent.text = if (isSet) resources.getString(R.string.string_single_pwd_7) else {
                resources.getString(R.string.string_single_pwd_8)
            } + changeNumber()
        }
        discreteView3.addOnItemChangedListener { _, _ ->
            tvCurrentContent.text = if (isSet) resources.getString(R.string.string_single_pwd_7) else {
                resources.getString(R.string.string_single_pwd_8)
            } + changeNumber()
        }
        ivConfirm.setOnClickListener {
            //执行动画
            if (mSet != null) {
                if (mSet!!.isRunning) {
                    mSet!!.cancel()
                }
            } else {
                mSet = AnimatorSet()
            }
            mSet!!.playTogether(android.animation.ObjectAnimator.ofFloat(ivConfirm, "ScaleX", 1f, 0.8f, 1f),
                    android.animation.ObjectAnimator.ofFloat(ivConfirm, "ScaleY", 1f, 0.8f, 1f))
            mSet!!.duration = 420
            mSet!!.start()
            if (isSet)
                showToast("当前密码是${scrollAdapter1.realCurrentPosition}${scrollAdapter2.realCurrentPosition}${scrollAdapter3.realCurrentPosition}不要忘记哦")
            else {
                request(1)
            }
        }
        tvHelp.setOnClickListener {
            //小二密码帮助
            startActivity(Intent(this, ChatActivity::class.java)
                    .putExtra("uid", "1")
                    .putExtra("userName", "声昔小二")
                    .putExtra("unreadCount", 0)
                    .putExtra("chatId", "")
                    .putExtra("isHelp", true)
            )
        }
        ivPwdBg.setOnLongClickListener {
            if (!isSet || !toggle_Button.isChecked)
                return@setOnLongClickListener true
            DialogSelectPhoto(this).hideAction(true).hindOther(true).changeText("更换封面").setOnItemClick(object : DialogSelectPhoto.OnItemClick {
                override fun itemView(view: View) {
                    when (view.id) {
                        R.id.tv_Save -> {
                            startActivityForResult(Intent(this@PwdActivity, AlbumActivity::class.java).putExtra("count", 1), 1)
                        }
                        R.id.tv_Other -> {
                            if (TextUtils.isEmpty(pathCover)) {
                                showToast("还没有设置封面")
                            } else
                                save(pathCover!!)
                        }
                    }
                }
            }).show()
            false
        }
        toggle_Button.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                ivPwdBg.visibility = View.VISIBLE
                //开启的第一次需要去查询一下是否有设置过的封面图 打开界面
                ObjectAnimator.ofFloat(linearPwdMenu, "translationX", 0f).setDuration(520).start()
                if (!isLoadingCover)
                    request(3)
            } else {
                ivClickHint.visibility = View.GONE
                ivPwdBg.visibility = View.GONE
                ObjectAnimator.ofFloat(linearPwdMenu, "translationX", AppTools.dp2px(this, 90).toFloat()).setDuration(520).start()
            }
        }
    }

    private var mSet: AnimatorSet? = null
    private fun saveStatus() {
        transLayout.showProgress()
        val login = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(this, "users/${login.user_id}/settings", FormBody.Builder().add("settingName", "login_check_switch")
                .add("settingValue", if (toggle_Button.isChecked) "1" else "2").add("settingTag", "other").build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }

            override fun success(result: Any?) {
                transLayout.showContent()
                result as BaseRepData
                if (result.code == 0) {
                    if (toggle_Button.isChecked) {
                        saveCode()
                    } else {//关闭密码
                        //缓存密码到本地做展示
                        SPUtils.setString(this@PwdActivity, IConstant.USER_PWD, "")
                        finish()
                    }
                } else
                    showToast(result.msg)
            }
        }, "V4.2")
    }

    private fun saveCode() {
        transLayout.showProgress()
        OkClientHelper.patch(this, "loginCheckCode", FormBody.Builder()
                .add("checkCode", changeNumber()).build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }

            override fun success(result: Any?) {
                transLayout.showContent()
                result as BaseRepData
                if (result.code == 0) {
                    //缓存密码到本地做展示
                    SPUtils.setString(this@PwdActivity, IConstant.USER_PWD, changeNumber())
                    finish()
                } else
                    showToast(result.msg)
            }
        }, "V4.3")
    }

    private fun changeNumber(): String {
        return "${scrollAdapter1.realCurrentPosition}${scrollAdapter2.realCurrentPosition}${scrollAdapter3.realCurrentPosition}"
    }

    private fun checkFinish() {
        if (toggle_Button.isChecked) {
            SPUtils.setString(this, IConstant.USER_PWD, changeNumber())
        } else {
            SPUtils.setString(this, IConstant.USER_PWD, "")
        }
        when {
            preStatus != if (toggle_Button.isChecked) 1 else 2 -> {//更改
                //如果关闭之后则不保存密码
                saveStatus()
            }
            preCode != changeNumber() -> {
                if (toggle_Button.isChecked) {
                    saveCode()
                } else finish()
            }
            else -> {
                finish()
            }
        }
    }

    private fun initRecyclerView(recyclerView: DiscreteScrollView) {
        recyclerView.setOrientation(DSVOrientation.HORIZONTAL)
        recyclerView.setSlideOnFling(true)
        recyclerView.setItemTransitionTimeMillis(100)
        recyclerView.setItemTransformer(ScaleTransformer.Builder().setMinScale(0.8f).build())
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "loginCheckCode", IntegerRespData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {
                    }

                    @SuppressLint("SetTextI18n")
                    override fun success(result: Any?) {
                        result as IntegerRespData
                        result.data?.let {
                            if (!TextUtils.isEmpty(it.check_code)) {
                                preCode = it.check_code
                                tvCurrentContent.text = resources.getString(R.string.string_single_pwd_7) + it.check_code
                                it.check_code.split("").filter { condition ->
                                    condition != ""
                                }.map { value ->
                                    value.toInt()
                                }.let { list ->
                                    //匹配当前内容到角标
                                    discreteView1.scrollToPosition(scrollAdapter1.getClosestPosition(list[0]))
                                    discreteView2.scrollToPosition(scrollAdapter2.getClosestPosition(list[1]))
                                    discreteView3.scrollToPosition(scrollAdapter3.getClosestPosition(list[2]))
                                }
                            }
                        }
                    }
                }, "V4.3")
                val login = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "users/${login.user_id}/settings?settingTag=other&settingName=login_check_switch", NewVersionSetSingleData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {
                    }

                    override fun success(result: Any?) {
                        result as NewVersionSetSingleData
                        result.data?.let {
                            if (it.setting_name == "login_check_switch") {
                                preStatus = it.setting_value
                                toggle_Button.isChecked = it.setting_value == 1
                            }
                        }
                        toggle_Button.postDelayed({
                        }, 300)
                    }
                }, "V4.2")
            }
            1 -> {
                transLayout.showProgress()
                OkClientHelper.post(this, "loginCheckCode/verify", FormBody.Builder().add("checkCode", changeNumber()).build(), BaseRepData::class.java, object : OkResponse {
                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }

                    override fun success(result: Any?) {
                        result as BaseRepData
                        if (result.code == 0) {
                            SPUtils.setInt(this@PwdActivity, IConstant.HAS_CHECKED_CODE, 0)
                            finish()
                            overridePendingTransition(R.anim.act_enter_alpha, R.anim.act_guide_anim_exit)
                        } else {
                            showToast(result.msg)
                            transLayout.showContent()
                        }
                    }
                }, "V4.3")
            }
            3 -> {//查询封面
                transLayout.showProgress()
                val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.get(this, "users/${obj.user_id}/covers?coverName=logincheck_cover", NewVersionCoverData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        isLoadingCover = true
                        result as NewVersionCoverData
                        ivPwdBg.visibility = View.VISIBLE
                        if (result.data?.let {
                                    ivClickHint.visibility = View.GONE
                                    //保存缓存图片
                                    pathCover = it[0].coverUrl
                                    SPUtils.setString(this@PwdActivity, IConstant.PWD_DEFAULT_COVER, it[0].coverUrl)
                                    Glide.with(this@PwdActivity)
                                            .load(it[0].coverUrl)
                                            .into(ivPwdBg)
                                    it
                                } == null) {
                            ivPwdBg.setImageResource(R.drawable.drawable_pwd_defalut_bg)
                            SPUtils.setString(this@PwdActivity, IConstant.PWD_DEFAULT_COVER, "")
                            if (isSet)
                                ivClickHint.visibility = View.VISIBLE
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        isLoadingCover = true
                        transLayout.showContent()
                    }
                }, "V4.2")
            }
            4 -> {//退出登录 删除华为 小米的推送id
                val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.delete(this, "users/${obj.user_id}/pushDevice", FormBody.Builder().add("vendorId", if (OsUtil.isMIUI()) "2" else "1").build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as BaseRepData
                        if (result.code == 0) {
                            request(5)
                        } else {
                            transLayout.showContent()
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V3.6")
            }
            5 -> {//置空机关id
                try {
                    val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    LocalLogUtils.writeLog("退出登录,    ${obj.user_id} ", System.currentTimeMillis())
                    JPushInterface.deleteAlias(this, obj.user_id.toInt())
                    MiPushClient.unsetAlias(this, obj.user_id, null)
                    OkClientHelper.patch(this, "users/${obj.user_id}", FormBody.Builder().add("jpushId", "").build(), BaseRepData::class.java, object : OkResponse {
                        override fun success(result: Any?) {
                            result as BaseRepData
                            if (result.code == 0) {
                                JPushInterface.deleteAlias(this@PwdActivity, obj.user_id.toInt())
                                PreferenceTools.clear(this@PwdActivity, IConstant.LOCALTOKEN)
                                PreferenceTools.clear(this@PwdActivity, IConstant.USERCACHE)
                                startActivity(Intent(this@PwdActivity, LoginActivity::class.java))
                                MainActivity.sInstance?.finish()
                                NIMClient.getService(AuthService::class.java).logout()
                                finish()
                            } else {
                                showToast(result.msg)
                                transLayout.showContent()
                            }
                        }

                        override fun onFailure(any: Any?) {
                            showToast(any.toString())
                            transLayout.showContent()
                        }
                    })
                    //关闭服务
                    SocketThread.instances(applicationContext).onDisConnect()
//                    stopService(Intent(this, SocketServer::class.java))
                } catch (e: Exception) {
                    PreferenceTools.clear(this@PwdActivity, IConstant.LOCALTOKEN)
                    PreferenceTools.clear(this@PwdActivity, IConstant.USERCACHE)
                    startActivity(Intent(this@PwdActivity, LoginActivity::class.java))
                    MainActivity.sInstance?.finish()
                    NIMClient.getService(AuthService::class.java).logout()
                    finish()
                }
            }
        }
    }

    private fun updateCover(path: String) {
        transLayout.showProgress()
        val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "users/${obj.user_id}/covers", FormBody.Builder().add("coverName", "logincheck_cover")
                .add("bucketId", AppTools.bucketId)
                .add("coverUri", path).build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }

            override fun success(result: Any?) {
                transLayout.showContent()
                result as BaseRepData
                if (result.code != 0) {
                    showToast(result.msg)
                }
            }
        }, "V4.2"
        )
    }

    @SuppressLint("StaticFieldLeak")
    private fun save(path: String) {
        var suffix = path.substring(path.lastIndexOf("/") + 1)
        if (!suffix.contains(".jpg") && !suffix.contains(".png") && !suffix.contains(".gif")) {
            suffix = "$suffix.jpg"
        }
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD + "/" + suffix)
        if (file.exists()) {
            showToast("图片已存在")
            return
        }
        transLayout.showProgress()
        object : AsyncTask<Void, Void, File>() {
            override fun doInBackground(vararg voids: Void): File? {
                return try {
                    Glide.with(this@PwdActivity)
                            .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(System.currentTimeMillis())))
                            .downloadOnly()
                            .load(path)
                            .submit()
                            .get()
                } catch (e: Exception) {
                    null
                }
            }

            override fun onPostExecute(s: File?) {
                transLayout.showContent()
                if (null == s) {
                    showToast("图片保存失败")
                } else {
                    val bootFile = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD)
                    if (!bootFile.exists()) {
                        bootFile.mkdirs()
                    }
                    val resultFile = File(bootFile.absolutePath + "/" + suffix)
                    FileUtils.copyFile(s, resultFile)
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    val contentUri = Uri.fromFile(resultFile)
                    mediaScanIntent.data = contentUri
                    sendBroadcast(mediaScanIntent)
                    showToast("保存成功")
                }
            }
        }.execute()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isSet) {
                checkFinish()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        if (isSet)
            super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                data?.let {
                    val result = it.getSerializableExtra("result") as ArrayList<*>
                    if (result.size > 0) {
                        startActivityForResult(Intent(this, CropBgActivity::class.java)
                                .putExtra("resourceType", "23")
                                .putExtra("path", result[0] as String), 0x02)
                    }
                }
            } else if (requestCode == 0x02) {
                data?.let {
                    val path = it.getStringExtra("result")
                    val originalPath = it.getStringExtra("originalPath")
                    ivClickHint.visibility = View.GONE
                    Glide.with(this)
                            .load(originalPath)
                            .into(ivPwdBg)
                    updateCover(path)
                }
            }
        }
    }
}