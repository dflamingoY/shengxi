package org.xiaoxingqi.shengxi.dialog

import android.content.Context
import android.graphics.drawable.ColorDrawable
import kotlinx.android.synthetic.main.dialog_alarm_selected_repeat.*
import org.xiaoxingqi.alarmService.model.DaysOfWeek
import org.xiaoxingqi.shengxi.R

/**
 * 闹钟选择重复时间
 */
class DialogAlarmSelectRepeat(context: Context) : BaseDialog(context, R.style.FullDialogTheme) {
    private var coded: Int = 0

    override fun getLayoutId(): Int {
        return R.layout.dialog_alarm_selected_repeat
    }

    override fun initView() {
        //解析当前选中了哪些周期
        weekMonday.isSelected = 0.isSet()
        weekTuesday.isSelected = 1.isSet()
        weekWed.isSelected = 2.isSet()
        weekThu.isSelected = 3.isSet()
        weekFri.isSelected = 4.isSet()
        weekSat.isSelected = 5.isSet()
        weekSun.isSelected = 6.isSet()
        weekMonday.setOnClickListener {
            it.isSelected = !it.isSelected
            parseCoded(0, it.isSelected)
        }
        weekTuesday.setOnClickListener {
            it.isSelected = !it.isSelected
            parseCoded(1, it.isSelected)
        }
        weekWed.setOnClickListener {
            it.isSelected = !it.isSelected
            parseCoded(2, it.isSelected)

        }
        weekThu.setOnClickListener {
            it.isSelected = !it.isSelected
            parseCoded(3, it.isSelected)
        }
        weekFri.setOnClickListener {
            it.isSelected = !it.isSelected
            parseCoded(4, it.isSelected)
        }
        weekSat.setOnClickListener {
            it.isSelected = !it.isSelected
            parseCoded(5, it.isSelected)
        }
        weekSun.setOnClickListener {
            it.isSelected = !it.isSelected
            parseCoded(6, it.isSelected)
        }
        tvCancel.setOnClickListener {
            dismiss()
        }
        tvConfirm.setOnClickListener {
            onWeekResult?.onWeek(coded)
            dismiss()
        }
        window.setBackgroundDrawable(ColorDrawable(0))
        initSystem()
    }

    private fun parseCoded(index: Int, isSelected: Boolean) {
        coded = if (isSelected) {
            coded or (1 shl index)
        } else {
            coded and (1 shl index).inv()
        }
    }

    fun setCode(coded: Int): DialogAlarmSelectRepeat {
        this.coded = coded
        return this
    }

    private var onWeekResult: OnWeekCheckResultListener? = null

    interface OnWeekCheckResultListener {
        fun onWeek(result: Int)
    }

    fun setOnWeekResultListener(onWeekResultListener: OnWeekCheckResultListener): DialogAlarmSelectRepeat {
        onWeekResult = onWeekResultListener
        return this
    }

    private fun Int.isSet(): Boolean {
        return coded and (1 shl this) > 0
    }
}