package org.xiaoxingqi.shengxi.wedgit

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.layout_user_topic.view.*
import org.xiaoxingqi.shengxi.R

class UserTopicView(context: Context, attrs: AttributeSet?) : BaseLayout(context, attrs) {

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.ToggleLayoutView)
        val name = array.getString(R.styleable.ToggleLayoutView_typeName)
        array.recycle()
        if (!TextUtils.isEmpty(name))
            tv_TitleName.text = name
        tvTopic.setOnClickListener {
            tv_AddTopic.visibility = View.VISIBLE
            tvTopic.text = ""
            deletedListener?.deleted()
        }
    }

    interface OnDeletedTopicListener {
        fun deleted()
    }

    private var deletedListener: OnDeletedTopicListener? = null
    fun setOnDeletedListener(deletedListener: OnDeletedTopicListener) {
        this.deletedListener = deletedListener
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_user_topic
    }

    @SuppressLint("SetTextI18n")
    fun setTopic(name: String) {
        tv_AddTopic.visibility = View.GONE
        tvTopic.text = "#$name# X"
    }

    /**
     * 设置标题
     */
    fun setTitle(title: String) {
        tv_TitleName.text = title
    }

}