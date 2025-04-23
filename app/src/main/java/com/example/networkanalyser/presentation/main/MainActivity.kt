package com.example.networkanalyser.presentation.main

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.networkanalyser.ui.theme.NetworkAnalyserTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NetworkAnalyserTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DashboardScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    var connectionInfo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        connectionInfo = getNetworkStatus(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("\uD83D\uDCE1 Network Analyser") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(text = "Current Network Status:", style = MaterialTheme.typography.titleMedium)
                Text(text = connectionInfo)

                Button(onClick = {
                    isLoading = true
                    connectionInfo = getNetworkStatus(context)
                    isLoading = false
                }) {
                    Text("Refresh Info")
                }

                Divider()

                Text(text = "\uD83D\uDD27 Coming Next:", style = MaterialTheme.typography.titleMedium)
                Text("• Threat Detection")
                Text("• Traffic Logging & Graphs")
                Text("• Nearby Device Alerts")
            }
        }
    }
}

fun getNetworkStatus(context: Context): String {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    val network = cm.activeNetwork
    val capabilities = cm.getNetworkCapabilities(network)
    val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    val isMobile = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

    val rssi = if (isWifi) "${wm.connectionInfo.rssi} dBm" else "N/A"
    val tx = TrafficStats.getTotalTxBytes() / (1024 * 1024)
    val rx = TrafficStats.getTotalRxBytes() / (1024 * 1024)

    return buildString {
        appendLine("Connection: ${if (isWifi) "WiFi" else if (isMobile) "Mobile" else "None"}")
        appendLine("Signal Strength: $rssi")
        appendLine("Data Sent: $tx MB")
        appendLine("Data Received: $rx MB")
    }
}
