package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.util.AttributeSet
import kotlinx.android.synthetic.main.layout_item_find_word.view.*
import org.xiaoxingqi.shengxi.R

class ViewFindWord @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseLayout(context, attrs, defStyleAttr) {

    override fun getLayoutId(): Int {
        return R.layout.layout_item_find_word
    }

    fun setTitle(title: String) {
        tvItemName.text = title
    }
}