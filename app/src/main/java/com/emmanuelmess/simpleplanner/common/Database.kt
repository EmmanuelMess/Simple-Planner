package com.emmanuelmess.simpleplanner.common

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Database
import androidx.room.RoomDatabase
import com.emmanuelmess.simpleplanner.SimplePlannerApplication
import com.emmanuelmess.simpleplanner.events.EventDao
import com.emmanuelmess.simpleplanner.events.EventEntity

@Database(entities = [EventEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "central-database"
    }

    abstract fun eventDao(): EventDao
}

@SuppressLint("Registered")
open class AppDatabaseAwareActivity: AppCompatActivity() {
    val database: AppDatabase get() = (application as SimplePlannerApplication).db
}

class NoDatabaseException: RuntimeException("The database doesn't exist anymore!")