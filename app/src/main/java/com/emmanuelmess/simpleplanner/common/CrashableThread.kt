package com.emmanuelmess.simpleplanner.common

import android.os.Handler
import android.os.Looper
import androidx.annotation.WorkerThread

@WorkerThread
inline fun <T> crashOnMainThread(block: () -> T): T {
    try {
        return block()
    } catch (e: Exception) {
        Handler(Looper.getMainLooper()).post { throw e }
        throw e
    }
}