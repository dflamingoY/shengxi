package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import org.xiaoxingqi.shengxi.model.BaseSearchBean
import skin.support.widget.SkinCompatImageView

class PlayMenuImageView(context: Context?, attrs: AttributeSet?) : SkinCompatImageView(context, attrs) {
    private var bean: BaseSearchBean? = null

    fun attchData(bean: BaseSearchBean) {
        this.bean = bean
    }

    fun update() {
        bean?.let {
            this.isSelected = it.first_voice.isPlaying
        }
    }
}