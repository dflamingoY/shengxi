package org.xiaoxingqi.shengxi.modules.user.frag.addAlbum

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.activity_add_item.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseThemeNoSwipeActivity
import org.xiaoxingqi.shengxi.core.http.AliLoadFactory
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogDeleteChatComment
import org.xiaoxingqi.shengxi.impl.IUpdateAlbumEvent
import org.xiaoxingqi.shengxi.impl.LoadStateListener
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.QiniuStringData
import org.xiaoxingqi.shengxi.model.VoiceAlbumData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.model.qiniu.UploadData
import org.xiaoxingqi.shengxi.modules.listen.addItem.AddItemCoverActivity
import org.xiaoxingqi.shengxi.modules.listen.addItem.AddItemNameActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.AlbumActivity
import org.xiaoxingqi.shengxi.utils.*
import java.io.File
import java.util.*

/**
 * 编辑心情专辑信息
 */
class EditAlbumActivity : BaseThemeNoSwipeActivity() {
    companion object {
        private const val REQUEST_PRIVACY = 0x00
        private const val REQUEST_GALLERY = 0x01
        private const val REQUEST_CROP = 0x02
        private const val REQUEST_NAME = 0x03
        private const val REQUEST_PERMISSION = 0x04
    }

    private lateinit var data: VoiceAlbumData.AlbumDataBean
    private var albumName: String? = null
    private var coverPath: String? = null
    private var privacy = "3"
    private lateinit var imgName: String
    override fun getLayoutId(): Int {
        return R.layout.activity_add_item
    }

    override fun initView() {
        relative_delete_album.visibility = View.VISIBLE
        tv_title.text = "编辑专辑信息"
        tv_push.text = "保存"
        relative_album.visibility = View.VISIBLE
        tv_cover_title.text = "封面"
        view_music.visibility = View.GONE
        view_singer.setTitle("谁都可以看")
        view_singer.setMsgCount(resources.getString(R.string.string_privacy_album_3))
        view_album.visibility = View.GONE
        cardLayout.visibility = View.VISIBLE
    }

