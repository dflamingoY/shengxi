package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import kotlinx.android.synthetic.main.dialog_guide_anim_hint.*
import org.xiaoxingqi.shengxi.R

class DialogGuideHint(context: Context) : BaseDialog(context) {
    override fun getLayoutId(): Int {
        return R.layout.dialog_guide_anim_hint
    }

    override fun initView() {
        ivDismiss.setOnClickListener { dismiss() }
        fillWidth()
    }
}