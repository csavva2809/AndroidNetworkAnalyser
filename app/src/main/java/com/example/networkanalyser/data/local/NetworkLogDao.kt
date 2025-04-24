package com.example.networkanalyser.data.local

import androidx.room.*
import com.example.networkanalyser.data.model.NetworkLog
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NetworkLog)

    @Query("SELECT * FROM network_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<NetworkLog>>

    @Query("DELETE FROM network_logs")
    suspend fun clearAllLogs()
}
