package com.example.networkanalyser.presentation.graphs

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.networkanalyser.data.model.NetworkLog
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun AnomalyHighlightGraphMP(logs: List<NetworkLog>) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
        LineChart(context).apply {
            val normalEntries = logs.takeLast(30)
                .filter { !it.isAnomaly }
                .mapIndexed { index, log -> Entry(index.toFloat(), log.signalStrength.toFloat()) }

            val anomalyEntries = logs.takeLast(30)
                .filter { it.isAnomaly }
                .mapIndexed { index, log -> Entry(index.toFloat(), log.signalStrength.toFloat()) }

            val normalDataSet = LineDataSet(normalEntries, "Normal").apply {
                color = Color.GRAY
                valueTextColor = Color.GRAY
                lineWidth = 2f
            }

            val anomalyDataSet = LineDataSet(anomalyEntries, "Anomalies").apply {
                color = Color.RED
                valueTextColor = Color.RED
                circleRadius = 5f
                setCircleColor(Color.RED)
            }

            data = LineData(normalDataSet, anomalyDataSet)

            description = Description().apply {
                text = "Anomaly Highlight"
            }

            animateX(1000)
        }
    })
}
