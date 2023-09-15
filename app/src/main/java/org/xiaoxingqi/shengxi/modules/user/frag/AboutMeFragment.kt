package org.xiaoxingqi.shengxi.modules.user.frag

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.frag_about_me.view.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseFragment
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogSelectPhoto
import org.xiaoxingqi.shengxi.impl.ITabClickCall
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.listen.UserTopicManagerActivity
import org.xiaoxingqi.shengxi.modules.listen.book.BookActivity
import org.xiaoxingqi.shengxi.modules.listen.book.OneUserBookDetailsListActivity
import org.xiaoxingqi.shengxi.modules.listen.book.UserBookListActivity
import org.xiaoxingqi.shengxi.modules.listen.movies.MovieActivity
import org.xiaoxingqi.shengxi.modules.listen.movies.OneUserMoviesDetailsListActivity
import org.xiaoxingqi.shengxi.modules.listen.movies.UserMoviesActivity
import org.xiaoxingqi.shengxi.modules.listen.music.MusicActivity
import org.xiaoxingqi.shengxi.modules.listen.music.OneUserMusicDetailsListActivity
import org.xiaoxingqi.shengxi.modules.listen.music.UserMusicListActivity
import org.xiaoxingqi.shengxi.modules.user.*
import org.xiaoxingqi.shengxi.utils.*
import java.io.File
import java.util.concurrent.ExecutionException

class AboutMeFragment : BaseFragment(), ITabClickCall {
    override fun tabClick(isVisible: Boolean) {

    }

    override fun doubleClickRefresh() {

    }

    private var relation = 5//-1 表示自己   0陌生人 1 待验证 2 好友
    private val REQUEST_GALLEY = 0x01
    private val REQUEST_CROP_BG = 0x02
    private var userId: String? = null
    private var userInfo: UserInfoData? = null
    private var nickName: String = ""
    private var commentCount: AboutUserCommentData.AboutCommentBean? = null

    override fun getLayoutId(): Int {
        return R.layout.frag_about_me
    }

    override fun initView(view: View?) {
        view!!.tv_MoviesCount.isSelected = true
    }

    override fun initData() {
//        userId = (activity as UserHomeActivity).uid
        request(0)
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (loginBean.user_id == userId) {
            val character = SPUtils.getString(activity, IConstant.USERCHARACTERTYPE + loginBean.user_id, "I")
            if (IConstant.USER_EXTROVERT.equals(character, true)) {
                mView!!.tvEmptyTopic.text = resources.getString(R.string.string_about_2_e)
            } else {
                mView!!.tvEmptyTopic.text = resources.getString(R.string.string_about_2_i)
            }
        } else {
            request(2)
            request(5)
        }
    }

