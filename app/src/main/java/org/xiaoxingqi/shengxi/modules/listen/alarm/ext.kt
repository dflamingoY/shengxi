package org.xiaoxingqi.shengxi.modules.listen.alarm

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.http.GlideJudeUtils
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.core.http.VolleyErrorHelper
import org.xiaoxingqi.shengxi.dialog.DialogNormalReport
import org.xiaoxingqi.shengxi.model.*
import org.xiaoxingqi.shengxi.model.alarm.BaseAlarmBean
import org.xiaoxingqi.shengxi.model.alarm.WordingData
import org.xiaoxingqi.shengxi.modules.getDownFilePath
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity
import org.xiaoxingqi.shengxi.utils.*
import org.xiaoxingqi.shengxi.wedgit.TransLayout
import org.xiaoxingqi.shengxi.wedgit.smallHeart.SmallBang
import java.io.File
import java.io.IOException

fun BaseAlarmBean.checkUser(userInfo: UserInfoData): BaseAlarmBean {
    if (from_user_info == null) {
        if (from_user_id == userInfo.data.user_id) {
            isSelf = true
            from_user_info = BaseUserBean().apply {
                nick_name = userInfo.data.nick_name
                avatar_url = userInfo.data.avatar_url
                id = userInfo.data.user_id
                identity_type = userInfo.data.identity_type
            }
        } else {
            from_user_info = BaseUserBean().apply {
                nick_name = "匿名"
                id = this@checkUser.user_id
            }
        }
    }
    return this
}

fun BaseAlarmBean.checkExist(): BaseAlarmBean {
    val file = File(Environment.getExternalStorageDirectory(), "${IConstant.DOCNAME}/${IConstant.CACHE_NAME}/${IConstant.DOWNAUDIO}/${from_user_info.nick_name} ${AppTools.getSuffix(dubbing_url)}")
    isDownload = file.exists()
    isDownCached = file.exists()
    if (!file.exists()) {//兼容4.2.2 之前的版本
        val file1 = File(Environment.getExternalStorageDirectory(), "${IConstant.DOCNAME}/${IConstant.CACHE_NAME}/${IConstant.DOWNAUDIO}/${from_user_info.nick_name} ${AppTools.getCompatibleSuffix(dubbing_url)}")
        isDownload = file1.exists()
        isDownCached = file1.exists()
    }
    return this
}

fun BaseAlarmBean.checkWord(userInfo: UserInfoData): BaseAlarmBean {
    if (is_anonymous == "1") {
        if (user_id == userInfo.data.user_id) {
            user = BaseUserBean().apply {
                avatar_url = userInfo.data.avatar_url
                nick_name = userInfo.data.nick_name
                id = userInfo.data.user_id
                identity_type = userInfo.data.identity_type
            }
        }
    }
    return this
}

