package org.xiaoxingqi.shengxi.modules.publicmoudle

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.ObjectAnimator
import kotlinx.android.synthetic.main.activity_album.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.model.FilePath
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.DataHelper
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.wedgit.CustomCheckImageView
import java.io.File
import java.io.FilenameFilter

const val REQUESTPERMSSIONCODE = 0x00
const val REQUESTPERMISSCAMERA = 0x01
const val CAMARECODE = 0x02
const val REQUEST_PRE = 0x03

class AlbumActivity : BaseAct() {

    private lateinit var contentAdapter: QuickAdapter<String>
    private val data by lazy {
        ArrayList<String>()
    }
    private val selected by lazy {
        ArrayList<String>()
    }
    private var allowCount = 3
    private var isLock = false
    private var synac: AsyncTask<Void, Void, ArrayList<String>>? = null
    private var mFile: File? = null
    private val pathList = ArrayList<FilePath>()
    private lateinit var fileAdapter: QuickAdapter<FilePath>

    override fun getLayoutId(): Int {
        return R.layout.activity_album
    }

    override fun initView() {
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        ObjectAnimator.ofFloat(cardLayout_recycler, "translationY", -AppTools.dp2px(this, 467).toFloat()).setDuration(0).start()
    }

    override fun initData() {
        if (intent.getBooleanExtra("isChat", false)) {
            relative_Bottom.visibility = View.GONE
        }
        data.add("相机")
        allowCount = intent.getIntExtra("count", 3)
        tvSelectedCount.text = resources.getString(R.string.string_max_picture).replace("3", allowCount.toString())
        contentAdapter = object : QuickAdapter<String>(this, R.layout.item_album, data) {
            override fun convert(helper: BaseAdapterHelper?, item: String?) {
                helper?.getView(R.id.checkAlbum)?.visibility = if ("相机" == item) View.GONE else View.VISIBLE
                if ("相机" == item) {
                    helper?.getImageView(R.id.iv_album)?.setImageResource(R.mipmap.btn_camera)
                } else
                    Glide.with(this@AlbumActivity)
                            .asBitmap()
                            .load(item)
                            .apply(RequestOptions().centerCrop().override(180))
                            .into(helper!!.getImageView(R.id.iv_album))
                helper!!.getView(R.id.checkAlbum).isSelected = selected.contains(item)
                helper.getView(R.id.viewCover).visibility = if (isLock and !selected.contains(item)) View.VISIBLE else View.GONE
                val checkImageView = helper.getView(R.id.checkAlbum) as CustomCheckImageView
                checkImageView.setOnClickListener {
                    if (!selected.contains(item)) {
                        if (selected.size >= allowCount) {
                            return@setOnClickListener
                        }
                        selected.add(item!!)
                    } else {
                        if (selected.contains(item))
                            selected.remove(item)
                    }
                    checkImageView.isSelected = !checkImageView.isSelected
                    changeBtnInfo()
                }
            }
        }
        recyclerView.adapter = contentAdapter
        fileAdapter = object : QuickAdapter<FilePath>(this, R.layout.item_album_file, pathList) {
            override fun convert(helper: BaseAdapterHelper?, item: FilePath?) {
                Glide.with(this@AlbumActivity)
                        .asBitmap()
                        .load(item!!.firstPath)
                        .into(helper!!.getImageView(R.id.iv_img))
                helper.getTextView(R.id.tv_name).text = item.name
                helper.getTextView(R.id.tv_count).text = "${item.count}"

                val position = helper.itemView.tag as Int
                if (position == mCurrent) {
                    helper.getView(R.id.view_point).visibility = View.VISIBLE
                    helper.itemView.isSelected = true
                } else {
                    helper.getView(R.id.view_point).visibility = View.GONE
                    helper.itemView.isSelected = false
                }
            }
        }
        recycler_dir.layoutManager = LinearLayoutManager(this)
        recycler_dir.adapter = fileAdapter
        /*请求权限*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUESTPERMSSIONCODE)
            } else {
                getDir()
            }
        } else {
            getDir()
        }
    }

    private var mCurrent = 0
    @SuppressLint("SetTextI18n")
    override fun initEvent() {
        contentAdapter.setOnItemClickListener { _, position ->
            if (position == 0 && data[position] == "相机") {
                /**
                 * 打开相机
                 */
                openCarema()
                return@setOnItemClickListener
            }
            /**
             * 打开图片的预览
             */
            DataHelper.getInstance().addData(data)
            startActivityForResult(Intent(this, PreAlbumActivity::class.java)
                    .putExtra("selects", selected)
                    .putExtra("current", if (mCurrent == 0) position - 1 else position)
                    .putExtra("all", allowCount), REQUEST_PRE)
        }
        tv_Commit.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent().putExtra("result", selected))
            finish()
        }
        btn_Back.setOnClickListener { finish() }
        fileAdapter.setOnItemClickListener { _, position ->
            if (position == mCurrent)
                return@setOnItemClickListener
            data.clear()
            mCurrent = position
            if (position == 0) {
                data.add("相机")
                getDir()
            } else {
                try {
                    getFileList(pathList[position].path)
                } catch (e: Exception) {
                }
                fileAdapter.notifyDataSetChanged()
            }
            anim()
        }
        tv_title.setOnClickListener {
            anim()
        }
        view_layer.setOnClickListener {
            anim()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun changeBtnInfo() {
        if (selected.size <= 0) {
            tv_Commit.text = resources.getString(R.string.string_Next_step)
            tv_Commit.isSelected = false
            tv_Commit.setTextColor(resources.getColor(R.color.colorTextGray))
        } else {
            tv_Commit.text = "${resources.getString(R.string.string_Next_step)}（" + selected.size + "）"
            tv_Commit.isSelected = true
            tv_Commit.setTextColor(Color.WHITE)
        }
        if (selected.size == allowCount) {//如果达到最大数量 ,则显示不可点
            isLock = true
            contentAdapter.notifyDataSetChanged()
        } else {
            if (isLock) {
                isLock = false
                contentAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * 文件夹列表的动画
     */
    private var isOpen = false
    private var isRunning = false
    private fun anim() {
        if (!isRunning) {
            isRunning = true
            isOpen = !isOpen
            val animator = ObjectAnimator.ofFloat(cardLayout_recycler, "translationY", if (isOpen) 0f else -AppTools.dp2px(this, 467).toFloat()).setDuration(300)
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    view_layer.visibility = if (isOpen) View.VISIBLE else View.GONE
                    isRunning = false
                }
            })
            animator.start()
            ObjectAnimator.ofFloat(iv_arrow, "rotation", if (isOpen) 0f else 180f).setDuration(200).start()
        }
    }

    /**
     * 检测权限
     */
    private fun openCarema() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUESTPERMISSCAMERA)
            } else {
                camera()
            }
        } else {
            camera()
        }
    }

    /**
     * 打开相机
     */
    private fun camera() {
        try {
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            // 判断存储卡是否可以用，可用进行存储
            if (AppTools.hasSdcard()) {
                val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME)
                if (!file.exists()) {
                    file.mkdirs()
                }
                mFile = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.CACHE_NAME + "/" + System.currentTimeMillis() + ".png")
                val uri: Uri?
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(
                            this,
                            "$packageName.fileprovider",
                            mFile!!)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    uri = Uri.fromFile(mFile)
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                startActivityForResult(intent, CAMARECODE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUESTPERMSSIONCODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDir()
            }
        } else if (requestCode == REQUESTPERMISSCAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                camera()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMARECODE) {
                try {

                    //在手机相册中显示刚拍摄的图片
//                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
//                    val contentUri = Uri.fromFile(mFile)
//                    mediaScanIntent.data = contentUri
//                    sendBroadcast(mediaScanIntent)
                    val insertImage = MediaStore.Images.Media.insertImage(
                            contentResolver,
                            mFile!!.absolutePath,
                            "",
                            "")
                    insertImage?.let {
                        val path = AppTools.getPath(Uri.parse(it), this)
                        this@AlbumActivity.data.add(1, path)
                        contentAdapter.notifyDataSetChanged()
                        mFile!!.delete()
                    }
                } catch (e: Exception) {

                }
            } else if (requestCode == REQUEST_PRE) {
                (data?.getSerializableExtra("selects") as ArrayList<String>)?.let {
                    setResult(Activity.RESULT_OK, Intent().putExtra("result", it))
                    finish()
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            if (requestCode == REQUEST_PRE) {
                try {
                    (data?.getSerializableExtra("selects") as ArrayList<String>)?.let {
                        selected.clear()
                        selected.addAll(it)
                        contentAdapter.notifyDataSetChanged()
                        changeBtnInfo()
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    /**
     * 获取所有文件夹
     * 并获取所有的本地图片
     */
    @SuppressLint("StaticFieldLeak")
    @Synchronized
    fun getDir() {
        pathList.clear()
        synac = object : AsyncTask<Void, Void, ArrayList<String>>() {
            override fun doInBackground(vararg params: Void?): ArrayList<String> {
                val data = arrayListOf<String>()
                try {
                    val tempFileList = arrayListOf<String>()
                    val imageUrl = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    val resolver = this@AlbumActivity.contentResolver
                    val cursor = resolver.query(imageUrl, null, MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                            arrayOf("image/jpeg", "image/png", "image/gif"), MediaStore.Images.Media.DATE_MODIFIED)
                    if (null != cursor && cursor.count > 0) {
                        while (cursor.moveToNext()) {
                            val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                            try {
                                if (File(path).length() < 5000)
                                    continue
                            } catch (e: Exception) {
                                continue
                            }
                            data.add(0, path)
                            val parentFile = File(path).parentFile ?: continue
                            val parentPath = parentFile.absolutePath
                            var filePath: FilePath
                            if (tempFileList.contains(parentPath)) {//已经包含了文件夹
                                continue
                            } else {
                                filePath = FilePath(parentPath, path)
                                filePath.name = parentPath.substring(parentPath.lastIndexOf("/") + 1)
                                tempFileList.add(parentPath)
                            }
                            val fileFilter = FilenameFilter { _, name -> name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".jpg") || name.endsWith("jpeg") }
                            val count = try {
                                parentFile.list(fileFilter).size
                            } catch (e: Exception) {
                                0
                            }
                            filePath.count = count
                            pathList.add(filePath)
                        }
                    }
                    cursor!!.close()
                } catch (e: Exception) {
                }
                return data
            }

            override fun onPostExecute(result: ArrayList<String>?) {
                super.onPostExecute(result)
                transLayout.showContent()
                result?.let {
                    if (it.size > 0) {
                        linear_album.visibility = View.VISIBLE
                        val filePath = FilePath()
                        filePath.name = "全部图片"
                        filePath.firstPath = it[0]
                        filePath.count = it.size
                        filePath.isSelected = true
                        pathList.add(0, filePath)
                        data.addAll(it)
                    }
                    contentAdapter.notifyDataSetChanged()
                    fileAdapter.notifyDataSetChanged()
                }
            }
        }.execute()
    }

    /**
     * 根据路径获取图片
     */
    @SuppressLint("StaticFieldLeak")
    private fun getFileList(dir: String) {
        object : AsyncTask<Void, Void, Array<String>>() {
            override fun doInBackground(vararg voids: Void): Array<String>? {
                val file = File(dir)
                if (file.isDirectory) {
                    val filter = FilenameFilter { _, s -> s.endsWith("png") || s.endsWith("jpg") || s.endsWith("jpeg") || s.endsWith("gif") }
                    return file.list(filter)
                }
                return null
            }

            override fun onPostExecute(strings: Array<String>?) {
                super.onPostExecute(strings)
                transLayout.showContent()
                if (strings != null) {
                    linear_album.visibility = View.VISIBLE
                    for (str in strings)
                        data.add("$dir/$str")
                    contentAdapter.notifyDataSetChanged()
                } else {
                    linear_album.visibility = View.GONE
                }
            }
        }.execute()

    }

    override fun onDestroy() {
        super.onDestroy()
        synac?.cancel(true)
    }
}