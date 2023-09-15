package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import org.xiaoxingqi.shengxi.R

class SelectorImage(context: Context?, attrs: AttributeSet?) : ImageView(context, attrs) {

    /**
     * @score 当前评分的等级
     * @type  当前展示的类型  1 2 为评书和电影, 展示普通的界面   3 .唱回忆, 展示心形
     */
    fun setImagResId(score: String, type: Int) {
        if (type == 1 || type == 2) {
            when (score) {
                "100" -> {
                    setImageResource(R.mipmap.icon_good)
                }
                "50" -> {
                    setImageResource(R.mipmap.icon_fair)
                }
                "0" -> {
                    setImageResource(R.mipmap.icon_bad)
                }
            }
        } else {
            when (score) {
                "100" -> {
                    setImageResource(R.mipmap.icon_music_bad)
                }
                "150" -> {
                    setImageResource(R.mipmap.icon_music_normal)
                }
                "200" -> {
                    setImageResource(R.mipmap.icon_music_good)
                }
                else -> {
                    setImageResource(R.mipmap.icon_music_bad)
                }
            }
        }
    }

}