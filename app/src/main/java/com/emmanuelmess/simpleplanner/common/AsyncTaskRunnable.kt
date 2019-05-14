package com.emmanuelmess.simpleplanner.common

import android.os.AsyncTask
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread

class AsyncTaskRunnable<Result>(
    startImmediatly: Boolean = true,
    @WorkerThread val onBackground: AsyncTask<Unit, Unit, Result>.() -> Result?,
    @MainThread val onPostExcecute: (Result) -> Unit
): AsyncTask<Unit, Unit, Result>() {

    init {
        if(startImmediatly) {
            execute()
        }
    }

    override fun doInBackground(vararg params: Unit?) = onBackground()

    override fun onPostExecute(result: Result) = onPostExcecute(result)
}