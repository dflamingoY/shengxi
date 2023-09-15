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
import org.xiaoxingqi.shengxi.dialog.DialogDateEnter
import org.xiaoxingqi.shengxi.dialog.DialogDeleteResource
import org.xiaoxingqi.shengxi.impl.AddResourceEvent
import org.xiaoxingqi.shengxi.impl.LoadStateListener
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.model.qiniu.UploadData
import org.xiaoxingqi.shengxi.modules.listen.movies.OneMovieDetailsActivity
import org.xiaoxingqi.shengxi.modules.publicmoudle.AlbumActivity
import org.xiaoxingqi.shengxi.utils.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class AddMovieItemActivity : BaseAct() {

    companion object {
        private const val REQUEST_MOVIES_NAME = 0x1
        private const val REQUEST_MOVIES_ALBUM = 0x02
        private const val REQUEST_MOVIES_CATEGORY = 0x03
        private const val REQUEST_MOVIES_CROP = 0x04
        private const val REQUEST_PERMISSION = 0x05
        private const val REQUEST_MOVIES_DESC = 0x06
    }

    private var coverPath: String? = null
    private lateinit var imgName: String
    private var movieName: String? = null
    private var movieCategory: String? = null
    private var releaseDate: String? = null
    private var cacheData: BaseSearchBean? = null
    private var netCover: String? = null
    private var desc: String? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_add_item
    }

    override fun initView() {
        tv_push.isSelected = false
        val params = cardLayout.layoutParams
        params.width = AppTools.dp2px(this, 29)
        cardLayout.layoutParams = params
        tv_cover_title.text = resources.getString(R.string.string_add_movie_item_cover)
        view_music.setTitle(resources.getString(R.string.string_add_movie_item_name))
        view_singer.setTitle(resources.getString(R.string.string_add_movie_item_release_time))
        view_album.setTitle(resources.getString(R.string.string_add_movie_item_category))
    }

    override fun initData() {
        cacheData = intent.getParcelableExtra("data")
        imgName = UUID.randomUUID().toString()
        cacheData?.let {
            view_Desc.visibility = View.VISIBLE
            cardLayout.visibility = View.VISIBLE
            tv_un_add.visibility = View.GONE
            Glide.with(this)
                    .applyDefaultRequestOptions(RequestOptions()
                            .signature(ObjectKey(it.movie_poster)))
                    .load(it.movie_poster)
                    .into(iv_img)
            netCover = it.movie_poster
            movieName = it.movie_title
            movieCategory = AppTools.array2String(it.movie_type)
            releaseDate = it.released_date.replace("00:00:00", "")
            desc = it.movie_intro
            view_singer.setMsgCount(releaseDate)
            view_singer.setChildTextStatus(true)
            view_music.setMsgCount(movieName)
            view_music.setChildTextStatus(true)
            view_album.setMsgCount(movieCategory)
            view_album.setChildTextStatus(true)
            view_Desc.setMsgCount(desc)
            tvDelete.visibility = View.VISIBLE
            tv_push.isSelected = true
            request(0)
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        relative_cover.setOnClickListener {
            checkPermission()
        }
        view_music.setOnClickListener {
            startActivityForResult(Intent(this, AddItemNameActivity::class.java)
                    .putExtra("name", movieName)
                    .putExtra("title", "添加" + resources.getString(R.string.string_add_movie_item_name)), REQUEST_MOVIES_NAME)
        }
        view_singer.setOnClickListener {
            DialogDateEnter(this).setLastDate(releaseDate).setOnClickResultListener(object : DialogDateEnter.OnClickResultListener {
                override fun onResult(date: String) {
                    releaseDate = date
//                    view_singer.setMsgCount("已添加")
                    view_singer.setMsgCount(date)
                    view_singer.setChildTextStatus(true)
                    tv_push.isSelected = jude()
                }
            }).show()
        }
        view_album.setOnClickListener {
            startActivityForResult(Intent(this, AddItemNameActivity::class.java)
                    .putExtra("name", movieCategory)
                    .putExtra("count", 10)
                    .putExtra("title", "添加" + resources.getString(R.string.string_add_movie_item_category)), REQUEST_MOVIES_CATEGORY)
        }
        tv_push.setOnClickListener {
            if (it.isSelected) {
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
        view_Desc.setOnClickListener {
            startActivityForResult(Intent(this, AddItemNameActivity::class.java)
                    .putExtra("name", desc)
                    .putExtra("count", 500)
                    .putExtra("title", "编辑简介"), REQUEST_MOVIES_DESC)
        }
        tvDelete.setOnClickListener {
            DialogDeleteResource(this).setOnClickListener(View.OnClickListener {
                deleteItem()
            }).show()
        }
    }

    private fun jude(): Boolean {
        return (!TextUtils.isEmpty(coverPath) || !TextUtils.isEmpty(netCover)) && !TextUtils.isEmpty(movieName) && !TextUtils.isEmpty(movieCategory) && !TextUtils.isEmpty(releaseDate)
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
        startActivityForResult(Intent(this, AlbumActivity::class.java).putExtra("count", 1), REQUEST_MOVIES_ALBUM)
    }

    //删除词条资源类型，1=影片  2 书籍  3 唱歌
    private fun deleteItem() {
        transLayout.showProgress()
        OkClientHelper.delete(this, "admin/resource/1/${cacheData?.id}", FormBody.Builder()
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
                    finish()
                } else {
                    showToast(result.msg)
                }
            }
        })
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(this, "resources/1/${cacheData?.id}", OneDetailsData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as OneDetailsData
                        cacheData = result.data
                        Glide.with(this@AddMovieItemActivity)
                                .applyDefaultRequestOptions(RequestOptions()
                                        .signature(ObjectKey(result.data.movie_poster)))
                                .load(result.data.movie_poster)
                                .into(iv_img)
                        netCover = result.data.movie_poster
                        movieName = result.data.movie_title
                        movieCategory = AppTools.array2String(result.data.movie_type)
                        releaseDate = result.data.released_date.replace("00:00:00", "")
                        desc = result.data.movie_intro
                        view_singer.setMsgCount(releaseDate)
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
                        .add("resourceType", "15")
                        .add("resourceContent", "${TimeUtils.getInstance().parseFileTime(System.currentTimeMillis())}_${AppTools.getFileMD5(File(coverPath))}.png")
                        .add("needBaseUri", "1")
                        .build()
                OkClientHelper.post(this, "resource", formBody, QiniuStringData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as QiniuStringData
                        if (result.code == 0) {
                            LocalLogUtils.writeLog("Add movie :上传movie cover:${result.data}", System.currentTimeMillis())
                            AliLoadFactory(this@AddMovieItemActivity, result.data.end_point, result.data.bucket, result.data.oss, object : LoadStateListener {
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
                                        LocalLogUtils.writeLog("Add movie:传音封面失败 oss error", System.currentTimeMillis())
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
                        LocalLogUtils.writeLog("Add movie:传音封面失败 ${any.toString()}", System.currentTimeMillis())
                        transLayout.showContent()
                    }
                })
            }
        }
    }

    private fun adminUpload(imgCover: String, resourceUrl: String) {
        val formBody = FormBody.Builder()
        if (!TextUtils.isEmpty(imgCover))
            formBody.add("movieCoverUri", imgCover)
        if (releaseDate != cacheData?.released_date?.replace("00:00:00", ""))
            formBody.add("releasedDate", releaseDate)
        if (movieName != cacheData?.movie_title)
            formBody.add("movieName", movieName)
        if (movieCategory != AppTools.array2String(cacheData?.movie_type))
            formBody.add("movieType", movieCategory)
        if (desc != cacheData?.movie_intro)
            formBody.add("movieIntro", desc)
        if (formBody.build().size() == 0) {
            finish()
            return
        }
        formBody.add("token", SPUtils.getString(this, IConstant.ADMINTOKEN, ""))
        OkClientHelper.patch(this, "admin/resource/1/${cacheData?.id}", formBody.build(), BaseRepData::class.java, object : OkResponse {
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

    private fun upload(imgCover: String, resourceUrl: String) {
        val formBody = FormBody.Builder()
                .add("releasedDate", releaseDate)
                .add("movieName", movieName)
                .add("movieCoverUri", imgCover)
                .add("movieType", movieCategory)
                .build()
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.post(this, "user/${loginBean.user_id}/entries/1", formBody, ConfessionsData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as ConfessionsData
                if (result.code == 0) {
                    File(coverPath).delete()
                    showToast("添加完成")
                    val searchBean = BaseSearchBean()
                    searchBean.id = result.data.resource_id
                    searchBean.movie_poster = resourceUrl + imgCover
                    searchBean.movie_title = movieName
                    searchBean.movie_type = arrayOf(movieCategory)
                    searchBean.released_at = TimeUtils.string2Long(releaseDate)
                    EventBus.getDefault().post(AddResourceEvent(1, searchBean))
                    startActivity(Intent(this@AddMovieItemActivity, OneMovieDetailsActivity::class.java).putExtra("id", result.data.resource_id))
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_MOVIES_NAME -> {
                    movieName = data?.getStringExtra("name")
                    view_music.setMsgCount(movieName)
                    view_music.setChildTextStatus(true)
                    tv_push.isSelected = jude()
                }
                REQUEST_MOVIES_ALBUM -> {
                    data?.let {
                        val result = it.getSerializableExtra("result") as ArrayList<String>
                        if (result != null && result.size > 0) {
                            startActivityForResult(Intent(this, AddItemCoverActivity::class.java)
                                    .putExtra("rate", 0.72f)
                                    .putExtra("name", imgName)
                                    .putExtra("path", result[0]), REQUEST_MOVIES_CROP)
                        }
                    }
                }
                REQUEST_MOVIES_CATEGORY -> {
                    movieCategory = data?.getStringExtra("name")
                    view_album.setMsgCount(movieCategory)
                    view_album.setChildTextStatus(true)
                    tv_push.isSelected = jude()
                }
                REQUEST_MOVIES_CROP -> {
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
                REQUEST_MOVIES_DESC -> {
                    data?.let {
                        desc = it.getStringExtra("name")
                        view_Desc.setMsgCount("已添加")
                    }
                }
            }
        }
    }
}