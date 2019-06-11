package com.emmanuelmess.simpleplanner.settings

import android.app.TimePickerDialog
import android.content.Context
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

                val calendar = Calendar.getInstance().setToFirstInstant().apply {
                    set(Calendar.MINUTE, minute)
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                }

                if(callChangeListener(calendar.timeInMillis)) {
                    persistLong(calendar.timeInMillis)
                    notifyChanged()
                }
            },
            hourOfDay,
            minute,
            DateFormat.is24HourFormat(context)
        ).show()
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val time: Long = if (defaultValue == null) {
            getPersistedLong(0L)
        } else {
            getPersistedLong(defaultValue as Long)
        }

        val calendar = Calendar.getInstance().setToFirstInstant().apply {
            timeInMillis = time
        }

        hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        minute = calendar.get(Calendar.MINUTE)
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