fun BaseAlarmBean.show(helper: BaseAdapterHelper, context: Context, glideUtil: GlideJudeUtils, function: () -> Unit) {
    helper.getView(R.id.iv_Privacy).visibility = if (isDownload) View.GONE else View.VISIBLE
    helper.getView(R.id.tv_download).visibility = if (isDownload) View.VISIBLE else View.GONE
    try {
        helper.getTextView(R.id.tvTime).text = TimeUtils.getInstance().paserFriends(context, created_at.toInt())
    } catch (e: Exception) {
    }
    try {
        if (line?.let {
                    helper.getTextView(R.id.tv_alarm_word).text = "#${line.tag_name}#${line.line_content}"
                    helper.getTextView(R.id.tv_dubbing_count).text = if (line.dubbing_num > "0") "${line.dubbing_num}" else "抢先配音"
                    it
                } == null) {
            helper.getTextView(R.id.tv_alarm_word).text = line_content
        }
    } catch (e: Exception) {
    }
    if (is_anonymous == "1") {
        function()
    } else {
        try {
            glideUtil.loadGlide(from_user_info.avatar_url, helper.getImageView(R.id.roundImg).apply {
                setOnClickListener {
                    context.startActivity(Intent(context, UserDetailsActivity::class.java).putExtra("id", from_user_info.id))
                }
            }, 0, glideUtil.getLastModified(from_user_info.avatar_url))
            helper.getTextView(R.id.tv_UserName).text = from_user_info.nick_name
            helper.getView(R.id.iv_user_type).visibility = if (from_user_info.identity_type == 0) View.GONE else View.VISIBLE
            helper.getView(R.id.iv_user_type).isSelected = from_user_info.identity_type == 1
        } catch (e: Exception) {
            function()
        }
        try {
            helper.getView(R.id.ivOfficial).visibility = if (from_user_info.id == "1") View.VISIBLE else View.GONE
        } catch (e: Exception) {
        }
    }
    changeSelected(helper)
    if (!TextUtils.isEmpty(vote_id))
        when (vote_option) {
            "1" -> helper.getView(R.id.iv_angel).isSelected = true
            "2" -> helper.getView(R.id.iv_monster).isSelected = true
            "3" -> helper.getView(R.id.iv_god).isSelected = true
        }
}

private fun changeSelected(helper: BaseAdapterHelper) {
    helper.getView(R.id.iv_angel).isSelected = false
    helper.getView(R.id.iv_monster).isSelected = false
    helper.getView(R.id.iv_god).isSelected = false
}

/**
 * @param function String 本地路径 Int 操作类型 0成功, 1报异常
 */
fun Activity.downPlay(url: String, function: (path: String, Int) -> Unit) {
    val file = getDownFilePath(url)
    if (file.exists()) {
        function(file.absolutePath, 0)
    } else {
        OkClientHelper.downFile(this, url, { o ->
            try {
                if (null == o) {
                    Toast.makeText(this, resources.getString(R.string.string_error_file), Toast.LENGTH_SHORT).show()
                    return@downFile
                }
                function(o, 0)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }, {
            //网络请求报错 执行重试, 或者下一首
//            Toast.makeText(this, VolleyErrorHelper.getMessage(it), Toast.LENGTH_SHORT).show()
            function("", 1)
        })
    }
}

fun Activity.downFile(bean: BaseAlarmBean, name: String, function: () -> Unit) {
    try {
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.VOICENAME + "/" + AppTools.getSuffix(bean.dubbing_url))
        val downAudioFile = File(Environment.getExternalStorageDirectory(), "${IConstant.DOCNAME}/${IConstant.CACHE_NAME}/${IConstant.DOWNAUDIO}")
        if (!downAudioFile.exists())
            downAudioFile.mkdirs()
        if (file.exists()) {//移动文件 用户名+当前时间
            copyFile(file, File(Environment.getExternalStorageDirectory(), "${IConstant.DOCNAME}/${IConstant.CACHE_NAME}/${IConstant.DOWNAUDIO}/$name"))
            bean.isDownload = true
            Toast.makeText(this, "配音已下载，可在闹钟「Tab」设为闹钟", Toast.LENGTH_SHORT).show()
            function()
        } else {//下载文件
            OkClientHelper.downFile(this, bean.dubbing_url, {
                if (TextUtils.isEmpty(it)) {
                    Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show()
                } else {
                    copyFile(File(it), File(Environment.getExternalStorageDirectory(), "${IConstant.DOCNAME}/${IConstant.CACHE_NAME}/${IConstant.DOWNAUDIO}/$name"))
                    bean.isDownload = true
                    Toast.makeText(this, "配音已下载，可在闹钟「Tab」设为闹钟", Toast.LENGTH_SHORT).show()
                    function()
                }
            }, {
                Toast.makeText(this, VolleyErrorHelper.getMessage(it), Toast.LENGTH_SHORT).show()
            })
        }
    } catch (e: Exception) {

    }
}

private fun copyFile(resourceFile: File, targetFile: File) {
    FileUtils.copyFile(resourceFile, targetFile)
}

