package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.item_theme_view.view.*
import org.xiaoxingqi.shengxi.R
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatSupportable

class ThemeItemView : BaseLayout, SkinCompatSupportable {

    private var selectedId: Drawable?
    private var imgResId = 0

    override fun getLayoutId(): Int {
        return R.layout.item_theme_view
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.SelectRecommendView)
        selectedId = array.getDrawable(R.styleable.SelectRecommendView_selected_img_id)
        imgResId = array.getResourceId(R.styleable.SelectRecommendView_selected_img_id, 0)
        val title = array.getString(R.styleable.SelectRecommendView_select_Title)
        array.recycle()
        if (!TextUtils.isEmpty(title)) {
            tv_theme_name.text = title
        }
        if (selectedId != null) {
            iv_theme.setImageDrawable(selectedId)
        }
        SkinCompatResources.init(getContext())
        applySkin()
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selected) {
            iv_isSelected.visibility = View.VISIBLE
        } else {
            iv_isSelected.visibility = View.GONE
        }
    }

    override fun applySkin() {
        try {
            selectedId = SkinCompatResources.getInstance().getMipmap(imgResId)
            if (selectedId != null) {
                iv_theme.setImageDrawable(selectedId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}