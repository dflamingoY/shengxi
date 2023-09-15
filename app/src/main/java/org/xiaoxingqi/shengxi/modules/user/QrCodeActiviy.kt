package org.xiaoxingqi.shengxi.modules.user

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.text.TextUtils
import android.view.View
import kotlinx.android.synthetic.main.activity_qrcode.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.dialog.DialogSelectPhoto
import org.xiaoxingqi.shengxi.model.PublicData
import org.xiaoxingqi.shengxi.model.UserInfoData
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.zxing.encoding.RxQRCode
import java.io.File
import java.io.FileOutputStream

class QrCodeActiviy : BaseAct() {
    private var userid: String? = null
    private var qrlink: String? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_qrcode
    }

    override fun initView() {

    }

    override fun initData() {
        intent.getParcelableExtra<UserInfoData.UserBean>("data")?.let {
            /* Glide.with(this)
                     .applyDefaultRequestOptions(RequestOptions().centerCrop().error(R.mipmap.icon_user_default).signature(ObjectKey(System.currentTimeMillis())))
                     .asBitmap()
                     .load(it.avatar_url)
                     .into(iv_img)*/
//            GlideUtil.load(this, it.avatar_url, iv_img, R.mipmap.icon_user_default)
            tv_UserName.text = it.nick_name
            tv_frequency_id.text = "${resources.getString(R.string.string_frequency)}${it.frequency_no}"
            userid = it.user_id
            request(0)
        }
    }

    override fun initEvent() {
        iv_Other.setOnClickListener {
            DialogSelectPhoto(this).hideAction(true).setOnItemClick(object : DialogSelectPhoto.OnItemClick {
                override fun itemView(view: View) {
                    when (view.id) {
                        R.id.tv_Save -> {
                            save(qrlink)
                        }
                    }
                }
            }).show()
        }
        btn_Back.setOnClickListener { finish() }
    }

    fun save(path: String?) {
        if (TextUtils.isEmpty(path)) {
            showToast("保存图片失败")
            return
        }
        val bootfile = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD)
        if (!bootfile.exists()) {
            bootfile.mkdirs()
        }
        val name = path!!.substring(path.lastIndexOf("/") + 1, path.length)
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD + "/" + name + ".jpg")
        if (file.exists()) {
            showToast("二维码已存在")
            return
        }
        transLayout.showProgress()
        object : AsyncTask<Void, Void, Boolean>() {
            override fun doInBackground(vararg voids: Void): Boolean? {
                try {
                    /*  frame_Qrcode.isDrawingCacheEnabled = true
                      frame_Qrcode.buildDrawingCache()
                      val stream = FileOutputStream(file, false)
                      val compress = frame_Qrcode.drawingCache.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                      stream.close()*/

                    val bitmap = Bitmap.createBitmap(frame_Qrcode.width, frame_Qrcode.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    frame_Qrcode.draw(canvas)
                    val stream = FileOutputStream(file, false)
                    val compress = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.close()
                    return compress
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return false
            }

            override fun onPostExecute(s: Boolean?) {
                super.onPostExecute(s)
                if (s == null) {
                    showToast("图片保存失败")
                } else {

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
        OkClientHelper.get(this, "users/$userid/qrlink", PublicData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                if ((result as PublicData).code == 0) {
                    qrlink = result.data.qrLink
                    RxQRCode.createQRCode(result.data.qrLink, iv_QrCode)
                }
            }

            override fun onFailure(any: Any?) {
            }
        })
    }

}
