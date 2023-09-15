package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.item_dynamic_view.view.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.model.BaseSearchBean
import org.xiaoxingqi.shengxi.modules.listen.book.OneBookDetailsActivity
import org.xiaoxingqi.shengxi.modules.listen.movies.OneMovieDetailsActivity
import org.xiaoxingqi.shengxi.modules.listen.music.OneMusicDetailsActivity
import org.xiaoxingqi.shengxi.utils.AppTools
import org.xiaoxingqi.shengxi.utils.TimeUtils
import skin.support.SkinCompatManager

/**
 * 展示 影评 书评 唱歌的item
 * 默认影评
 */
class ItemDynamicView : BaseLayout {

    override fun getLayoutId(): Int {
        return R.layout.item_dynamic_view
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    /**
     * 设置展示的数据
     * 1: 处理布局
     * 2.展示数据
     */
    fun setData(data: BaseSearchBean, resourceType: Int, userScore: Int) {
        if (resourceType == 1 || resourceType == 2) {
            val params = ivItemCover.layoutParams
            params.height = AppTools.dp2px(context, 60)

            val layoutParams = linearItemText.layoutParams as RelativeLayout.LayoutParams
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            layoutParams.setMargins(0, AppTools.dp2px(context, 17), 0, 0)
            linearItemText.layoutParams = layoutParams

            val scoreParams = itemScoreType.layoutParams as RelativeLayout.LayoutParams
            scoreParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            scoreParams.setMargins(0, AppTools.dp2px(context, 16), AppTools.dp2px(context, 12), 0)

        } else if (resourceType == 3) {
            val params = ivItemCover.layoutParams
            params.height = AppTools.dp2px(context, 53)
            val layoutParams = linearItemText.layoutParams as RelativeLayout.LayoutParams
//            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
            layoutParams.setMargins(0, AppTools.dp2px(context, 19), 0, 0)
            linearItemText.layoutParams = layoutParams

            val scoreParams = itemScoreType.layoutParams as RelativeLayout.LayoutParams
            scoreParams.addRule(RelativeLayout.CENTER_VERTICAL)
            scoreParams.setMargins(0, 0, AppTools.dp2px(context, 12), 0)

        } else {
            this.visibility = View.GONE
        }
        try {
            Glide.with(this)
                    .applyDefaultRequestOptions(RequestOptions()
                            .placeholder(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                            .error(if (TextUtils.isEmpty(SkinCompatManager.getInstance().curSkinName)) R.drawable.drawable_default_tmpry else R.drawable.drawable_default_tmpry_night)
                            .signature(ObjectKey(if (resourceType == 1) data.movie_poster else if (resourceType == 2) data.book_cover else data.song_cover)))
                    .load(if (resourceType == 1) data.movie_poster else if (resourceType == 2) data.book_cover else data.song_cover)
                    .into(ivItemCover)
            if (resourceType == 1) {
                tvItemName.text = data.movie_title
                tvItemType.text = "${data.movie_score / 10f}分/${if (data.released_at != 0 || TimeUtils.string2Long(data.released_date) == 0) {
                    TimeUtils.getInstance().paserYyMm(data.released_at)
                } else {
                    data.released_date.substring(0, data.released_date.indexOf(" "))
                }}${AppTools.getActors(data.movie_area)}"
            } else if (resourceType == 2) {
                tvItemName.text = data.book_name
                tvItemType.text = "${data.book_score / 10f}分/${data.book_author}"
            } else {
                tvItemName.text = data.song_name
                tvItemType.text = "${data.song_singer}"
            }
            itemScoreType.setImagResId(userScore.toString(), resourceType)
        } catch (e: Exception) {
        }
        setOnClickListener {
            context.startActivity(Intent(context, if (resourceType == 1) OneMovieDetailsActivity::class.java else if (resourceType == 2) OneBookDetailsActivity::class.java else OneMusicDetailsActivity::class.java)
                    .putExtra("id", data.id)
            )
        }
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        relativeParent.isSelected = selected
    }

}