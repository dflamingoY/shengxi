package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.layout_top_to_button_user_type.view.*
import org.xiaoxingqi.shengxi.R
import skin.support.content.res.SkinCompatResources
import skin.support.widget.SkinCompatSupportable

class LayoutTopUserButton(context: Context, attrs: AttributeSet?) : BaseLayout(context, attrs), SkinCompatSupportable {
    private var drawId = 0
    private var privacyStatus = 1//1 公开 2 仅限好友 3仅自己可见
    private var isEmptyData = false

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.EchoTypeView)
        val title = array.getString(R.styleable.EchoTypeView_echoTitle)
        if (array.hasValue(R.styleable.EchoTypeView_echoImg)) {
            drawId = array.getResourceId(R.styleable.EchoTypeView_echoImg, 0)
        }
        array.recycle()
        SkinCompatResources.init(getContext())
        applySkin()
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_top_to_button_user_type
    }

    override fun applySkin() {
        try {
            val mDraw = SkinCompatResources.getInstance().getDrawable(drawId)
            if (null != mDraw) {
                ivIcon.setImageDrawable(mDraw)
            }
        } catch (e: Exception) {
        }
    }

    /**
     * 设置此功能为锁的状态 此状态不可点击
     */
    fun setLockStatus(status: Int = 2) {
        privacyStatus = status
        ivLock.visibility = View.VISIBLE
        ivIcon.visibility = View.GONE
    }

    //获取当前按钮的状态,1 公开 2 仅限好友 3仅自己可见
    fun getButtonStatus() = privacyStatus

    //设置button点击为空内容的状态
    fun setEmptyStatus() {
        isSelected = true
        isEmptyData = true
    }

    fun getEmptyStatus() = isEmptyData
}