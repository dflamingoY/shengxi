package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.util.AttributeSet
import kotlinx.android.synthetic.main.voice_show_anim_progress.view.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.model.BaseAnimBean
import org.xiaoxingqi.shengxi.utils.AppTools

class VoiceShowAnimProgress @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseLayout(context, attrs, defStyleAttr) {

    override fun getLayoutId(): Int {
        return R.layout.voice_show_anim_progress
    }

    fun setData(bean: BaseAnimBean) {
        val params = progress.layoutParams
        try {
            val length = bean.voice_len.toInt()
            val width = (AppTools.dp2px(context, 100 - 50) * (length * 1f / 120) + 0.5f).toInt() + AppTools.dp2px(context, 32)
            params.width = width
            progress.layoutParams = params
            tv_Time.text = AppTools.parseTime2Str(length * 1000.toLong())
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            params.width = AppTools.dp2px(context, 32)
            progress.layoutParams = params
            tv_Time.text = "00:00"
        }
    }

}