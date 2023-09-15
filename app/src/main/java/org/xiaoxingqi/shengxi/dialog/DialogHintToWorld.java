package org.xiaoxingqi.shengxi.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.xiaoxingqi.shengxi.R;

public class DialogHintToWorld extends Dialog {

    public DialogHintToWorld(@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        setContentView(R.layout.dialog_share_to_world);
        findViewById(R.id.tv_Commit).setOnClickListener(view -> {
            if (null != clickListener) {
                clickListener.onClick(view);
            }
            dismiss();
        });
        findViewById(R.id.tv_Cancel).setOnClickListener(view -> {
            if (null != clickListener) {
                clickListener.onClick(view);
            }
            dismiss();
        });
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = d.getWidth(); //设置dialog的宽度为当前手机屏幕的宽度
        getWindow().setAttributes(p);
    }

    private View.OnClickListener clickListener;

    public DialogHintToWorld setOnClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
        return this;
    }
}