fun BaseAlarmBean.deleteFile(function: () -> Unit) {
    File(Environment.getExternalStorageDirectory(), "${IConstant.DOCNAME}/${IConstant.CACHE_NAME}/${IConstant.DOWNAUDIO}/${from_user_info.nick_name} ${AppTools.getSuffix(dubbing_url)}").let {
        if (it.exists()) {
            it.delete()
        }
        isDownload = false
        function()
    }
}

/**
 * 提交下载记录
 */
fun Activity.downloadLog(id: String) {
    OkClientHelper.post(this, "dubbings/downloadlog", FormBody.Builder().add("dubbingId", id).build(), BaseRepData::class.java, object : OkResponse {
        override fun success(result: Any?) {

        }

        override fun onFailure(any: Any?) {

        }
    }, "V4.1")
}

fun Activity.editDub(bean: BaseAlarmBean, transLayout: TransLayout?, function: (item: BaseAlarmBean) -> Unit) {
    transLayout?.showProgress()
    OkClientHelper.patch(this, "dubbings/${bean.id}", FormBody.Builder().add("isAnonymous", if (bean.is_anonymous == "1") "0" else "1").build(), BaseRepData::class.java, object : OkResponse {
        override fun success(result: Any?) {
            if ((result as BaseRepData).code == 0) {
                //hide user info
                bean.is_anonymous = if (bean.is_anonymous == "1") "0" else "1"
                function(bean)
            } else {
                showToast(result.msg)
            }
            transLayout?.showContent()
        }

        override fun onFailure(any: Any?) {
            transLayout?.showContent()
        }
    }, "V4.1")
}

fun Activity.deleteDub(bean: BaseAlarmBean, transLayout: TransLayout?, function: (item: BaseAlarmBean) -> Unit) {
    transLayout?.showProgress()
    OkClientHelper.delete(this, "dubbings/${bean.id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
        override fun success(result: Any?) {
            if ((result as BaseRepData).code == 0) {
                function(bean)
            } else {
                showToast(result.msg)
            }
            transLayout?.showContent()
        }

        override fun onFailure(any: Any?) {
            transLayout?.showContent()
        }
    }, "V4.1")
}

/**
 * 投票
 */
fun Activity.artVote(bean: BaseAlarmBean, transLayout: TransLayout?, view: View, type: Int, function: (view: View, bean: BaseAlarmBean) -> Unit) {
    transLayout?.showProgress()
    OkClientHelper.post(this, "dubbingsVote", FormBody.Builder()
            .add("dubbingId", bean.id)
            .add("voteOption", "$type").build(), IntegerRespData::class.java, object : OkResponse {
        override fun success(result: Any?) {
            result as IntegerRespData
            if (result.code == 0) {
                if (!TextUtils.isEmpty(bean.vote_id)) {
                    when (bean.vote_option) {
                        "1" -> {
                            bean.vote_option_one -= 1
                            view.findViewById<ImageView>(R.id.iv_angel).isSelected = false
                        }
                        "2" -> {
                            bean.vote_option_two -= 1
                            view.findViewById<ImageView>(R.id.iv_monster).isSelected = false
                        }
                        "3" -> {
                            bean.vote_option_three -= 1
                            view.findViewById<ImageView>(R.id.iv_god).isSelected = false
                        }
                    }
                }
                bean.vote_id = result.data.id.toString()
                val bangView = when (type) {
                    1 -> {
                        bean.vote_option_one += 1
                        view.findViewById<ImageView>(R.id.iv_angel).apply { isSelected = true }
                    }
                    2 -> {
                        bean.vote_option_two += 1
                        view.findViewById<ImageView>(R.id.iv_monster).apply { isSelected = true }
                    }
                    3 -> {
                        bean.vote_option_three += 1
                        view.findViewById<ImageView>(R.id.iv_god).apply { isSelected = true }
                    }
                    else -> view
                }
                function(view, bean)
                bean.vote_option = type.toString()
                SmallBang.attach2Window(this@artVote).bang(bangView, 60f, null)
            } else {
                showToast(result.msg)
            }
            transLayout?.showContent()
        }

        override fun onFailure(any: Any?) {
            transLayout?.showContent()
        }
    }, "V4.1")
}

