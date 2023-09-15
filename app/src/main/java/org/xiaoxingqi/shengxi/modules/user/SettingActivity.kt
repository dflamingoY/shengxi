package org.xiaoxingqi.shengxi.modules.user

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.view.View
import cn.jpush.android.api.JPushInterface
import com.alibaba.fastjson.JSONArray
import com.bumptech.glide.Glide
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.AuthService
import com.nostra13.universalimageloader.core.ImageLoader
import com.xiaomi.mipush.sdk.MiPushClient
import kotlinx.android.synthetic.main.activity_setting.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivityForResult
import org.xiaoxingqi.shengxi.BuildConfig
import org.xiaoxingqi.shengxi.model.VersionData
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.AliLoadFactory
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogHintCancle
import org.xiaoxingqi.shengxi.dialog.DialogUpadte
import org.xiaoxingqi.shengxi.impl.LoadStateListener
import org.xiaoxingqi.shengxi.impl.OnThemeEvent
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.ListenData
import org.xiaoxingqi.shengxi.model.QiniuStringData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.model.qiniu.QiniuToken
import org.xiaoxingqi.shengxi.model.qiniu.UploadData
import org.xiaoxingqi.shengxi.modules.MainActivity
import org.xiaoxingqi.shengxi.modules.echoes.ChatActivity
import org.xiaoxingqi.shengxi.modules.listen.ActionActivity
import org.xiaoxingqi.shengxi.modules.login.LoginActivity
import org.xiaoxingqi.shengxi.modules.user.set.MsgHintActivity
import org.xiaoxingqi.shengxi.modules.user.set.PrivacyHomeTabActivity
import org.xiaoxingqi.shengxi.service.SocketThread
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.RippleAnimation
import org.xiaoxingqi.shengxi.wedgit.VoiceProgress
import skin.support.SkinCompatManager
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class SettingActivity : BaseAct() {
    val cache = ClearCache()
    private lateinit var dialogUpdate: DialogUpadte

    companion object {
        var instances: SettingActivity? = null
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_setting
    }

    override fun initView() {
        instances = this
        toggleEar.isSelected = SPUtils.getBoolean(this, IConstant.ISEARPIECE, false)
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        toggleWord.isSelected = SPUtils.getBoolean(this, IConstant.WORD_ENABLE_VOICE + loginBean.user_id, false)
        moreHome.setMsgCount(if (SPUtils.getString(this, IConstant.TAB_HOME_INDEX + loginBean.user_id, "0") == "0") "操场" else "寝室")
    }

    override fun initData() {
        try {
//            val caCheSizeFormat = cache.getCaCheSizeFormat(this)
//            viewClear.setMsgCount(caCheSizeFormat)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        tv_Theme_Model.text = if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) resources.getString(R.string.string_setting_7) else resources.getString(R.string.string_setting_17)
    }

    override fun initEvent() {
        moreInfo.setOnClickListener {
            startActivity(Intent(this, UserInfoActivity::class.java))
        }
        btn_Back.setOnClickListener { finish() }
        morePrivacy.setOnClickListener { startActivity(Intent(this, PrivacyActivity::class.java)) }
        linearThemeModel.setOnClickListener {
            tv_Theme_Model.text = if (!TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) resources.getString(R.string.string_setting_7) else resources.getString(R.string.string_setting_17)
            EventBus.getDefault().post(OnThemeEvent())
            RippleAnimation.create(ivThemeMode).setDuration(1000).start(500)
            updateAction("changeViewType", if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) "0" else "1")
        }
        moreAccount.setOnClickListener { startActivity(Intent(this, AccountActivity::class.java)) }
        moreMsg.setOnClickListener { startActivity(Intent(this, MsgHintActivity::class.java)) }
        viewClear.setOnClickListener {
            /**
             * 清除所有缓存
             */
//            clear()
            startActivity(Intent(this, CacheActivity::class.java))
        }
        tv_LoginOut.setOnClickListener {
            if (OsUtil.isMIUI() || OsUtil.isHw()) {
                request(9)
            } else {
                request(7)
            }
        }
        moreUpdate.setOnClickListener {
            request(4)
        }
        logUpdate.setOnClickListener {
            /**
             *更新日志文件到服务器   文件地址   org.xiaoxingqi.shengxi\nim\log
             */
            val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.LOGCACHE + "/sxLog.log")
            if (file.exists() && file.length() > 0) {
                DialogHintCancle(this).setHintTitle("是否上传日志？").setOnClickListener(View.OnClickListener {
                    request(5)
                }).show()
            } else {
                showToast("暂无可上传日志")
            }
        }
        toggleEar.setOnClickListener {
            toggleEar.isSelected = !it.isSelected
            SPUtils.setBoolean(this, IConstant.ISEARPIECE, it.isSelected)
            updateAction("changeSoundType", if (toggleEar.isSelected) "1" else "0")
        }
        moreFaq.setOnClickListener {
            startActivity(Intent(this, FAQActivity::class.java))
        }
        relative_Custom.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java)
                    .putExtra("uid", "1")
                    .putExtra("userName", "声昔小二")
                    .putExtra("unreadCount", 0)
                    .putExtra("chatId", "")
            )
        }
        logUpdateYx.setOnClickListener {
            DialogHintCancle(this).setHintTitle("是否上传？").setOnClickListener(View.OnClickListener {
                request(8)
            }).show()
        }
        viewServer.setOnClickListener {
            startActivity(Intent(this, NetServerActivity::class.java))
        }
        toggleWord.setOnClickListener {
            toggleWord.isSelected = !it.isSelected
            val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            SPUtils.setBoolean(this, IConstant.WORD_ENABLE_VOICE + loginBean.user_id, it.isSelected)
            VoiceProgress.isHideText = it.isSelected
        }
        moreHome.setOnClickListener {
            startActivityForResult<PrivacyHomeTabActivity>(0xf)
        }
    }

    private fun updateAction(type: String, mode: String) {
        transLayout.showProgress()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "users/" + loginBean!!.user_id + "/action", FormBody.Builder()
                .add("actionName", type)
                .add("actionValue", mode).build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any) {
                transLayout.showContent()
            }

            override fun onFailure(any: Any) {
                transLayout.showContent()
            }
        })
    }


    override fun request(flag: Int) {
        transLayout.showProgress()
        when (flag) {
            2 -> {
                transLayout.showProgress()
                OkClientHelper.get(this, "h5/type?htmlType=1", ListenData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as ListenData
                        if (result.code == 0) {
                            if (result.data != null && result.data.size > 0) {
                                /**
                                 * 替換文本中的版本號
                                 */
                                startActivity(Intent(this@SettingActivity, ActionActivity::class.java)
                                        .putExtra("title", result.data[0].html_title)
                                        .putExtra("url", result.data[0].html_content.replace("V1.0.0", "V" + AppTools.getVersion(this@SettingActivity)))
                                        .putExtra("isHtml", true)
                                )
                            }
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            3 -> {
                transLayout.showProgress()
                OkClientHelper.get(this, "h5/type?htmlType=2", ListenData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as ListenData
                        if (result.code == 0) {
                            if (result.data != null && result.data.size > 0) {
                                startActivity(Intent(this@SettingActivity, ActionActivity::class.java)
                                        .putExtra("title", result.data[0].html_title)
                                        .putExtra("url", result.data[0].html_content)
                                        .putExtra("isHtml", true)
                                )
                            }
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            4 -> {
                transLayout.showProgress()
                OkClientHelper.get(this, "apps/1", VersionData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as VersionData
                        if (result.code == 0) {
                            if (result.data != null && result.data.app_version > AppTools.getVersion(this@SettingActivity)) {
                                dialogUpdate = DialogUpadte(this@SettingActivity).setFocusUpdate(result.data.forced_update).setText(result.data.app_content).setOnClickListener(View.OnClickListener {
                                    if (result.data.forced_update == 0) {
                                        val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                                        val uri = Uri.parse(result.data.app_url)
                                        val request = DownloadManager.Request(uri)
                                        request.setTitle(resources.getString(R.string.app_name) + "下载中...")
                                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)//在wifi下下载
                                        val name = "shengxi_${System.currentTimeMillis() / 1000}.apk"
                                        request.setDestinationInExternalPublicDir(IConstant.DOCNAME, name)
                                        request.setMimeType("application/vnd.android.package-archive")
                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                        val enqueue = manager.enqueue(request)
                                        listenerLoad(enqueue, name)
                                    } else {
                                        load(result.data.app_url)
                                    }
                                })
                                dialogUpdate.show()
                            } else {
                                showToast("您的声昔已是最新版本!")
                            }
                        } else {
                            showToast(result.msg)
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            5 -> {//上传日志
                transLayout.showProgress()
                val frombody = FormBody.Builder()
                        .add("resourceType", "8")
                        .add("resourceContent", "${TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())}_${TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())}_${AppTools.getFileMD5(File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.LOGCACHE + "/sxLog.log"))}")
                        .build()
                OkClientHelper.post(this, "resource", frombody, QiniuStringData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        /**
                         * 上传到七牛
                         */
                        result as QiniuStringData
                        if (result.code == 0) {
                            result.data.bucket_id?.let {
                                AppTools.bucketId = it
                            }
                            AliLoadFactory(this@SettingActivity, result.data.end_point, result.data.bucket, result.data.oss, object : LoadStateListener {
                                override fun progress(current: Long) {

                                }

                                override fun success() {
                                    updateLog = result.data.resource_content
                                    request(6)
                                }

                                override fun fail() {
                                    runOnUiThread {
                                        transLayout.showContent()
                                        showToast("上传失败")
                                    }
                                }

                                override fun oneFinish(endTag: String?, position: Int) {

                                }
                            }, UploadData(result.data.resource_content, Environment.getExternalStorageDirectory().absolutePath + "/" + IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.LOGCACHE + "/sxLog.log"))
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            6 -> {
                val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                val formBody = FormBody.Builder()
                        .add("logType", "1")
                        .add("logUri", updateLog)
                        .add("bucketId", AppTools.bucketId)
                        .build()
                OkClientHelper.post(this, "userslog/${loginBean.user_id}", formBody, BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as BaseRepData
                        if (result.code == 0) {
                            try {
                                val file = File(Environment.getExternalStorageDirectory().absolutePath + "/" + IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.LOGCACHE + "/sxLog.log")
                                if (file.exists()) {
                                    file.delete()
                                }
                            } catch (e: Exception) {
                            }
                            updateLog = null
                            transLayout.showContent()
                            showToast("上传成功")
//                            request(8)
                        } else {
                            showToast(result.msg)
                            transLayout.showContent()
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            7 -> {
                /**
                 * 更新极光地址
                 */
                try {
                    val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    LocalLogUtils.writeLog("退出登录,    ${obj.user_id} ", System.currentTimeMillis())
                    JPushInterface.deleteAlias(this, obj.user_id.toInt())
                    MiPushClient.unsetAlias(this, obj.user_id, null)
                    OkClientHelper.patch(this, "users/${obj.user_id}", FormBody.Builder().add("jpushId", "").build(), BaseRepData::class.java, object : OkResponse {
                        override fun success(result: Any?) {
                            result as BaseRepData
                            if (result.code == 0) {
                                JPushInterface.deleteAlias(this@SettingActivity, obj.user_id.toInt())
                                PreferenceTools.clear(this@SettingActivity, IConstant.LOCALTOKEN)
                                PreferenceTools.clear(this@SettingActivity, IConstant.USERCACHE)
                                startActivity(Intent(this@SettingActivity, LoginActivity::class.java))
                                MainActivity.sInstance?.let { it.finish() }
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
                    PreferenceTools.clear(this@SettingActivity, IConstant.LOCALTOKEN)
                    PreferenceTools.clear(this@SettingActivity, IConstant.USERCACHE)
                    startActivity(Intent(this@SettingActivity, LoginActivity::class.java))
                    MainActivity.sInstance?.let { it.finish() }
                    NIMClient.getService(AuthService::class.java).logout()
                    finish()
                }
            }
            8 -> {//上传云信的日志
                transLayout.showProgress()
                val frombody = FormBody.Builder()
                        .add("resourceType", "8")
                        .add("resourceContent", calcYxMd5())
                        .build()
                OkClientHelper.post(this, "resource", frombody, QiniuToken::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        /**
                         * 上传到七牛
                         */
                        result as QiniuToken
                        if (result.code == 0) {
                            AliLoadFactory(this@SettingActivity, result.data.end_point, result.data.bucket, result.data.oss, object : LoadStateListener {
                                override fun progress(current: Long) {

                                }

                                var mJSONArray = JSONArray()
                                override fun success() {
                                    updateYxLog(mJSONArray.toJSONString())
                                }

                                override fun fail() {
                                    runOnUiThread {
                                        transLayout.showContent()
                                        showToast("上传失败")
                                    }
                                }

                                override fun oneFinish(endTag: String?, position: Int) {
                                    mJSONArray.add(result.data.resource_content[result.data.resource_content.size - 1 - position])
                                }
                            }, *paserData(result.data.resource_content))
                        }
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                })
            }
            9 -> {
                /**
                 * 删除第三方推送的token
                 */
                val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                OkClientHelper.delete(this, "users/${obj.user_id}/pushDevice", FormBody.Builder().add("vendorId", if (OsUtil.isMIUI()) "2" else "1").build(), BaseRepData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as BaseRepData
                        if (result.code == 0) {
                            request(7)
                        } else {
                            transLayout.showContent()
                        }
                    }

                    override fun onFailure(any: Any?) {
                    }
                }, "V3.6")
            }
        }
    }

    /**
     * 上传云信的日志文件
     */
    private fun updateYxLog(logPath: String) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val formBody = FormBody.Builder()
                .add("logType", "1")
                .add("logUri", logPath)
                .add("bucketId", AppTools.bucketId)
                .build()
        OkClientHelper.post(this, "userslog/${loginBean.user_id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    showToast("日志上传成功")
                } else {
                    showToast(result.msg)
                }
                transLayout.showContent()
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    private fun paserData(keys: List<String>): Array<UploadData?> {
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/log")
        val files = file.listFiles { filter -> !filter.endsWith(".log") }
        val strs = arrayOfNulls<UploadData>(keys.size)
        for (a in keys.indices) {
            strs[a] = UploadData(keys[a], files[a].absolutePath)
        }
        return strs
    }

    private fun calcYxMd5(): String {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/log")
        val files = file.listFiles { filter -> !filter.endsWith(".log") }
        val jsonArray = JSONArray()
        for (bootFile in files) {
            jsonArray.add("${loginBean.user_id}_${bootFile.absolutePath.substring(bootFile.absolutePath.lastIndexOf("/") + 1)}")
        }
        return jsonArray.toJSONString()
    }

    private var broadcastReceiver: BroadcastReceiver? = null
    private fun listenerLoad(id: Long, name: String) {
        try {
            val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val ID = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (ID == id) {
                        /**
                         * 打开安装界面
                         */
                        val intent = Intent()
                        intent.action = "android.intent.action.VIEW"
                        intent.addCategory("android.intent.category.DEFAULT")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            val contentUri = FileProvider.getUriForFile(this@SettingActivity, BuildConfig.APPLICATION_ID + ".fileprovider", File(Environment.getExternalStorageDirectory().absolutePath + "/" + IConstant.DOCNAME + "/" + name))
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
                        } else {
                            intent.setDataAndType(Uri.fromFile(File(Environment.getExternalStorageDirectory().absolutePath + "/" + IConstant.DOCNAME + "/" + name)), "application/vnd.android.package-archive")
                        }
                        startActivity(intent)
                    }
                }
            }
            registerReceiver(broadcastReceiver, intentFilter)
        } catch (e: Exception) {
        }
    }

    /**
     *HTTP下载
     */
    @SuppressLint("StaticFieldLeak")
    private fun load(path: String) {
        object : AsyncTask<Void, Float, File>() {
            override fun doInBackground(vararg params: Void?): File? {
                val url = URL(path)
                val connt = url.openConnection() as HttpURLConnection
                val allLength = connt.contentLength//文件总长度
                if (allLength <= 0) {
                    return null
                }
                val fileParent = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/${IConstant.CACHE_NAME}/${IConstant.VOICENAME}")
                if (!fileParent.exists()) {
                    fileParent.mkdirs()
                }
                val file = File(fileParent, "shengxi.apk")
                if (connt.responseCode == 200) {
                    val ins = connt.inputStream
                    val fos = FileOutputStream(file)
                    val bytes = ByteArray(1024)
                    var len = 0
                    var current = 0
                    ins.use {
                        fos.use {
                            while (ins.read(bytes).also { len = it } != -1) {
                                it.write(bytes, 0, len)
                                it.flush()
                                current += len
                                publishProgress(current.toFloat() / allLength)
                            }
                        }
                    }
                    fos.close()
                    ins.close()
                }
                return file
            }

            override fun onProgressUpdate(vararg values: Float?) {
                dialogUpdate.updateProgress(values[0]!!)
                super.onProgressUpdate(*values)
            }

            override fun onPostExecute(result: File?) {
                super.onPostExecute(result)
                val intent = Intent()
                intent.action = "android.intent.action.VIEW"
                intent.addCategory("android.intent.category.DEFAULT")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    val contentUri = FileProvider.getUriForFile(this@SettingActivity, BuildConfig.APPLICATION_ID + ".fileprovider", result!!)
                    intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
                } else {
                    intent.setDataAndType(Uri.fromFile(result), "application/vnd.android.package-archive")
                }
                startActivity(intent)

            }
        }.execute()
    }

    private var updateLog: String? = null

    @SuppressLint("StaticFieldLeak")
    fun clear() {
        Glide.get(this@SettingActivity).clearMemory()
        object : AsyncTask<Void, Void, Int>() {
            override fun doInBackground(vararg voids: Void): Int? {
                Glide.get(this@SettingActivity).clearDiskCache()
                ImageLoader.getInstance().memoryCache.clear()
                ImageLoader.getInstance().diskCache.clear()
                cache.clearCache(this@SettingActivity)
//                cache.deleteFilesByDirectory(File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME))
                //删除云信的录音缓存
                val file = File(Environment.getExternalStorageDirectory(), "org.xiaoxingqi.shengxi/nim/audio")
                cache.deleteFilesByDirectory(file)
//                file.delete()
                return 0
            }

            override fun onPostExecute(aVoid: Int) {
                super.onPostExecute(aVoid)
                showToast(resources.getString(R.string.string_clear_cache_success))
                viewClear.setMsgCount("0KB")
            }
        }.execute()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0xf) {
            val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
            moreHome.setMsgCount(if (SPUtils.getString(this, IConstant.TAB_HOME_INDEX + obj.user_id, "0") == "0") "操场" else "寝室")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instances = null
        broadcastReceiver?.let {
            unregisterReceiver(it)
        }
    }
}