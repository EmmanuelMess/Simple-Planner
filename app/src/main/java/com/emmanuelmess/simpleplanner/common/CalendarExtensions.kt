package com.emmanuelmess.simpleplanner.common

import java.util.*

fun Calendar.setToFirstDay() = this.apply { set(0, 0, 0, 0, 0, 0) }