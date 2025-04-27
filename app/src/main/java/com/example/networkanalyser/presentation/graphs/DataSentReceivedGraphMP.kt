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
fun DataSentReceivedGraphMP(logs: List<NetworkLog>) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
        LineChart(context).apply {
            val sentEntries = logs.takeLast(30)
                .mapIndexed { index, log -> Entry(index.toFloat(), log.dataSentMB.toFloat()) }

            val receivedEntries = logs.takeLast(30)
                .mapIndexed { index, log -> Entry(index.toFloat(), log.dataReceivedMB.toFloat()) }

            val sentDataSet = LineDataSet(sentEntries, "Data Sent (MB)").apply {
                color = Color.BLUE
                valueTextColor = Color.BLUE
                lineWidth = 2f
            }

            val receivedDataSet = LineDataSet(receivedEntries, "Data Received (MB)").apply {
                color = Color.GREEN
                valueTextColor = Color.GREEN
                lineWidth = 2f
            }

            data = LineData(sentDataSet, receivedDataSet)

            description = Description().apply {
                text = "Sent vs Received"
            }

            animateX(1000)
        }
    })
}
