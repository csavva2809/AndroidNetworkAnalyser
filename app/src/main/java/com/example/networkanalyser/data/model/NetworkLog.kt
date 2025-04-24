package com.example.networkanalyser.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_logs")
data class NetworkLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val connectionType: String,
    val signalStrength: Int, // in dBm
    val dataSentMB: Long,
    val dataReceivedMB: Long,
    val isAnomaly: Boolean = false
)
{
}