package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import kotlinx.android.synthetic.main.layout_album_cover_view.view.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.model.VoiceAlbumData
import kotlin.math.ceil

class AlbumCoverView(context: Context) : BaseLayout(context) {

    override fun getLayoutId(): Int {
        return R.layout.layout_album_cover_view
    }

    /**
     * 设置显示名字
     * @param relation 用户关系，0=陌生人，1=待验证，2=已是好友，3：被对方拉黑，4：自己拉黑对方,5=自己
     */
    fun setTitle(bean: VoiceAlbumData.AlbumDataBean, relation: Int, width: Int, marginLeft: Int, isTalkAlbum: Boolean = false) {
        try {
            if (null != bean.album_cover_url)
                Glide.with(this)
                        .applyDefaultRequestOptions(RequestOptions().signature(ObjectKey(bean.album_cover_url)))
                        .load(bean.album_cover_url)
                        .into(ivAlbumCover)
            if (isTalkAlbum) {
                ivAlbumType.visibility = View.GONE
                tvAlbumLength.text = "${bean.dialog_num}条"
            } else {
                ivAlbumType.visibility = if (bean.album_type != 1) View.VISIBLE else View.GONE
                ivAlbumType.isSelected = bean.album_type == 3
                tvAlbumLength.text = "${ceil(bean.voice_total_len / 60f).toInt()} min"
            }
            if (!TextUtils.isEmpty(bean.album_name)) {
                tvAlbumTitle.text = bean.album_name
            }
            if (relation != 5) {
                tvAlbumTitle.text = when (bean.album_type) {
                    1 -> bean.album_name
                    2 -> {
                        if (relation == 2) {
                            relativeOtherType.visibility = View.GONE
                            bean.album_name
                        } else {
                            relativeOtherType.visibility = View.VISIBLE
                            resources.getString(R.string.string_visible_friends)
                        }
                    }
                    3 -> {
                        relativeOtherType.visibility = View.VISIBLE
                        resources.getString(R.string.string_default_empty_season_11)
                    }
                    else -> bean.album_name
                }
                if (bean.album_type == 2 || bean.album_type == 3) {
                    iv_privacy_type.isSelected = bean.album_type == 3
                } else {
                    relativeOtherType.visibility = View.GONE
                }
            }
            val params = cardLayout.layoutParams as RelativeLayout.LayoutParams
            params.width = width
            params.height = width
            params.setMargins(marginLeft, 0, 0, 0)
            cardLayout.layoutParams = params
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}