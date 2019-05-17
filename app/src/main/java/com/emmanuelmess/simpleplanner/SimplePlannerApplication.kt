package com.emmanuelmess.simpleplanner

import android.app.Application
import androidx.room.Room.databaseBuilder
import com.emmanuelmess.simpleplanner.common.AppDatabase
import com.emmanuelmess.simpleplanner.common.AppDatabase.Companion.DATABASE_NAME

class SimplePlannerApplication: Application() {
    lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()

        db = databaseBuilder(
            applicationContext,
            AppDatabase::class.java, DATABASE_NAME
        ).build()
    }
}