package com.example.networkanalyser.presentation.graphs

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.example.networkanalyser.data.model.NetworkLog
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun SignalStrengthGraphMP(logs: List<NetworkLog>) {
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { ctx ->
            LineChart(ctx).apply {
                val entries = logs.takeLast(30).mapIndexed { index, log ->
                    Entry(index.toFloat(), log.signalStrength.toFloat())
                }

                val dataSet = LineDataSet(entries, "Signal Strength (dBm)").apply {
                    color = Color.BLUE
                    valueTextColor = Color.BLACK
                    lineWidth = 2f
                    circleRadius = 3f
                    setCircleColor(Color.RED)
                    setDrawValues(true)
                }

                data = LineData(dataSet)
                description.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                axisRight.isEnabled = false
                legend.isEnabled = false
                invalidate()
            }
        }
    )
}
