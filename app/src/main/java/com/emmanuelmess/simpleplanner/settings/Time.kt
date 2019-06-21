package com.emmanuelmess.simpleplanner.settings

/**
 * Easily saved and restored from [String]
 */
data class Time(val hourOfDay: Int, val minute: Int) {
    override fun toString(): String {
        return "$hourOfDay:$minute"
    }
}

fun String.toTime(): Time {
    val split = this.split(":")
    return Time(split[0].toInt(), split[1].toInt())
}