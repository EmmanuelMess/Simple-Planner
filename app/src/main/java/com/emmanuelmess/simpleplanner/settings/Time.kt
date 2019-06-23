package com.emmanuelmess.simpleplanner.settings

import java.util.*

/**
 * Easily saved and restored from [String]
 */
data class Time(val hourOfDay: Int, val minute: Int) {
    override fun toString(): String {
        return "$hourOfDay:$minute"
    }

    fun toCalendar(): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }
    }
}

fun String.toTime(): Time {
    val split = this.split(":")
    return Time(split[0].toInt(), split[1].toInt())
}