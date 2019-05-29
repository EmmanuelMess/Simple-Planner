package com.emmanuelmess.simpleplanner.common

import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.View
import android.widget.TimePicker
import androidx.core.content.ContextCompat.getDrawable
import com.emmanuelmess.simpleplanner.R
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE

class TimeChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): Chip(context, attrs) {
    var callback: (() -> Unit)? = null
    var hourOfDay = 10
        private set
    var minute = 0
        private set

    init {
        chipIcon = getDrawable(context, R.drawable.ic_access_time_black_24dp)

        setChipBackgroundColorResource(R.color.colorAccent)

        setOnClickListener(::onClick)

        reloadView()
    }

    var error: Boolean? = null
        set(value) {
            if(value != null) {
                setChipBackgroundColorResource(R.color.colorError)
            } else {
                setChipBackgroundColorResource(R.color.colorAccent)
            }
            field = value
        }

    private fun onClick(view: View) {
        TimePickerDialog(
            context,
            {_: TimePicker?, hourOfDay: Int, minute: Int ->
                this.hourOfDay = hourOfDay
                this.minute = minute
                reloadView()
                callback?.invoke()
            },
            hourOfDay,
            minute,
            DateFormat.is24HourFormat(context)
        ).show()
    }

    private fun reloadView() {
        val calendar = Calendar.getInstance().setToFirstDay().apply {
            set(HOUR_OF_DAY, hourOfDay)
            set(MINUTE, minute)
        }
        text = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(calendar.time)
    }
}