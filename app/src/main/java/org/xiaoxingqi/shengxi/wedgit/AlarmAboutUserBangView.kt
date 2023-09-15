package org.xiaoxingqi.shengxi.wedgit

import android.content.Context
import android.util.AttributeSet
import kotlinx.android.synthetic.main.layout_alarm_bang_user.view.*
import org.xiaoxingqi.shengxi.R

class AlarmAboutUserBangView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseLayout(context, attrs, defStyleAttr) {
    override fun getLayoutId(): Int {
        return R.layout.layout_alarm_bang_user
    }

    fun setAngel(count: Int) {
        if (count > 0)
            tvAngel.text = "${count}票"
    }

    fun setMonster(count: Int) {
        if (count > 0)
            tvMonster.text = "${count}票"
    }

    fun setGod(count: Int) {
        if (count > 0)
            tvGod.text = "${count}票"
    }

    fun setDubbing(count: Int) {
        if (count > 0)
            tvDubbingCount.text = "${count}配"
    }

    fun setDividingVisible(visible: Int) {
        viewDividing.visibility = visible
    }
}