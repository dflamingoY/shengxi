package org.xiaoxingqi.shengxi.modules.listen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import android.webkit.*
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_action.*
import okhttp3.FormBody
import org.jsoup.Jsoup
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.adapter.BaseAdapterHelper
import org.xiaoxingqi.shengxi.core.adapter.QuickAdapter
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.ArgumentData
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.model.H5CommentData
import org.xiaoxingqi.shengxi.model.login.LoginData
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.FileUtils
import org.xiaoxingqi.shengxi.utils.IConstant
import org.xiaoxingqi.shengxi.utils.PreferenceTools
import org.xiaoxingqi.shengxi.wedgit.LoadContentWebView
import skin.support.SkinCompatManager
import java.io.File
import java.util.concurrent.ExecutionException


class ActionActivity : BaseAct() {
    private var url: String? = null
    private var isHtml = false
    private val REQUEST_COMMENT = 0x01
    private lateinit var adapter: QuickAdapter<H5CommentData.H5CommentBean>
    private var loginBean: LoginData.LoginBean? = null
    private val mData by lazy {
        ArrayList<H5CommentData.H5CommentBean>()
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_action
    }

    override fun initView() {

    }

    override fun initData() {
        loginBean = PreferenceTools.getObj(this, IConstant.LOCALTOKEN, LoginData.LoginBean::class.java)
        isHtml = intent.getBooleanExtra("isHtml", false)
        intent.getStringExtra("title")?.let {
            tv_Title.text = it
        }
        url = intent.getStringExtra("url")
        init()
        adapter = object : QuickAdapter<H5CommentData.H5CommentBean>(this, R.layout.item_h5_comment, mData) {

            override fun convert(helper: BaseAdapterHelper?, item: H5CommentData.H5CommentBean?) {
                if (item?.from_user_id == loginBean?.user_id) {//自己
                    helper?.getTextView(R.id.tv_Content)?.text = "你：${item?.content}"
                } else {//作者
                    helper?.getTextView(R.id.tv_Content)?.text = "作者：${item?.content}"
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false
        webView.setBackgroundColor(0)
    }

    fun init() {
        val settings = webView.settings
        settings.builtInZoomControls = false
        settings.javaScriptEnabled = true
        settings.blockNetworkImage = false
        settings.setSupportZoom(false)
//        settings.useWideViewPort = true
//        settings.loadWithOverviewMode = true
//        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
//        settings.javaScriptCanOpenWindowsAutomatically = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.domStorageEnabled = true
        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView.setDownloadListener { url, _, _, _, _ ->
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                if (!TextUtils.isEmpty(title) && "声兮" != title && !isHtml)
                    tv_Title.text = title
            }
        }
        request(0)
    }

    /**
     * 防止网页注入多次滚动
     */
    private var isScrolled = false


    /**
     * 上传出现错误时候的bug
     */
    private fun uploadErrorInfo(formBody: FormBody) {
        OkClientHelper.post(this, "userslog/errors", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {

            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    /**
     * 开放评论功能
     */
    private var isOpenComment = 1
    private var content: String? = null
    override fun request(flag: Int) {
        OkClientHelper.get(this, "h5/html/$url", ArgumentData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as ArgumentData
                if (result.code == 0) {
                    tv_Title.text = result.data.html_title
                    val extra = intent.getBooleanExtra("isVersion", false)
                    var loadHtml = result.data.html_content
                    if (extra) {
                        loadHtml = result.data.html_content.replace("V1.0.0", "V" + AppTools.getVersion(this@ActionActivity))
                    }
                    webView.setHtml(loadHtml)
                    webView.setNightStyle(!TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName))
                    isOpenComment = result.data.is_open
                    content = result.data.html_content
                    if (result.data.is_open == 0) {
                        linearComment.visibility = View.GONE
                    }
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
        OkClientHelper.get(this, "htmls/$url/dialog", H5CommentData::class.java, object : OkResponse {
            override fun success(result: Any?) {
                result as H5CommentData
                if (result.data != null) {
                    for (item in result.data) {
                        mData.add(item)
                        adapter.notifyItemInserted(adapter.itemCount - 1)
                    }
                }
            }

            override fun onFailure(any: Any?) {

            }
        })
    }


    private fun getNewContent(htmltext: String) {
        val doc = Jsoup.parse(htmltext)
        val elements = doc.getElementsByTag("img")
        if (elements.size > 0) {
            for (element in elements) {
                val srcPath = element.attr("src")
                //下载图片
                save(srcPath)
            }
        } else {
            showToast("当前没有可下载图片")
        }
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        tv_Comment.setOnClickListener {
            /**
             * 留言
             */
            startActivityForResult(Intent(this, CommentActivity::class.java), REQUEST_COMMENT)
        }

        transLayout.findViewById<View>(R.id.tv_retry).setOnClickListener {
            /**
             * 重新加載
             */
            webView.setHtml(content)
        }
        webView.setWebClientListener(object : LoadContentWebView.onWebClientListener {
            override fun contentFinish(value: String?) {
                if (isOpenComment == 1) {
                    linearComment.visibility = View.VISIBLE
                } else {
                    linearComment.visibility = View.GONE
                }
                if (intent.getBooleanExtra("isScroll", false)) {
                    if (!isScrolled) {
                        nestedView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                isScrolled = true
                                nestedView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                nestedView.smoothScrollTo(0, nestedView.height)
                            }
                        })
                    }
                }
                transLayout.showContent()
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {

            }

            override fun onPageFinished(view: WebView?, url: String?) {


            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler, error: SslError?) {
                try {
//                    handler!!.proceed() // 接受所有网站的证书
                    val builder = AlertDialog.Builder(this@ActionActivity)
                    builder.setMessage(R.string.notification_error_ssl_cert_invalid)
                    builder.setPositiveButton(getString(R.string.string_confirm)) { dialog, which -> handler.proceed() }
                    builder.setNegativeButton(getString(R.string.string_cancel)) { dialog, which -> handler.cancel() }
                    val dialog = builder.create()
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.show()
                } catch (e: Exception) {

                }
            }
        })
        /*webView.setOnLongClickListener {
            if (webView.hitTestResult.type == WebView.HitTestResult.IMAGE_TYPE || webView.hitTestResult.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                //长按保存图片
            }
            false
        }*/
        ivDownload.setOnClickListener {
            /**
             * 获取图片地址 下载所有图片
             */
            content?.let {
                getNewContent(it)
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun save(url: String) {
        val path = if (url.contains("?")) {
            url.substring(0, url.lastIndexOf("?"))
        } else url
        var suffix = path.substring(path.lastIndexOf("/") + 1)
        if (!suffix.contains(".jpg", true) && !suffix.contains(".png", true) && !suffix.contains(".gif", true) && !suffix.contains(".jpeg", true)) {
            suffix = "$suffix.jpg"
        }
        val file = File(Environment.getExternalStorageDirectory(), IConstant.DOCNAME + "/" + IConstant.DOWNLOAD + "/" + suffix)
        if (file.exists()) {
            toast("图片已存在")
            return
        }
        object : AsyncTask<Void, Void, File>() {

            override fun doInBackground(vararg voids: Void): File? {
                try {
                    return Glide.with(this@ActionActivity)
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
                    toast("图片保存失败")
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
                    toast("图片已保存")
                }
            }
        }.execute()
    }

    private var toast: Toast? = null
    private fun toast(text: String) {
        if (toast == null)
            toast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
        else
            toast!!.setText(text)
        toast!!.show()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun postComment(formBody: FormBody) {
        OkClientHelper.post(this, "htmls/$url/dialog", formBody, BaseRepData::class.java, object : OkResponse {
            override fun success(result: Any?) {

            }

            override fun onFailure(any: Any?) {

            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_COMMENT) {
                data?.let {
                    val comment = it.getStringExtra("result")
                    postComment(FormBody.Builder().add("content", comment).build())
                    val bean = H5CommentData.H5CommentBean()
                    bean.content = comment
                    bean.from_user_id = loginBean?.user_id
                    mData.add(bean)
                    adapter.notifyItemInserted(adapter.itemCount - 1)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (toast != null) {
            toast!!.cancel()
            toast = null
        }
    }

}