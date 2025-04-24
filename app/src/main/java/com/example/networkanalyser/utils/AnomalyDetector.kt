package com.example.networkanalyser.utils

import com.example.networkanalyser.data.model.NetworkLog

data class AnomalyResult(
    val isAnomaly: Boolean,
    val types: List<AnomalyType>
)

enum class AnomalyType {
    LOW_SIGNAL,
    HIGH_DATA_USAGE,
    UNSECURED_WIFI
}

object AnomalyDetector {

    fun detect(current: NetworkLog, previous: NetworkLog?): AnomalyResult {
        val detected = mutableListOf<AnomalyType>()

        if (current.signalStrength < AppConfig.signalThresholdDbm) {
            detected.add(AnomalyType.LOW_SIGNAL)
        }

        val highUsage = previous?.let {
            (current.dataSentMB - it.dataSentMB > AppConfig.dataSpikeThresholdMB) ||
                    (current.dataReceivedMB - it.dataReceivedMB > AppConfig.dataSpikeThresholdMB)
        } ?: false

        if (highUsage) {
            detected.add(AnomalyType.HIGH_DATA_USAGE)
        }

        if (current.connectionType == "Unsecured WiFi" && AppConfig.alertOnUnsecuredWiFi) {
            detected.add(AnomalyType.UNSECURED_WIFI)
        }

        return AnomalyResult(
            isAnomaly = detected.isNotEmpty(),
            types = detected
        )
    }
}
