package org.xiaoxingqi.shengxi.modules.user

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.bumptech.glide.Glide
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.activity_cache.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.AliLoadFactory
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.database.VoiceCacheDao
import org.xiaoxingqi.shengxi.dialog.DialogDeleteTestMsg
import org.xiaoxingqi.shengxi.impl.*
import org.xiaoxingqi.shengxi.model.CacheBean
import org.xiaoxingqi.shengxi.model.QiniuStringData
import org.xiaoxingqi.shengxi.model.SendVoiceData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.model.qiniu.QiniuToken
import org.xiaoxingqi.shengxi.model.qiniu.UploadData
import org.xiaoxingqi.shengxi.utils.*
import java.io.File
import java.lang.Exception

class CacheActivity : BaseAct() {
    val cache = ClearCache()
    private val mData by lazy { ArrayList<CacheBean>() }
    private lateinit var adapter: QuickAdapter<CacheBean>
    private var voiceDao: VoiceCacheDao? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_cache
    }

    override fun initView() {

    }

    override fun initData() {
        voiceDao = VoiceCacheDao(this)
        val obj = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        voiceDao?.getDate(obj.user_id)?.let {
            mData.addAll(it)
        }
        adapter = object : QuickAdapter<CacheBean>(this, R.layout.item_voice_cache, mData) {
            override fun convert(helper: BaseAdapterHelper?, item: CacheBean?) {
                helper!!.getTextView(R.id.tv_Time).text = TimeUtils.getInstance().paserLong(item!!.time / 1000) + "的心情"
                helper.getView(R.id.tv_Delete).setOnClickListener {

                    DialogDeleteTestMsg(this@CacheActivity).setTitle("确定删除这条缓存吗?").setOnClickListener(View.OnClickListener {
                        if (voiceDao?.delete(item.url)!!) {
                            mData.remove(item)
                            adapter.notifyDataSetChanged()
                        }
                    }).show()
                }
                helper.getView(R.id.tv_Resend).setOnClickListener {
                    sendVoice(item)
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        transLayout.showContent()
    }

    override fun initEvent() {
        btn_Back.setOnClickListener {
            finish()
        }
        tv_clear.setOnClickListener {
            clear()
        }
    }

    @SuppressLint("StaticFieldLeak")
    fun clear() {
        Glide.get(this@CacheActivity).clearMemory()
        object : AsyncTask<Void, Void, Int>() {
            override fun doInBackground(vararg voids: Void): Int? {
                Glide.get(this@CacheActivity).clearDiskCache()
                ImageLoader.getInstance().memoryCache.clear()
                ImageLoader.getInstance().diskCache.clear()
                cache.clearCache(this@CacheActivity)
//                cache.deleteFilesByDirectory(File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME))
                //删除云信的录音缓存
//                val file = File(Environment.getExternalStorageDirectory(), "org.xiaoxingqi.shengxi/nim/audio")
//                cache.deleteFilesByDirectory(file)
//                file.delete()
                return 0
            }

            override fun onPostExecute(aVoid: Int) {
                super.onPostExecute(aVoid)
                showToast(resources.getString(R.string.string_clear_cache_success))
            }
        }.execute()
    }

    /**
     * 发送音频 判断当前的图片是否存在, 不存在则跳过
     */
    private fun sendVoice(item: CacheBean) {
        transLayout.showProgress()
        if (!File(item.url).exists()) {
            showToast("缓存文件已经被删除")
            voiceDao?.delete(item.url)
            mData.remove(item)
            adapter.notifyDataSetChanged()
            transLayout.showContent()
            return
        }
        item.imgJson = null
        item.upVoicePath = null
        if (item.type == 1 || item.type == 3) {
            if (!TextUtils.isEmpty(item.images)) {
                upImgs(item)
            }
        } else {//直接发送评论音频文件
            upVoice(item)
        }
    }

    /**
     * 计算文件的MD5
     */
    private fun calcMd5(imgs: String): String {
        val array = JSON.parseArray(imgs, String::class.java)
        val jsonArray = JSONArray()
        for (bean in array) {
            if (!File(bean).exists()) {//如果图片不存在, 则跳过
                continue
            }
            var endSuffix: String
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(bean, options)
            val type = options.outMimeType ?: continue
            if (type.contains("gif", true)) {
                endSuffix = ".gif"
            } else {
                try {
                    endSuffix = bean.substring(bean.lastIndexOf("."), bean.length)
                } catch (e: Exception) {
                    endSuffix = ".png"
                }
            }
            var suffix: String = TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())
            /*try {
                val exif = ExifInterface(bean)
                val time = exif.getAttribute(ExifInterface.TAG_DATETIME)
                if (!TextUtils.isEmpty(time)) {
                    suffix = time.substring(0, time.indexOf(" ")).replace(":", "").replace(" ", "")
                }
            } catch (e: Exception) {

            }*/
            jsonArray.add(suffix + "_" + AppTools.getFileMD5(File(bean)) + endSuffix)
        }
        return if (jsonArray.size > 0) jsonArray.toJSONString() else ""
    }

    private fun paserData(keys: List<String>, imgs: String): Array<UploadData?> {
        val array = JSON.parseArray(imgs, String::class.java)
//        val strs = arrayOfNulls<UploadData>(keys.size)
        val list = ArrayList<UploadData>()

        array.toTypedArray()
        for (a in array.indices) {
            if (!File(array[a]).exists()) {
                continue
            }
            list.add(UploadData(keys[list.size], array[a]))
//            strs[a] = UploadData(keys[a], array[a])
        }
        return list.toTypedArray()
    }


    /**
     * 上传图片
     */
    private fun upImgs(item: CacheBean) {
        val calcMd5 = calcMd5(item.images)
        if (TextUtils.isEmpty(calcMd5)) {
            /**
             * 发送音频文件
             */
            upVoice(item)
            return
        }
        val formBody = FormBody.Builder()
                .add("resourceType", "5")
                .add("resourceContent", calcMd5)
                .build()

        OkClientHelper.post(this, "resource", formBody, QiniuToken::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as QiniuToken
                LocalLogUtils.writeLog("缓存:上传图片${item.images}", System.currentTimeMillis())
                if (result.code == 0) {
                    AliLoadFactory(this@CacheActivity, result.data.end_point, result.data.bucket, result.data.oss, object : LoadStateListener {
                        override fun progress(current: Long) {

                        }

                        var mJSONArray = JSONArray()
                        override fun success() {
                            item.imgJson = mJSONArray.toJSONString()
                            upVoice(item)
                        }

                        override fun fail() {
                            runOnUiThread {
                                transLayout.showContent()
                                showToast("发布失败, 请等网络好的时候再尝试发布")
                            }
                        }

                        override fun oneFinish(endTag: String?, position: Int) {
                            mJSONArray.add(result.data.resource_content[result.data.resource_content.size - 1 - position])
                        }
                    }, *paserData(result.data.resource_content, item.images))
                } else {
                    transLayout.showContent()
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                if (!AppTools.isNetOk(this@CacheActivity)) {
                    showToast("发布失败, 请等网络好的时候再尝试发布")
                } else {
                    if (any is String) {
                        showToast("发布失败, 请等网络好的时候再尝试发布")
                    } else
                        showToast(any.toString())
                }
                transLayout.showContent()
            }
        })
    }


    /**
     * 上传音频文件
     */
    private fun upVoice(item: CacheBean) {
        val formBody = FormBody.Builder()
                .add("resourceType", "2")
                .add("resourceContent", "${TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())}_${AppTools.getFileMD5(File(item.url))}.aac")
                .build()
        OkClientHelper.post(this, "resource", formBody, QiniuStringData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as QiniuStringData
                if (result.code == 0) {
                    result.data.bucket_id?.let {
                        bucketId = it
                    }
                    LocalLogUtils.writeLog("缓存:上传声兮源文件", System.currentTimeMillis())
                    AliLoadFactory(this@CacheActivity, result.data.end_point, result.data.bucket, result.data.oss, object : LoadStateListener {
                        override fun progress(current: Long) {

                        }

                        override fun success() {
                            item.upVoicePath = result.data.resource_content
                            requestVoice(item)
                        }

                        override fun fail() {//oss 异常
                            runOnUiThread {
                                transLayout.showContent()
                                showToast("发布失败, 请等网络好的时候再尝试发布")
                            }
                        }

                        override fun oneFinish(endTag: String?, position: Int) {

                        }
                    }, UploadData(result.data.resource_content, item.url))
                } else {
                    showToast(result.msg)
                    transLayout.showContent()
                }
            }

            override fun onFailure(any: Any?) {
                if (!AppTools.isNetOk(this@CacheActivity)) {
                    showToast("发布失败, 请等网络好的时候再尝试发布")
                } else {
                    if (any is String) {
                        showToast("发布失败, 请等网络好的时候再尝试发布")
                    } else
                        showToast(any.toString())
                }
                transLayout.showContent()
            }
        })
    }

    /**
     * 发布心情
     */
    private fun requestVoice(item: CacheBean) {
        val buider = FormBody.Builder()
                .add("voiceType", item.typeId)
                .add("voiceUri", item.upVoicePath)
                .add("voiceLen", item.voiceLength)
                .add("bucketId", bucketId)
        if (!TextUtils.isEmpty(item.imgJson)) {
            buider.add("voiceImg", item.imgJson)
        }
        if (!TextUtils.isEmpty(item.topicName)) {
            buider.add("topicId", if (null == item.topicId) "" else item.topicId)
                    .add("topicName", item.topicName)
        }
        if (item.type == 2 || item.type == 4 || item.type == 5) {
            buider.add(if (item.type == 2) "movieId" else if (item.type == 4) "songId" else "bookId", item.resourceId)
                    .add("score", item.score)
                    .add("resourceType", if (item.type == 2) "1" else if (item.type == 4) "3" else "2")
        }
        OkClientHelper.post(this, "voices", buider.build(), SendVoiceData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                /**
                 * 发送通知 刷新界面
                 */
                transLayout.showContent()
                result as SendVoiceData
                if (result.code == 0) {
                    showToast("发布成功")
                    /**
                     * 重置总时长
                     */
                    mData.remove(item)
                    adapter.notifyDataSetChanged()
                    voiceDao?.delete(item.url)
                    val loginBean = PreferenceTools.getObj(this@CacheActivity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    SPUtils.setInt(this@CacheActivity, IConstant.TOTALLENGTH + loginBean.user_id, 1)
                    EventBus.getDefault().post(UpdateSendVoice(1, if (null == result.data) null else result.data.voice_id))
                } else {
                    showToast(result.msg)
                    transLayout.showContent()
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
                if (!AppTools.isNetOk(this@CacheActivity)) {
                    showToast("发布失败, 请等网络好的时候再尝试发布")
                } else {
                    showToast(any.toString())
                }
            }
        })
    }

    private var bucketId = "0"
}