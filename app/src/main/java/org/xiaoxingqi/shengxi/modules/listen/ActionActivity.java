//package org.xiaoxingqi.shengxi.modules.listen;
//
//import android.app.Dialog;
//import android.content.Intent;
//import android.net.Uri;
//import android.net.http.SslError;
//import android.os.Build;
//import android.support.v7.app.AlertDialog;
//import android.text.TextUtils;
//import android.view.KeyEvent;
//import android.view.Window;
//import android.webkit.SslErrorHandler;
//import android.webkit.WebChromeClient;
//import android.webkit.WebResourceRequest;
//import android.webkit.WebSettings;
//import android.webkit.WebView;
//import android.webkit.WebViewClient;
//import android.widget.TextView;
//
//import org.jetbrains.annotations.NotNull;
//import org.xiaoxingqi.shengxi.R;
//import org.xiaoxingqi.shengxi.core.BaseAct;
//import org.xiaoxingqi.shengxi.modules.user.UserDetailsActivity;
//
//public class ActionActivity extends BaseAct {
//    private String url;
//    private boolean isHtml;
//    private TextView mTvTitle;
//    private WebView mWebview;
//
//    @Override
//    public int getLayoutId() {
//        return R.layout.activity_action;
//    }
//
//    @Override
//    public void initView() {
//        mTvTitle = findViewById(R.id.tv_Title);
//        mWebview = findViewById(R.id.webView);
//    }
//
//    @Override
//    public void initData() {
//        isHtml = getIntent().getBooleanExtra("isHtml", false);
//        if (!TextUtils.isEmpty(getIntent().getStringExtra("title"))) {
//            mTvTitle.setText(getIntent().getStringExtra("title"));
//        }
//        url = getIntent().getStringExtra("url");
//        init();
//    }
//
//    private void init() {
//
//        WebSettings settings = mWebview.getSettings();
//        settings.setLoadWithOverviewMode(true);
//        settings.setBuiltInZoomControls(false);
//        settings.setJavaScriptEnabled(true);
//        settings.setUseWideViewPort(true);
//        settings.setBlockNetworkImage(false);
//        settings.setSupportZoom(false);
//        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        settings.setJavaScriptCanOpenWindowsAutomatically(true);
//        settings.setDomStorageEnabled(true);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//        }
//        mWebview.setDownloadListener((url1, userAgent, contentDisposition, mimetype, contentLength) -> {
//            Uri uri = Uri.parse(url);
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            startActivity(intent);
//        });
//        mWebview.setWebChromeClient(new WebChromeClient() {
//            @Override
//            public void onReceivedTitle(WebView view, String title) {
//                super.onReceivedTitle(view, title);
//                if (!TextUtils.isEmpty(title) && !"声兮".equals(title) && !isHtml)
//                    mTvTitle.setText(title);
//            }
//        });
//        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
//        mWebview.setWebViewClient(new WebViewClient() {
//
//            @Override
//            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                super.onReceivedSslError(view, handler, error);
//                handler.proceed(); // 接受所有网站的证书
//                AlertDialog.Builder builder = new AlertDialog.Builder(ActionActivity.this);
//                builder.setMessage(R.string.notification_error_ssl_cert_invalid);
//                builder.setPositiveButton(getResources().getString(R.string.string_confirm), (dialog, which) -> {
//                    handler.proceed();
//                });
//                builder.setNegativeButton(getString(R.string.string_cancel), (dialog, which) -> {
//                    handler.cancel();
//                });
//                Dialog dialog = builder.create();
//                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                dialog.show();
//            }
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//                if (request.getUrl().toString().contains("uid")) {
//                    startActivity(new Intent(ActionActivity.this, UserDetailsActivity.class).
//                            putExtra("id", request.getUrl().toString().substring(request.getUrl().toString().lastIndexOf("uid=", request.getUrl().toString().length())).replace("uid=", "")));
//                    return true;
//                }
//
//                return super.shouldOverrideUrlLoading(view, request);
//            }
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                if (url.contains("uid")) {
//                    startActivity(new Intent(ActionActivity.this, UserDetailsActivity.class).
//                            putExtra("id", url.substring(url.lastIndexOf("uid=", url.length())).replace("uid=", "")));
//                    return true;
//                }
//
//                return super.shouldOverrideUrlLoading(view, url);
//            }
//        });
//
//
//        if (!isHtml) {
//            mWebview.loadUrl(url);
//        } else {
//            String head = "<meta name=viewport content=width=device-width,minimum-scale=1.0,maximum-scale=1.0,initial-scale=1.0 user-scalable=no />" +
//                    "<style>\n" +
//                    "  img{\n" +
//                    "    max-width:100%; \n" +
//                    "    max-height:100%;\n" +
//                    "    display:block;\n" +
//                    "    margin: 0 auto;\n" +
//                    "  }\n" +
//                    "</style>";
//            mWebview.loadDataWithBaseURL(null, head + url, "text/html", "UTF-8", null);
//        }
//    }
//
//    @Override
//    public void initEvent() {
//        findViewById(R.id.btn_Back).setOnClickListener(view -> finish());
//    }
//
//    @Override
//    public boolean onKeyDown(int keyCode, @NotNull KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (mWebview.canGoBack()) {
//                mWebview.goBack();
//                return true;
//            }
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//
//}