    override fun initEvent() {
        mView!!.iv_img.setOnClickListener {
            userInfo?.let {
                val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                DialogSelectPhoto(activity!!).hideAction(true).hindOther(loginBean.user_id == it.data.user_id).changeText(if (loginBean.user_id == it.data.user_id) "换头像" else "保存图片").setOnItemClick(object : DialogSelectPhoto.OnItemClick {
                    override fun itemView(view: View) {
                        when (view.id) {
                            R.id.tv_Save -> {
                                if (loginBean.user_id == it.data.user_id) {
                                    val intent: Intent
                                    if (Build.VERSION.SDK_INT < 19) {
                                        intent = Intent(Intent.ACTION_GET_CONTENT)
                                        intent.type = "image/*"
                                    } else {
                                        intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                                    }
                                    startActivityForResult(intent, REQUEST_GALLEY)
                                } else {
                                    if (TextUtils.isEmpty(it.data.avatar_url)) {
                                        /**
                                         * 没有图片
                                         */
                                        showToast("图片保存失敗")
                                    } else {//可以保存圖片
                                        if (it.data.avatar_url.contains("?"))
                                            save(it.data.avatar_url.substring(0, it.data.avatar_url?.lastIndexOf("?")!!))
                                        else {
                                            save(it.data.avatar_url)
                                        }
                                    }
                                }
                            }
                            R.id.tv_Other -> {
                                /**
                                 * 保存图片
                                 */
                                if (TextUtils.isEmpty(it.data.avatar_url)) {
                                    /**
                                     * 没有图片
                                     */
                                    showToast("图片保存失敗")
                                } else {  //可以保存圖片
                                    if (it.data.avatar_url.contains("?"))
                                        save(it.data.avatar_url.substring(0, it.data.avatar_url?.lastIndexOf("?")!!))
                                    else {
                                        save(it.data.avatar_url)
                                    }
                                }
                            }
                        }
                    }
                }).show()
            }
        }
        mView!!.tv_Setting.setOnClickListener {
            startActivity(Intent(activity, PrivacyActivity::class.java))
        }
        mView!!.tv_MoviesCount.setOnClickListener {
            if (!it.isSelected) {
                userInfo?.let { infoData ->
                    val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    if (infoData.data.user_id == loginBean.user_id) {
                        startActivity(Intent(activity, UserMoviesActivity::class.java)
                                .putExtra("uid", userId))
                    } else {
                        startActivity(Intent(activity, OneUserMoviesDetailsListActivity::class.java)
                                .putExtra("nickName", infoData.data.nick_name)
                                .putExtra("uid", userId)
                        )
                    }
                }
            } else {
                if (resources.getString(R.string.string_about_me_privacy_movies) != mView!!.tv_MoviesCount.text.toString().trim()) {
                    val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    if (userId == loginBean.user_id) {
                        startActivity(Intent(activity, MovieActivity::class.java))
                    } else
                        showToast(resources.getString(R.string.string_about_me_empty_movies))
                }
            }
        }
        mView!!.tv_BookCount.setOnClickListener {
            if (!it.isSelected) {
                userInfo?.let { infoData ->
                    val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    if (infoData.data.user_id == loginBean.user_id) {
                        startActivity(Intent(activity, UserBookListActivity::class.java)
                                .putExtra("uid", userId))
                    } else {
                        startActivity(Intent(activity, OneUserBookDetailsListActivity::class.java)
                                .putExtra("nickName", infoData.data.nick_name)
                                .putExtra("uid", userId)
                        )
                    }
                }
            } else {
                if (resources.getString(R.string.string_book_9) != mView!!.tv_BookCount.text.toString().trim()) {
                    val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    if (userId == loginBean.user_id) {
                        startActivity(Intent(activity, BookActivity::class.java))
                    } else
                        showToast(resources.getString(R.string.string_book_10))
                }
            }
        }
        mView!!.tv_MusicCount.setOnClickListener {
            if (!it.isSelected) {
                userInfo?.let { infoData ->
                    val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    if (infoData.data.user_id == loginBean.user_id) {
                        startActivity(Intent(activity, UserMusicListActivity::class.java)
                                .putExtra("uid", userId))
                    } else {
                        startActivity(Intent(activity, OneUserMusicDetailsListActivity::class.java)
                                .putExtra("nickName", infoData.data.nick_name)
                                .putExtra("uid", userId)
                        )
                    }
                }
            } else {
                if (resources.getString(R.string.string_music_4) != mView!!.tv_MusicCount.text.toString().trim()) {
                    val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                    if (userId == loginBean.user_id) {
                        startActivity(Intent(activity, MusicActivity::class.java))
                    } else
                        showToast(resources.getString(R.string.string_music_5))
                }
            }
        }
        mView!!.linearContainer.setOnItemClickListener { view, position ->
            if (null != topicData) {
                if (position < topicData!!.size)
                    startActivity(Intent(activity, UserTopicManagerActivity::class.java)
                            .putExtra("title", nickName)
                            .putExtra("tag", topicData!![position].topic_name)
                            .putExtra("uid", userId)
                            .putExtra("tagId", topicData!![position].topic_id))
            }
        }
    }

    private var topicData: List<SearchTopicData.SearchTopicBean>? = null

    override fun onResume() {
        super.onResume()
        userInfo?.let {
            it.data?.let {
                var url = it.avatar_url
                if (it.avatar_url?.contains("?")!!) {
                    url = it.avatar_url?.substring(0, it.avatar_url?.lastIndexOf("?")!!)
                }
                glideUtil.loadGlide(url, mView!!.iv_img, R.mipmap.icon_user_default, glideUtil.getLastModified(url))
            }
        }
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        if (loginBean.user_id == userId) {
            request(1)
            request(4)
            mView!!.tv_Setting.visibility = View.VISIBLE
        }
    }

