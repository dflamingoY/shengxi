package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.layout_selected_user_indentity.view.*
import org.xiaoxingqi.shengxi.R
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatSupportable

class ViewUserIdentity(context: Context, attrs: AttributeSet?) : BaseLayout(context, attrs), SkinCompatSupportable {

    private var coverDraw: Drawable? = null
    private var coverDrawId = 0

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.ViewUserIdentity)
        coverDraw = array.getDrawable(R.styleable.ViewUserIdentity_identity_cover)
        val title = array.getString(R.styleable.ViewUserIdentity_identity_title)
        coverDrawId = array.getResourceId(R.styleable.ViewUserIdentity_identity_cover, 0)
        val desc = array.getString(R.styleable.ViewUserIdentity_identity_desc)
        array.recycle()
        tv_title.text = title
        tv_desc.text = desc
        iv_identity_cover.setImageDrawable(coverDraw)
        SkinCompatResources.init(getContext())
        applySkin()
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_selected_user_indentity
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        tv_selected.visibility = if (selected) View.VISIBLE else View.INVISIBLE
    }

    override fun applySkin() {
        coverDraw = SkinCompatResources.getInstance().getDrawable(coverDrawId)
        if (coverDraw != null) {
            iv_identity_cover.setImageDrawable(coverDraw)
        }
    }

}