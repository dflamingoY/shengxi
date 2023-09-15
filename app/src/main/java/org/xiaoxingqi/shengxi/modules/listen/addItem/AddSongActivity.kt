package org.xiaoxingqi.shengxi.modules.listen.addItem

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
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.AliLoadFactory
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogCommitPushItem
import org.xiaoxingqi.shengxi.dialog.DialogDeleteResource
import org.xiaoxingqi.shengxi.impl.AddResourceEvent
import org.xiaoxingqi.shengxi.impl.LoadStateListener
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.model.qiniu.UploadData
import org.xiaoxingqi.shengxi.modules.listen.music.OneMusicDetailsActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.AlbumActivity
import org.xiaoxingqi.shengxi.utils.*
import java.io.File
import java.util.*

/**
 * 添加词条
 */
class AddSongActivity : BaseAct() {

    companion object {
        private const val REQUEST_MUSIC_NAME = 0x00
        private const val REQUEST_MUSIC_SINGER = 0x01
        private const val REQUEST_MUSIC_ALBUM = 0x02
        private const val REQUEST_MUSIC_COVER = 0x03
        private const val REQUEST_PERMISSION_FILE = 0x04
        private const val REQUEST_CROP_COVER = 0x05
    }

    private lateinit var imgName: String
    private var coverPath: String? = null
    private var songName: String? = null
    private var songSinger: String? = null
    private var songAlbum: String? = null
    private var cacheData: BaseSearchBean? = null
    private var netCover: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_add_item
    }

    override fun initView() {
        tv_push.isSelected = false
    }

    override fun initData() {
        cacheData = intent.getParcelableExtra("data")
        imgName = UUID.randomUUID().toString()
        cacheData?.let {
            //管理员
            cardLayout.visibility = View.VISIBLE
            tv_un_add.visibility = View.GONE
            Glide.with(this)
                    .applyDefaultRequestOptions(RequestOptions()
                            .signature(ObjectKey(it.song_cover)))
                    .load(it.song_cover)
                    .into(iv_img)
            netCover = it.song_cover
            songName = it.song_name
            songSinger = it.song_singer
            songAlbum = it.album_name
            view_music.setMsgCount(songName)
            view_music.setChildTextStatus(true)
            view_singer.setMsgCount(songSinger)
            view_singer.setChildTextStatus(true)
            view_album.setMsgCount(songAlbum)
            view_album.setChildTextStatus(true)
            tv_push.isSelected = true
            tvDelete.visibility = View.VISIBLE
            request(0)
        }
    }

    override fun initEvent() {
        tv_push.setOnClickListener {
            if (tv_push.isSelected) {
                if (cacheData != null) {
                    if (TextUtils.isEmpty(coverPath)) {
                        transLayout.showProgress()
                        adminUpload("", "")
                    } else {
                        request(1)
                    }
                } else
                    DialogCommitPushItem(this).setOnClickListener(View.OnClickListener {
                        request(1)
                    }).show()
            }
        }
        btn_Back.setOnClickListener { finish() }
        relative_cover.setOnClickListener {
            checkPermission()
        }
        view_music.setOnClickListener {
            startActivityForResult(Intent(this, AddItemNameActivity::class.java)
                    .putExtra("name", songName)
                    .putExtra("title", "添加歌曲名"), REQUEST_MUSIC_NAME)
        }
        view_singer.setOnClickListener {
            startActivityForResult(Intent(this, AddItemNameActivity::class.java)
                    .putExtra("name", songSinger)
                    .putExtra("title", "添加歌手"), REQUEST_MUSIC_SINGER)
        }
        view_album.setOnClickListener {
            startActivityForResult(Intent(this, AddItemNameActivity::class.java)
                    .putExtra("name", songAlbum)
                    .putExtra("title", "添加" + resources.getString(R.string.string_song_album)), REQUEST_MUSIC_ALBUM)
        }
        tvDelete.setOnClickListener {
            DialogDeleteResource(this).setOnClickListener(View.OnClickListener {
                deleteItem()
            }).show()
        }
    }

    private fun jude(): Boolean {
        return (!TextUtils.isEmpty(coverPath) || !TextUtils.isEmpty(netCover)) && !TextUtils.isEmpty(songName) && !TextUtils.isEmpty(songSinger) && !TextUtils.isEmpty(songAlbum)
    }

    /**
     * 检测是否有权限
     */
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION_FILE)
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
        startActivityForResult(Intent(this, AlbumActivity::class.java).putExtra("count", 1), REQUEST_MUSIC_COVER)
    }

    //删除词条资源类型，1=影片  2 书籍  3 唱歌
    private fun deleteItem() {
        transLayout.showProgress()
        OkClientHelper.delete(this, "admin/resource/3/${cacheData?.id}", FormBody.Builder()
                .add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, ""))
                .build(), BaseRepData::class.java, object : OkResponse {
            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }

            override fun success(result: Any?) {
                transLayout.showContent()
                result as BaseRepData
                if (result.code == 0) {
                    showToast("操作成功")
                } else {
                    showToast(result.msg)
                }
            }
        })
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                transLayout.showProgress()
                OkClientHelper.get(this, "resources/3/${cacheData?.id}", OneDetailsData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as OneDetailsData
                        if (result.code == 0) {
                            if (result.data != null) {
                                cacheData = result.data
                                Glide.with(this@AddSongActivity)
                                        .applyDefaultRequestOptions(RequestOptions()
                                                .signature(ObjectKey(result.data.song_cover)))
                                        .load(result.data.song_cover)
                                        .into(iv_img)
                                netCover = result.data.song_cover
                                songName = result.data.song_name
                                songSinger = result.data.song_singer
                                songAlbum = result.data.album_name
                            }
                        }
                        transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        transLayout.showContent()
                    }
                }, "V3.2")
            }
            1 -> {
                if (File(coverPath).length() == 0L) {
                    showToast("封面图已被删除")
                    return
                }
                transLayout.showProgress()
                val formBody = FormBody.Builder()
                        .add("resourceType", "17")
                        .add("resourceContent", "${TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())}_${AppTools.getFileMD5(File(coverPath))}.png")
                        .add("needBaseUri", "1")
                        .build()
                OkClientHelper.post(this, "resource", formBody, QiniuStringData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as QiniuStringData
                        if (result.code == 0) {
                            LocalLogUtils.writeLog("Add song :上传book cover:${result.data}", System.currentTimeMillis())
                            AliLoadFactory(this@AddSongActivity, result.data.end_point, result.data.bucket, result.data.oss, object : LoadStateListener {
                                override fun progress(current: Long) {

                                }

                                override fun success() {
                                    if (cacheData != null) {
                                        adminUpload(result.data.resource_content, result.data.baseUri)
                                    } else
                                        upload(result.data.resource_content, result.data.baseUri)
                                }

                                override fun fail() {//oss 异常
                                    runOnUiThread {
                                        transLayout.showContent()
                                        LocalLogUtils.writeLog("Add song:传音封面失败 oss error", System.currentTimeMillis())
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
                        LocalLogUtils.writeLog("Add song:传音封面失败 ${any.toString()}", System.currentTimeMillis())
                        transLayout.showContent()
                    }
                })
            }
        }
    }

    private fun adminUpload(imgCover: String, baseUri: String) {
        val formBody = FormBody.Builder()
        if (!TextUtils.isEmpty(imgCover))
            formBody.add("songCoverUri", imgCover)
        if (songAlbum != cacheData?.album_name) {
            formBody.add("albumName", songAlbum)
        }
        if (songName != cacheData?.song_name) {
            formBody.add("songName", songName)
        }
        if (songSinger != cacheData?.song_singer)
            formBody.add("songSinger", songSinger)
        if (formBody.build().size() == 0) {
            finish()
            return
        }
        formBody.add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, ""))
        OkClientHelper.patch(this, "admin/resource/3/${cacheData?.id}", formBody.build(), BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                transLayout.showContent()
                if (result.code == 0) {
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        })
    }

    private fun upload(imgCover: String, baseUri: String) {
        val formBody = FormBody.Builder()
                .add("albumName", songAlbum)
                .add("songName", songName)
                .add("songCoverUri", imgCover)
                .add("songSinger", songSinger)
                .build()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "user/${loginBean.user_id}/entries/3", formBody, ConfessionsData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as ConfessionsData
                if (result.code == 0) {
                    File(coverPath).delete()
                    showToast("添加完成")
                    val searchBean = BaseSearchBean()
                    searchBean.id = result.data.resource_id
                    searchBean.song_cover = baseUri + imgCover
                    searchBean.album_name = songAlbum
                    searchBean.song_name = songName
                    searchBean.song_singer = songSinger
                    EventBus.getDefault().post(AddResourceEvent(3, searchBean))
                    startActivity(Intent(this@AddSongActivity, OneMusicDetailsActivity::class.java).putExtra("id", result.data.resource_id))
                    finish()
                } else {
                    showToast(result.msg)
                    transLayout.showContent()
                }
            }

            override fun onFailure(any: Any?) {
                transLayout.showContent()
            }
        }, "V3.7")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_MUSIC_NAME -> {
                    songName = data?.getStringExtra("name")
                    view_music.setMsgCount(songName)
                    view_music.setChildTextStatus(true)
                    tv_push.isSelected = jude()
                }
                REQUEST_MUSIC_SINGER -> {
                    songSinger = data?.getStringExtra("name")
                    view_singer.setMsgCount(songSinger)
                    view_singer.setChildTextStatus(true)
                    tv_push.isSelected = jude()
                }
                REQUEST_MUSIC_ALBUM -> {
                    songAlbum = data?.getStringExtra("name")
                    view_album.setMsgCount(songAlbum)
                    view_album.setChildTextStatus(true)
                    tv_push.isSelected = jude()
                }
                REQUEST_MUSIC_COVER -> {
                    data?.let {
                        val result = it.getSerializableExtra("result") as ArrayList<String>
                        if (result != null && result.size > 0) {
                            startActivityForResult(Intent(this, AddItemCoverActivity::class.java)
                                    .putExtra("rate", 1f)
                                    .putExtra("name", imgName)
                                    .putExtra("path", result[0]), REQUEST_CROP_COVER)
                        }
                    }
                }
                REQUEST_CROP_COVER -> {
                    cardLayout.visibility = View.VISIBLE
                    tv_un_add.visibility = View.GONE
                    data?.let {
                        Glide.with(this)
                                .applyDefaultRequestOptions(RequestOptions()
                                        .skipMemoryCache(true)
                                        .signature(ObjectKey(System.currentTimeMillis().toString())))
                                .load(it.getStringExtra("result"))
                                .into(iv_img)
                        coverPath = it.getStringExtra("result")
                        tv_push.isSelected = jude()
                    }
                }
            }
        }
    }
}