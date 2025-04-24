package com.example.networkanalyser.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.networkanalyser.data.model.NetworkLog

@Database(entities = [NetworkLog::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun networkLogDao(): NetworkLogDao
}
