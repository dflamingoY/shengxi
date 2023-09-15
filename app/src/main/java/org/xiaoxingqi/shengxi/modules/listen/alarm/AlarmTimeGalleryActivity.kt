package org.xiaoxingqi.shengxi.modules.listen.alarm

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.TextUtils
import android.view.ViewTreeObserver
import kotlinx.android.synthetic.main.activity_alarm_time_gallery.*
import org.xiaoxingqi.alarmService.model.DaysOfWeek
import org.xiaoxingqi.shengxi.R
import org.xiaoxingqi.shengxi.core.BaseAct
import org.xiaoxingqi.shengxi.dialog.DialogAlarmSelectRepeat
import java.time.DayOfWeek

class AlarmTimeGalleryActivity : BaseAct() {
    private var dialogWeek: DialogAlarmSelectRepeat? = null
    private var preHour = 0
    private var preMinute = 0
    private lateinit var sp: SharedPreferences
    private var coded = 0
    override fun getLayoutId(): Int {
        return R.layout.activity_alarm_time_gallery
    }

    override fun initView() {

    }

    override fun initData() {
        preHour = intent.getIntExtra("hour", 0)
        preMinute = intent.getIntExtra("minute", 0)
        coded = intent.getIntExtra("daysOfWeek", 0)
        tvWeeks.text = DaysOfWeek(coded).toString(this, true)
        sp = PreferenceManager.getDefaultSharedPreferences(this)
        toggleVibrate.isSelected = sp.getString("snooze_duration", "-1") != "-1"
    }

    override fun initEvent() {
        tvDismiss.setOnClickListener { finish() }
        tvSave.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent()
                    .putExtra("hour", pickerHour.contentByCurrValue)
                    .putExtra("snooze_duration", if (toggleVibrate.isSelected) "5" else "-1")
                    .putExtra("daysOfWeek", coded)
                    .putExtra("minute", pickerMinutes.contentByCurrValue))
            finish()
        }
        toggleVibrate.setOnClickListener {
            toggleVibrate.isSelected = !it.isSelected
        }
        relativeRepeat.setOnClickListener {
            if (dialogWeek == null) {
                dialogWeek = DialogAlarmSelectRepeat(this).setCode(coded).setOnWeekResultListener(object : DialogAlarmSelectRepeat.OnWeekCheckResultListener {
                    override fun onWeek(arrays: Int) {
                        coded = arrays
                        tvWeeks.text = DaysOfWeek(arrays).toString(this@AlarmTimeGalleryActivity, true)
                    }
                })
            }
            dialogWeek?.show()
        }
        pickerHour.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                pickerHour.viewTreeObserver.removeOnGlobalLayoutListener(this)
                pickerHour.smoothScrollToValue(preHour + 1)
                pickerMinutes.smoothScrollToValue(preMinute + 1)
            }
        })
    }
}