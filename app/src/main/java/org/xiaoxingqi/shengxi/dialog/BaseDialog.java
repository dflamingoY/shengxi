package org.xiaoxingqi.shengxi.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import org.xiaoxingqi.shengxi.utils.AppTools;

public abstract class BaseDialog extends Dialog {

    public BaseDialog(@NonNull Context context) {
        super(context);
    }

    public BaseDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(getLayoutId());
        initView();
    }

    protected void initSystem() {
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.gravity = Gravity.BOTTOM;
        p.width = AppTools.getWindowsWidth(getContext());
        getWindow().setAttributes(p);
    }

    protected void fillWidth() {
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        WindowManager manager = getWindow().getWindowManager();
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        Display display = manager.getDefaultDisplay();
        attributes.width = display.getWidth();
        getWindow().setAttributes(attributes);
    }

    protected abstract int getLayoutId();

    protected abstract void initView();

}