    override fun initData() {
        imgName = UUID.randomUUID().toString()
        try {
            data = intent.getSerializableExtra("data") as VoiceAlbumData.AlbumDataBean
            Glide.with(this)
                    .applyDefaultRequestOptions(RequestOptions()
                            .signature(ObjectKey(data.album_cover_url)))
                    .load(data.album_cover_url)
                    .into(iv_img)
            privacy = data.album_type.toString()
            albumName = data.album_name
            view_singer.setMsgCount(when (data.album_type) {
                1 -> {
                    resources.getString(R.string.string_privacy_album_5)
                }
                2 -> {
                    resources.getString(R.string.string_privacy_album_4)
                }
                else -> {
                    resources.getString(R.string.string_privacy_album_3)
                }
            })
            tv_album_name.text = data.album_name
            tv_push.isSelected = true
        } catch (e: Exception) {
            finish()
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        relative_cover.setOnClickListener {
            checkPermission()
        }
        relative_album.setOnClickListener {
            startActivityForResult(Intent(this, AddItemNameActivity::class.java)
                    .putExtra("count", 20)
                    .putExtra("name", albumName)
                    .putExtra("title", resources.getString(R.string.string_add_album_name)), REQUEST_NAME)
        }
        view_singer.setOnClickListener {
            startActivityForResult(Intent(this, AlbumPrivacySetActivity::class.java).putExtra("privacy", privacy), REQUEST_PRIVACY)
        }
        tv_push.setOnClickListener {
            if (TextUtils.isEmpty(coverPath) && privacy == data.album_type.toString() && albumName == data.album_name) {
                finish()
            } else {
                if (!TextUtils.isEmpty(coverPath)) {
                    request(0)
                } else {
                    upload("", null)
                }
            }
        }
        relative_delete_album.setOnClickListener {
            DialogDeleteChatComment(this).setHint(resources.getString(R.string.string_delete_album_hint)).setOnClickListener(View.OnClickListener {
                request(1)
            }).show()
        }
    }

    /**
     * 检测是否有权限
     */
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION)
            } else {
                openGallery()
            }
        } else {
            openGallery()
        }
    }

    /**
     * 选取图片
     */
    private fun openGallery() {
        startActivityForResult(Intent(this, AlbumActivity::class.java).putExtra("count", 1), REQUEST_GALLERY)
    }

    private fun upload(uri: String, baseUri: String?) {
        transLayout.showProgress()
        val builder = FormBody.Builder()
        if (!TextUtils.isEmpty(uri)) {
            builder.add("albumCoverUri", uri).add("bucketId", AppTools.bucketId)
        }
        if (data.album_name != albumName)
            builder.add("albumName", albumName)
        if (data.album_type.toString() != privacy)
            builder.add("albumType", privacy)
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(this, "user/${loginBean.user_id}/voiceAlbum/${data.id}", builder.build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                transLayout.showProgress()
                if (result.code == 0) {
                    val event = IUpdateAlbumEvent(2, data.id)
                    event.originSort = data.album_type
                    if (!TextUtils.isEmpty(albumName)) {
                        event.name = albumName
                    }
                    if (!TextUtils.isEmpty(uri)) {
                        event.cover = baseUri + uri
                    }
                    event.visibleType = Integer.parseInt(privacy)
                    EventBus.getDefault().post(event)
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
        if (flag == 0) {
            if (File(coverPath).length() == 0L) {
                showToast("封面图已被删除")
                return
            }
            transLayout.showProgress()
            val formBody = FormBody.Builder()
                    .add("resourceType", "18")
                    .add("resourceContent", "${TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())}_${AppTools.getFileMD5(File(coverPath))}.png")
                    .add("needBaseUri", "1")
                    .build()
            OkClientHelper.post(this, "resource", formBody, QiniuStringData::class.java, object : OkResponse {
                override fun success(result: Any?) {
                    result as QiniuStringData
                    if (result.code == 0) {
                        if (!TextUtils.isEmpty(result.data.bucket_id))
                            AppTools.bucketId = result.data.bucket_id
                        LocalLogUtils.writeLog("Add 修改专辑 :上传book cover:${result.data}", System.currentTimeMillis())
                        AliLoadFactory(this@EditAlbumActivity, result.data.end_point, result.data.bucket, result.data.oss, object : LoadStateListener {
                            override fun progress(current: Long) {

                            }

                            override fun success() {
                                upload(result.data.resource_content, result.data.baseUri)
                            }

                            override fun fail() {//oss 异常
                                runOnUiThread {
                                    transLayout.showContent()
                                    LocalLogUtils.writeLog("Add 修改专辑:传音封面失败 oss error", System.currentTimeMillis())
                                }
                            }

                            override fun oneFinish(endTag: String?, position: Int) {

                            }
                        }, UploadData(result.data.resource_content, coverPath))
                    } else {
                        showToast(result.msg)
                    }
                }

                override fun onFailure(any: Any?) {
                    LocalLogUtils.writeLog("Add 修改专辑:传音封面失败 ${any.toString()}", System.currentTimeMillis())
                    transLayout.showContent()
                }
            })
        } else if (flag == 1) {
            transLayout.showProgress()
            OkClientHelper.delete(this, "user/${data.user_id}/voiceAlbum/${data.id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
                override fun success(result: Any?) {
                    result as BaseRepData
                    transLayout.showContent()
                    if (result.code == 0) {
                        val event = IUpdateAlbumEvent(1, data.id)
                        event.originSort = data.album_type
                        EventBus.getDefault().post(event)
                        finish()
                    } else {
                        showToast(result.msg)
                    }
                }

                override fun onFailure(any: Any?) {
                    transLayout.showContent()
                    LocalLogUtils.writeLog("Add 修改专辑:删除专辑 ${any.toString()}", System.currentTimeMillis())
                }
            }, "V3.8")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_NAME -> {
                    albumName = data?.getStringExtra("name")
                    tv_album_name.text = albumName
                }
                REQUEST_PRIVACY -> {
                    data?.let {
                        privacy = it.getStringExtra("privacy")
                        view_singer.setMsgCount(when (privacy) {
                            "1" -> {
                                resources.getString(R.string.string_privacy_album_5)
                            }
                            "2" -> {
                                resources.getString(R.string.string_privacy_album_4)
                            }
                            else -> {
                                resources.getString(R.string.string_privacy_album_3)
                            }
                        })
                    }
                }
                REQUEST_GALLERY -> {
                    data?.let {
                        val result = it.getSerializableExtra("result") as ArrayList<String>
                        if (result != null && result.size > 0) {
                            startActivityForResult(Intent(this, AddItemCoverActivity::class.java)
                                    .putExtra("rate", 1f)
                                    .putExtra("name", imgName)
                                    .putExtra("path", result[0]), REQUEST_CROP)
                        }
                    }
                }
                REQUEST_CROP -> {
                    data?.let {
                        Glide.with(this)
                                .applyDefaultRequestOptions(RequestOptions()
                                        .skipMemoryCache(true)
                                        .signature(ObjectKey(System.currentTimeMillis().toString())))
                                .load(it.getStringExtra("result"))
                                .into(iv_img)
                        coverPath = it.getStringExtra("result")
                    }
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        try {
            if (File(coverPath).exists()) {
                File(coverPath).delete()
            }
        } catch (e: Exception) {
        }
    }
}