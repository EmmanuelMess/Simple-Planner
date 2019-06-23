package com.emmanuelmess.simpleplanner.common

import java.util.*

fun Calendar.setToFirstInstant() = this.apply { set(1970, 0, 1, 0, 0, 0) }

fun Calendar.setToFirstDay() = this.apply { set(1970, 0, 1) }

var Calendar.calendarHourOfDay: Int
    set(value) {
        set(Calendar.HOUR_OF_DAY, value)
    }
    get() = get(Calendar.HOUR_OF_DAY)

var Calendar.calendarMinute: Int
    set(value) {
        set(Calendar.MINUTE, value)
    }
    get() = get(Calendar.MINUTE)