package com.example.networkanalyser.data.util

import android.content.Context
import android.net.wifi.WifiManager
import com.example.networkanalyser.data.model.NetworkLog
import com.example.networkanalyser.utils.AppConfig
import com.example.networkanalyser.utils.AnomalyResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun uploadAnomalyToFirebase(context: Context, log: NetworkLog, result: AnomalyResult) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val ssid = wm.connectionInfo.ssid
        .removePrefix("\"")
        .removeSuffix("\"")
        .takeIf { it != "<unknown ssid>" } ?: "Unknown Network"

    val anomalyData = mapOf(
        "userId" to uid,
        "timestamp" to log.timestamp,
        "connectionType" to log.connectionType,
        "networkName" to ssid,
        "signalStrength" to log.signalStrength,
        "dataSentMB" to log.dataSentMB,
        "dataReceivedMB" to log.dataReceivedMB,
        "anomalyTypes" to result.types.map { it.name }
    )

    if (AppConfig.uploadAnomaliesToFirebase) {
        FirebaseFirestore.getInstance()
            .collection("anomalies")
            .add(anomalyData)
    }
}
