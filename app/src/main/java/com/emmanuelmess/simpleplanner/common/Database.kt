package com.emmanuelmess.simpleplanner.common

import androidx.room.Database
import androidx.room.RoomDatabase
import com.emmanuelmess.simpleplanner.events.EventDao
import com.emmanuelmess.simpleplanner.events.EventEntity

@Database(entities = [EventEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "central-database"
    }

    abstract fun eventDao(): EventDao
}
