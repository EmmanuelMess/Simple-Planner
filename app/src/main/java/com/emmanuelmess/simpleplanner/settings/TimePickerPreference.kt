package com.emmanuelmess.simpleplanner.settings

import android.app.TimePickerDialog
import android.content.Context
import android.content.res.TypedArray
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.util.AttributeSet
import android.widget.TimePicker
import androidx.preference.DialogPreference
import com.emmanuelmess.simpleplanner.common.setToFirstInstant
import java.util.*

class TimePickerPreference(ctxt: Context, attrs: AttributeSet) : DialogPreference(ctxt, attrs) {
    var hourOfDay = 0
        private set
    var minute = 0
        private set

    init {
        summaryProvider = TimeSummaryProvider()
    }

    override fun onClick() {
        TimePickerDialog(
            context,
            {_: TimePicker?, hourOfDay: Int, minute: Int ->
                this.hourOfDay = hourOfDay
                this.minute = minute

                val saveable = Time(hourOfDay, minute)

                if(callChangeListener(saveable)) {
                    persistString(saveable.toString())
                    notifyChanged()
                }
            },
            hourOfDay,
            minute,
            DateFormat.is24HourFormat(context)
        ).show()
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        if (defaultValue !is String) {
            throw IllegalArgumentException()
        }

        val time: Time = getPersistedString(defaultValue)!!.toTime()

        hourOfDay = time.hourOfDay
        minute = time.minute
    }

    class TimeSummaryProvider: SummaryProvider<TimePickerPreference> {
        override fun provideSummary(preference: TimePickerPreference): CharSequence {
            val calendar = Calendar.getInstance().setToFirstInstant().apply {
                set(Calendar.MINUTE, preference.minute)
                set(Calendar.HOUR_OF_DAY, preference.hourOfDay)
            }

            return DateUtils.formatDateTime(preference.context, calendar.timeInMillis, DateUtils.FORMAT_SHOW_TIME)
        }
    }

}