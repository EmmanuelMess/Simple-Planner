package com.emmanuelmess.simpleplanner.common

import java.util.*

fun Calendar.setToFirstInstant() = this.apply { set(1970, 0, 1, 0, 0, 0) }

fun Calendar.setToFirstDay() = this.apply { set(1970, 0, 1) }