    private fun update(formBody: FormBody) {
        val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.patch(activity, "users/${loginBean.user_id}", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as BaseRepData
                if (result.code == 0) {
                    /* userInfo?.let {
                         it.data?.let {
                             glideUtil.loadGlide(it.avatar_url, mView!!.iv_img, R.mipmap.icon_user_default, glideUtil.getLastModified(it.avatar_url))
                         }
                     }*/
                    request(0)
                } else {
                    showToast(result.msg)
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    override fun request(flag: Int) {
        when (flag) {
            0 -> {
                OkClientHelper.get(activity, "users/$userId/about", UserInfoData::class.java, object : OkResponse {
                    @SuppressLint("SetTextI18n")
                    override fun success(result: Any?) {
                        result as UserInfoData
                        if (result.code == 0) {
                            userInfo = result
                            if (result.data != null) {
                                mView!!.iv_user_type.visibility = if (result.data.identity_type == 0) View.GONE else View.VISIBLE
                                mView!!.iv_user_type.isSelected = result.data.identity_type == 1
                                nickName = result.data.nick_name
                                var url = result.data.avatar_url
                                if (result.data.avatar_url?.contains("?")!!) {
                                    url = result.data.avatar_url?.substring(0, result.data.avatar_url?.lastIndexOf("?")!!)
                                }
                                glideUtil.loadGlide(url, mView!!.iv_img, R.mipmap.icon_user_default, glideUtil.getLastModified(url))
                                mView!!.tv_UserName.text = result.data.nick_name
                                mView!!.tv_frequency_id.text = resources.getString(R.string.string_frequency) + result.data.frequency_no
                                if (result.data.friend_num == 0 && result.data.voice_total_len == 0) {
                                    mView!!.tv_UserSummary.text = "${resources.getString(R.string.string_empty_friends)} ${resources.getString(R.string.string_empty_voices)}"
                                } else if (result.data.friend_num > 0 && result.data.voice_total_len == 0) {
                                    mView!!.tv_UserSummary.text = Html.fromHtml(String.format(resources.getString(R.string.string_only_friend_summary), result.data.friend_num))
                                } else if (result.data.friend_num == 0 && result.data.voice_total_len > 0) {
                                    mView!!.tv_UserSummary.text = Html.fromHtml(String.format(resources.getString(R.string.string_only_voice_summary), TimeUtils.formatterS(activity, result.data.voice_total_len)))
                                } else {
                                    mView!!.tv_UserSummary.text = Html.fromHtml(String.format(resources.getString(R.string.string_user_summary), result.data.friend_num, TimeUtils.formatterS(activity, result.data.voice_total_len)))
                                }
                            }
                        } else {
                            showToast(result.msg)
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            1 -> {
                /**
                 * 查询用户的摘要数量, 非自己查看其它用户的配置信息
                 */
                OkClientHelper.get(activity, "review/users/$userId/summary", AboutUserCommentData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as AboutUserCommentData
                        if (result.code == 0) {
                            commentCount = result.data
                            val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                            if (userId == loginBean.user_id || relation == 2) {
                                mView!!.tv_MoviesCount.isSelected = result.data.film_num <= 0
                                mView!!.tv_BookCount.isSelected = result.data.book_num <= 0
                                mView!!.tv_MusicCount.isSelected = result.data.song_num <= 0

                                mView!!.tv_MoviesCount.text = "${resources.getString(R.string.string_Movies)}  ${result.data.film_num}"
                                mView!!.tv_BookCount.text = "${resources.getString(R.string.string_book)}  ${result.data.book_num}"
                                mView!!.tv_MusicCount.text = "${resources.getString(R.string.string_music_title)}  ${result.data.song_num}"
                            } else {
                                request(3)
                                request(6)
                                request(7)
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                }, "V3.2")
            }
            2 -> {//查询用户关系
                OkClientHelper.get(activity, "relations/$userId", RelationData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        /**
                         * 查询用户关系, 是否是好友
                         */
                        result as RelationData
                        if (result.code == 0) {
                            relation = result.data.friend_status
                            if (result.data != null) {
                                request(1)
                                if (result.data.friend_status == 2) {//好友
                                    request(4)
                                } else if (result.data.friend_status == 0 || result.data.friend_status == 1) {//无关系 待验证
//                                    request(3)
                                }
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {
                        if (AppTools.isNetOk(activity)) {
                            showToast(any.toString())
                        } else {
                        }
                    }
                })
            }
            3 -> {//查询电影的权限
                OkClientHelper.get(activity, "users/$userId/setting/filmReview?key=list", PatchData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as PatchData
                        if (result.code == 0) {
                            if (result.data.film_review.list == 1) {
                                mView!!.tv_MoviesCount.isSelected = true
                                mView!!.tv_MoviesCount.text = resources.getString(R.string.string_about_me_privacy_movies)
                            } else {
                                mView!!.tv_MoviesCount.isSelected = commentCount?.film_num!! <= 0
                                mView!!.tv_MoviesCount.text = "${resources.getString(R.string.string_Movies)}  ${commentCount?.film_num}"
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            4 -> {//查询话题
                mView!!.transLayout.showProgress()
                OkClientHelper.get(activity, "users/$userId/favorite/topic", PrivacyTopicData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as PrivacyTopicData
                        mView!!.linearContainer.removeAllViews()
//                        mView!!.linearTopic.visibility = View.VISIBLE
                        if (result.code == 0) {
                            if (result.data != null && result.data.size > 0) {
                                topicData = result.data
                                mView!!.tvEmptyTopic.visibility = View.GONE
                                for (bean in result.data) {
                                    val view = View.inflate(activity, R.layout.item_flow_topics, null)
                                    view.findViewById<TextView>(R.id.text_inf).text = "#${bean.topic_name}#"
                                    mView!!.linearContainer.addView(view)
                                }
                            } else {
                                val loginBean = PreferenceTools.getObj(activity, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
                                mView!!.tvEmptyTopic.visibility = View.VISIBLE
                                if (loginBean.user_id != userId) {
                                    mView!!.tvEmptyTopic.text = resources.getString(R.string.string_about_4)
                                }
                            }
                        }
                        mView!!.transLayout.showContent()
                    }

                    override fun onFailure(any: Any?) {
                        mView!!.transLayout.showContent()
                    }
                }, "V3.2")
            }
            5 -> {
                //查詢是否設置話題展示
                OkClientHelper.get(activity, "users/$userId/setting/favoriteTopic", PatchData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as PatchData
                        if (result.code == 0) {
                            if (result.data.favorite_topic == 1) {
                                mView!!.tvEmptyTopic.text = resources.getString(R.string.string_about_3)
//                                mView!!.linearTopic.visibility = View.GONE
                            } else {
                                request(4)
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            6 -> {
                //查询电影是否展示
                OkClientHelper.get(activity, "users/$userId/setting/bookReview?key=list", PatchData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as PatchData
                        if (result.code == 0) {
                            if (result.data.book_review.list == 1) {
                                mView!!.tv_BookCount.isSelected = true
                                mView!!.tv_BookCount.text = resources.getString(R.string.string_book_9)
                            } else {
                                mView!!.tv_BookCount.isSelected = commentCount?.book_num!! <= 0
                                mView!!.tv_BookCount.text = "${resources.getString(R.string.string_book)}  ${commentCount?.book_num}"
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
            7 -> {
                //查询唱回忆是否展示
                OkClientHelper.get(activity, "users/$userId/setting/songReview?key=list", PatchData::class.java, object : OkResponse {
                    override fun success(result: Any?) {
                        result as PatchData
                        if (result.code == 0) {
                            if (result.data.song_review.list == 1) {
                                mView!!.tv_MusicCount.isSelected = true
                                mView!!.tv_MusicCount.text = resources.getString(R.string.string_music_4)
                            } else {
                                mView!!.tv_MusicCount.isSelected = commentCount?.song_num!! <= 0
                                mView!!.tv_MusicCount.text = "${resources.getString(R.string.string_music_title)}  ${commentCount?.song_num}"
                            }
                        }
                    }

                    override fun onFailure(any: Any?) {

                    }
                })
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun save(path: String) {

        val path_ = path
        var end = ""
        if (!path_!!.contains(".jpg") && !path_.contains(".png") && !path_.contains(".gif") && !path_.contains(".Jpeg")) {
            end = ".png"
        }
        val suffix = System.currentTimeMillis().toString() + path_.substring(path_.lastIndexOf("/") + 1) + end
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD + "/" + suffix)
//        if (file.exists()) {
//            showToast("图片已存在")
//            return
//        }
        object : AsyncTask<Void, Void, File>() {

            override fun doInBackground(vararg voids: Void): File? {
                try {
                    return Glide.with(activity!!)
                            .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(System.currentTimeMillis().toString())))
                            .load(path)
                            .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }

                return null
            }

            override fun onPostExecute(s: File?) {
                if (s == null) {
                    showToast("图片保存失败")
                } else {
                    try {
                        val bootfile = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD)
                        if (!bootfile.exists()) {
                            bootfile.mkdirs()
                        }
                        val file = File(bootfile.absolutePath + "/" + suffix)
                        FileUtils.copyFile(s, file)
                        //在手机相册中显示刚拍摄的图片
                        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                        val contentUri = Uri.fromFile(file)
                        mediaScanIntent.data = contentUri
                        activity!!.sendBroadcast(mediaScanIntent)
                        showToast("保存成功")
                    } catch (e: Exception) {
                    }
                }
            }
        }.execute()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_GALLEY) {
                val selectedImage = data?.data
                val path = AppTools.getPath(selectedImage, activity)
                startActivityForResult(Intent(activity, CropActivity::class.java).putExtra("path", path), REQUESTCROP)
            } else if (requestCode == REQUESTCROP) {
                data?.let {
                    val path = it.getStringExtra("result")
                    update(FormBody.Builder()
                            .add("avatarUri", path)
                            .add("bucketId", AppTools.bucketId)
                            .build())
                }
            }
        }
    }
}