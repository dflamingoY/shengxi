package org.xiaoxingqi.shengxi.modules.login;

import android.os.Build;
import android.webkit.WebSettings;
import android.widget.TextView;

import org.xiaoxingqi.shengxi.R;
import org.xiaoxingqi.shengxi.core.BaseLoginAct;
import org.xiaoxingqi.shengxi.wedgit.LoadContentWebView;

public class ArgumentActivity extends BaseLoginAct {
    private TextView tvTitle;
    private LoadContentWebView webView;


    @Override
    public int getLayoutId() {
        return R.layout.activity_argument;
    }

    @Override
    public void initView() {
        tvTitle = findViewById(R.id.tv_Title);
        webView = findViewById(R.id.webView);
    }

    @Override
    public void initData() {
        String title = getIntent().getStringExtra("title");
        tvTitle.setText(title);
        String url = getIntent().getStringExtra("url");
        WebSettings settings = webView.getSettings();
        settings.setBuiltInZoomControls(false);
        settings.setJavaScriptEnabled(true);
        settings.setBlockNetworkImage(false);
        settings.setSupportZoom(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.setHtml(url);
    }

    @Override
    public void initEvent() {
        findViewById(R.id.btn_Back).setOnClickListener(view -> finish());
    }
}