//删除投票
fun Activity.deleteVote(item: BaseAlarmBean, transLayout: TransLayout?, view: View, voteType: Int, function: (view: View, bean: BaseAlarmBean) -> Unit) {
    transLayout?.showProgress()
    OkClientHelper.delete(this, "dubbingsVote/${item.vote_id}", FormBody.Builder().build(), BaseRepData::class.java, object : OkResponse {
        override fun success(result: Any?) {
            if ((result as BaseRepData).code == 0) {
                item.vote_id = null
                val bangView = when (voteType) {
                    1 -> {
                        item.vote_option_one--
                        view.findViewById<View>(R.id.iv_angel).apply { isSelected = false }
                    }
                    2 -> {
                        item.vote_option_two--
                        view.findViewById<View>(R.id.iv_monster).apply { isSelected = false }
                    }
                    3 -> {
                        item.vote_option_three--
                        view.findViewById<View>(R.id.iv_god).apply { isSelected = false }
                    }
                    else -> view
                }
                function(view, item)
                SmallBang.attach2Window(this@deleteVote).bang(bangView, 60f, null)
            } else {
                showToast(result.msg)
            }
            transLayout?.showContent()
        }

        override fun onFailure(any: Any?) {
            transLayout?.showContent()
        }
    }, "V4.1")
}


fun DialogNormalReport.show(function: (type: String) -> Unit) {
    setOnClickListener(View.OnClickListener {
        when (it.id) {
            R.id.tv_Attach -> function("1")
            R.id.tv_Porn -> function("2")
            R.id.tv_Junk -> function("3")
            R.id.tv_illegal -> function("4")
        }
    }).show()
}

//遍历获取数据
fun <T> List<T>.loop(function: (item: T) -> Boolean): T? {
    if (size == 0) {
        return null
    }
    forEach {
        if (function(it)) {
            return it
        }
    }
    return null
}

fun Activity.getTotalPeople(url: String, function: (IntegerRespData) -> Unit) {
    OkClientHelper.get(this, url, IntegerRespData::class.java, object : OkResponse {
        override fun success(result: Any?) {
            result as IntegerRespData
            function(result)
        }

        override fun onFailure(any: Any?) {

        }
    }, "V4.2")
}

private fun Activity.showToast(msg: String) {
    if (!TextUtils.isEmpty(msg))
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

//获取某个榜单上的数据
fun Activity.getBangWord(userId: String, ext: String, action: (AlarmUserBangCountData) -> Unit) {
    OkClientHelper.get(this, "leaderboard/${userId}/lines?type=${ext}", AlarmUserBangCountData::class.java, object : OkResponse {
        override fun onFailure(any: Any?) {

        }

        override fun success(result: Any?) {
            action(result as AlarmUserBangCountData)
        }
    }, "V4.3")
}

fun Activity.getBangUserDubbing(userId: String, ext: String, action: (result: AlarmUserBangCountData) -> Unit) {
    OkClientHelper.get(this, "leaderboard/${userId}/dubbings?type=$ext", AlarmUserBangCountData::class.java, object : OkResponse {
        override fun success(result: Any?) {
            action(result as AlarmUserBangCountData)
        }

        override fun onFailure(any: Any?) {

        }
    }, "V4.3")
}

fun Activity.executeRequest(url: String, action: (Any?) -> Unit) {
    OkClientHelper.get(this, "$url", WordingData::class.java, object : OkResponse {
        override fun success(result: Any?) {
            action(result)
        }

        override fun onFailure(any: Any?) {

        }
    }, "V4.3")


}





