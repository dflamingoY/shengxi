package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.item_echo_type.view.*
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.model.SystemNoticeData
import org.xiaoxingqi.shengxi.utils.TimeUtils
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatSupportable

class EchoTypeView : BaseLayout, SkinCompatSupportable {
    private var mDraw: Drawable? = null
    var title: String? = null
    private var defaultHint: String? = null
    private var resourcesId = 0

    constructor (context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.EchoTypeView)
        mDraw = array.getDrawable(R.styleable.EchoTypeView_echoImg)
        title = array.getString(R.styleable.EchoTypeView_echoTitle)
        defaultHint = array.getString(R.styleable.EchoTypeView_echoesDefaultHint)
        resourcesId = array.getResourceId(R.styleable.EchoTypeView_echoImg, 0)
        array.recycle()
        SkinCompatResources.init(getContext())
        applySkin()
        init()
    }

    fun init() {
        mDraw?.let {
            roundCircle.setImageDrawable(mDraw)
        }
        tvUserName.text = (title)
        tv_Hint.text = defaultHint
    }

    override fun getLayoutId(): Int {
        return R.layout.item_echo_type
    }

    fun setLineVisible(isVisible: Boolean) {
        view_line.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    /**
     * 设置提醒事件 时间  数量
     */
    fun setData(any: SystemNoticeData.MessageNoticeBean?) {
        if (any == null) {
            tv_Hint.text = defaultHint
            tv_Count.visibility = View.GONE
            tvTime.text = ""
        } else {
            if (!TextUtils.isEmpty(any.tips)) {
                tv_Hint.text = any.tips
            } else {
                tv_Hint.text = defaultHint
            }
            if (!TextUtils.isEmpty(any.num) && "0" != any.num) {
                tv_Count.visibility = View.VISIBLE
                tv_Count.text = any.num
            } else {
                tv_Count.visibility = View.GONE
            }
            if (!TextUtils.isEmpty(any.time) && "0" != any.time) {
                tvTime.text = TimeUtils.getInstance().paserFriends(context, Integer.parseInt(any.time))
            }
        }
    }

    override fun applySkin() {
        try {
            mDraw = SkinCompatResources.getInstance().getMipmap(resourcesId)
            roundCircle.setImageDrawable(mDraw)
        } catch (e: Exception) {
        }
    }
}