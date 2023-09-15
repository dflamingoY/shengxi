package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.layout_item_voice_list.view.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.model.BaseSearchBean
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.TimeUtils
import skin.support.SkinCompatManager

class VoiceListItemView(context: Context, attrs: AttributeSet?) : BaseLayout(context, attrs) {

    override fun getLayoutId(): Int {
        return R.layout.layout_item_voice_list
    }

    fun setData(data: BaseSearchBean, resourceType: Int, userScore: Int) {
        if (resourceType == 1 || resourceType == 2) {
            val params = iv_Movies.layoutParams
            params.height = AppTools.dp2px(context, 133)
            val layoutParams = iv_ScoreType.layoutParams as RelativeLayout.LayoutParams
            layoutParams.setMargins(AppTools.dp2px(context, 12), AppTools.dp2px(context, 20), 0, 0)
        } else {
            val params = iv_Movies.layoutParams
            params.height = AppTools.dp2px(context, 93)
            val layoutParams = iv_ScoreType.layoutParams as RelativeLayout.LayoutParams
            layoutParams.setMargins(AppTools.dp2px(context, 12), 0, 0, 0)
        }
        try {
            Glide.with(this)
                    .applyDefaultRequestOptions(RequestOptions()
                            .centerCrop()
                            .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                            .error(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                            .signature(ObjectKey(if (resourceType == 1) data.movie_poster else if (resourceType == 2) data.book_cover else data.song_cover)))
                    .load(if (resourceType == 1) data.movie_poster else if (resourceType == 2) data.book_cover else data.song_cover)
                    .into(iv_Movies)
            when (resourceType) {
                1 -> {
                    tv_MovieName.text = data.movie_title
                    tv_MovieDesc.text = "${data.movie_score / 10f}分/${TimeUtils.getInstance().paserYyMm(data.released_at)}${AppTools.getActors(data.movie_area)}"
                }
                2 -> {
                    tv_MovieName.text = data.book_name
                    tv_MovieDesc.text = "${data.book_score / 10}${if (!TextUtils.isEmpty(data.published_date)) "/${data.published_date}" else {
                        ""
                    }}/${data.book_author}"
                }
                else -> {
                    tv_MovieName.text = data.song_name
                    tv_MovieDesc.text = data.song_singer + "/" + data.album_name
                }
            }
            iv_ScoreType.setImagResId(userScore.toString(), resourceType)
        } catch (e: Exception) {

        }
    }
}