package org.xiaoxingqi.shengxi.modules.user

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_user_avatar.*
import okhttp3.FormBody
import org.greenrobot.eventbus.EventBus
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogSelectPhoto
import org.xiaoxingqi.shengxi.impl.UpdateFriendInfoEvent
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.modules.publicmoudle.AlbumActivity
import org.xiaoxingqi.shengxi.utils.*
import java.io.File
import java.util.ArrayList
import java.util.concurrent.ExecutionException

const val REQUEST_CAMERA = 0x01
const val REQUEST_PHOTO = 0x02
const val PERMISSIONCAMERA = 0x03
const val REQUESTCROP = 0x04

class UserAvatarActivity : BaseAct() {
    private var mFile: File? = null
    private var path: String? = ""
    private var isHide = false
    override fun getLayoutId(): Int {
        return R.layout.activity_user_avatar
    }

    override fun initView() {

    }

    override fun initData() {
        touchImg.isEnabled = false
        path = intent.getStringExtra("img")
        isHide = intent.getBooleanExtra("isSelf", false)
        var url = path
        if (path?.contains("?")!!) {
            url = path?.substring(0, path?.lastIndexOf("?")!!)
        }
        glideUtil.loadGlide(url, touchImg, 0, glideUtil.getLastModified(url))
    }

    override fun initEvent() {
        iv_Other.setOnClickListener {
            DialogSelectPhoto(this).hideAction(!isHide).setOnItemClick(object : DialogSelectPhoto.OnItemClick {
                override fun itemView(view: View) {
                    when (view.id) {
                        R.id.tv_Camera -> {
                            val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + IConstant.IMG_CACHE)
                            if (!file.exists()) {
                                file.mkdirs()
                            }
                            mFile = File(file.absolutePath + "/" + System.currentTimeMillis() + ".png")
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (ContextCompat.checkSelfPermission(this@UserAvatarActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(this@UserAvatarActivity, arrayOf(Manifest.permission.CAMERA), PERMISSIONCAMERA)
                                } else {
                                    AppTools.openCamera(this@UserAvatarActivity, REQUEST_CAMERA, mFile)
                                }
                            } else {
                                AppTools.openCamera(this@UserAvatarActivity, REQUEST_CAMERA, mFile)
                            }
                        }
                        R.id.tv_Album -> {
                            startActivityForResult(Intent(this@UserAvatarActivity, AlbumActivity::class.java)
                                    .putExtra("isChat", true)
                                    .putExtra("count", 1), REQUEST_PHOTO)
                        }
                        R.id.tv_Save -> {
                            path?.let { save() }
                        }
                    }
                }
            }).show()
        }
        btn_Back.setOnClickListener { finish() }
    }

    private fun save() {
        val path_ = path
        var end = ""
        if (!path_!!.contains(".jpg") && !path_.contains(".png") && !path_.contains(".gif") && !path_.contains(".Jpeg")) {
            end = ".png"
        }
        val suffix = path_.substring(path_.lastIndexOf("/") + 1) + end
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD + "/" + suffix)
        transLayout.showProgress()
        object : AsyncTask<Void, Void, File>() {

            override fun doInBackground(vararg voids: Void): File? {
                try {
                    return Glide.with(this@UserAvatarActivity)
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
                    sendBroadcast(mediaScanIntent)
                    showToast("保存成功")
                }
                transLayout.showContent()
            }
        }.execute()
    }


    override fun request(flag: Int) {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        val formbody = FormBody.Builder()
                .add("avatarUri", path)
                .add("bucketId", AppTools.bucketId)
                .build()
        OkClientHelper.patch(this, "users/${loginBean.user_id}", formbody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                /**
                 * 查询数据 更新信息
                 */
                if ((result as BaseRepData).code == 0) {
                    EventBus.getDefault().post(UpdateFriendInfoEvent(loginBean.user_id, 3, null, null, null))
                }
                queryInfo()
            }

            override fun onFailure(any: Any?) {

            }
        })
    }


    private fun queryInfo() {
        val loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        OkClientHelper.get(this, "users/${loginBean.user_id}", UserInfoData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as UserInfoData
                if (result.code == 0) {
                    PreferenceTools.saveObj(this@UserAvatarActivity, IConstant.USERCACHE, result)
                    var url = result.data.avatar_url
                    if (url?.contains("?")!!) {
                        url = url?.substring(0, url?.lastIndexOf("?")!!)
                    }
                    glideUtil.loadGlide(url, touchImg, 0, glideUtil.getLastModified(url))
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONCAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AppTools.openCamera(this@UserAvatarActivity, REQUEST_CAMERA, mFile)
            } else {

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> startActivityForResult(Intent(this@UserAvatarActivity, CropActivity::class.java).putExtra("path", mFile?.absolutePath), REQUESTCROP)
                REQUEST_PHOTO -> data?.let {
                    val result = it.getSerializableExtra("result") as ArrayList<String>
                    if (result != null && result.size > 0) {
                        startActivityForResult(Intent(this, CropActivity::class.java).putExtra("path", result[0]), REQUESTCROP)
                    }
                }
                REQUESTCROP -> data?.let {
                    path = it.getStringExtra("result")
                    request(0)
                }
            }
        }
    }

}