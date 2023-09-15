package org.xiaoxingqi.shengxi.modules.listen

import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.webkit.*
import kotlinx.android.synthetic.main.activity_webview.*
import okhttp3.FormBody
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.core.http.OkClientHelper
import org.xiaoxingqi.shengxi.core.http.OkResponse
import org.xiaoxingqi.shengxi.model.BaseRepData
import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity

class WebViewActivity : BaseAct() {
    private var url: String? = null


    override fun getLayoutId(): Int {
        return R.layout.activity_webview
    }

    override fun initView() {
        val settings = webView.settings
        settings.builtInZoomControls = false
        settings.javaScriptEnabled = true
        settings.blockNetworkImage = false
        settings.defaultTextEncodingName = "UTF-8"
        settings.setSupportZoom(false)
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
                if (!TextUtils.isEmpty(title) && "声兮" != title && "声昔" != title)
                    tv_Title.text = title
            }
        }
        webView.webViewClient = object : WebViewClient() {

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                // handler.cancel(); // Android默认的处理方式
                try {
//                    handler.proceed() // 接受所有网站的证书
                    val builder = AlertDialog.Builder(this@WebViewActivity)
                    builder.setMessage(R.string.notification_error_ssl_cert_invalid)
                    builder.setPositiveButton(getString(R.string.string_confirm)) { _, _ -> handler.proceed() }
                    builder.setNegativeButton(getString(R.string.string_cancel)) { _, _ -> handler.cancel() }
                    val dialog = builder.create()
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.show()
                } catch (e: Exception) {

                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                request?.let {
                    if (it.url.toString().contains("byebyetext/uid")) {
                        startActivity(Intent(this@WebViewActivity, UserDetailsActivity::class.java).putExtra("id", it.url.toString().substring(it.url.toString().lastIndexOf("uid=", it.url.toString().length)).replace("uid=", "")))
                        return true
                    }
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let {
                    if (it.contains("byebyetext/uid")) {
                        startActivity(Intent(this@WebViewActivity, UserDetailsActivity::class.java).putExtra("id", it.substring(it.lastIndexOf("uid=", it.length)).replace("uid=", "")))
                        return true
                    }
                }
                return super.shouldOverrideUrlLoading(view, url)
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                if ("net::ERR_NAME_NOT_RESOLVED" == error?.description) {

                    val url = request?.url.toString()
                    try {
                        url?.let {
                            uploadErrorInfo(FormBody.Builder().add("action", url.substring(0, url.indexOf("?")))
                                    .add("error", error?.description.toString())
                                    .add("params", url.substring(url.indexOf("?") + 1, url.length))
                                    .build())
                        }
                    } catch (e: Exception) {
                    }
                    transLayout.showOffline()
                }
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                if ("net::ERR_NAME_NOT_RESOLVED" == description) {

                    try {
                        failingUrl?.let {
                            uploadErrorInfo(FormBody.Builder().add("action", failingUrl?.substring(0, failingUrl?.indexOf("?")))
                                    .add("error", description)
                                    .add("params", failingUrl?.substring(failingUrl?.indexOf("?") + 1, failingUrl?.length))
                                    .build())
                        }
                    } catch (e: Exception) {
                    }
                    transLayout.showOffline()
                }
            }

        }
    }

    override fun initData() {
        intent.getStringExtra("title")?.let {
            tv_Title.text = it
        }
        url = intent.getStringExtra("url")
        webView.loadUrl(url)
//        webView.loadData(url, "text/html; charset=UTF-8", null)
    }

    override fun initEvent() {
        btn_Back.setOnClickListener { finish() }
        transLayout.findViewById<View>(R.id.tv_retry).setOnClickListener {
            /**
             * 重新加載
             */
            webView.loadUrl(url)
        }
    }

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

    override fun request(flag: Int) {


    }
}