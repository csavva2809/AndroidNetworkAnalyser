package com.example.networkanalyser.presentation.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.networkanalyser.ui.theme.NetworkAnalyserTheme
import com.example.networkanalyser.utils.AppConfig
import com.example.networkanalyser.data.local.DatabaseProvider
import com.example.networkanalyser.data.model.NetworkLog
import com.example.networkanalyser.utils.AnomalyDetector
import com.example.networkanalyser.data.util.uploadAnomalyToFirebase
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import com.example.networkanalyser.presentation.graphs.*
import com.example.networkanalyser.utils.NotificationHelper
import com.example.networkanalyser.data.local.*
import android.app.Activity
import android.os.Build
import androidx.core.app.ActivityCompat
import com.example.networkanalyser.utils.PermissionHelper
import com.example.networkanalyser.utils.NearbyAlertManager

sealed class Screen {
    object Dashboard : Screen()
    object Graphs : Screen()
}

@Composable
fun RequestNotificationPermissionIfNeeded() {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (context is Activity) {
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1001
                    )
                }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!PermissionHelper.hasAllPermissions(this)) {
            PermissionHelper.requestAllPermissions(this)
        }
        NotificationHelper.createNotificationChannel(this)

        setContent {
            NetworkAnalyserTheme {
                RequestNotificationPermissionIfNeeded()
                AppNavigator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigator() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menu", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                Divider()
                NavigationDrawerItem(label = { Text("Dashboard") }, selected = selectedScreen == Screen.Dashboard, onClick = { selectedScreen = Screen.Dashboard })
                NavigationDrawerItem(label = { Text("Graphs") }, selected = selectedScreen == Screen.Graphs, onClick = { selectedScreen = Screen.Graphs })
            }
        }
    ) {
        when (selectedScreen) {
            is Screen.Dashboard -> DashboardScreen()
            is Screen.Graphs -> GraphsScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    var connectionInfo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }

    val dao = remember { DatabaseProvider.getDatabase(context).networkLogDao() }
    var previousLog by remember { mutableStateOf<NetworkLog?>(null) }
    val logs by dao.getAllLogs().collectAsState(initial = emptyList())

    var showLegend by remember { mutableStateOf(false) }

    var alertNearby by remember { mutableStateOf(false) }


    LaunchedEffect(alertNearby) {
        if (alertNearby) {
            NearbyAlertManager.initialize(context)
        }
    }
    LaunchedEffect(Unit) {
        connectionInfo = getNetworkStatus(context)
    }

    LaunchedEffect(isScanning) {
        while (isScanning) {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val scanResult: ScanResult? = try {
                if (hasPermission) {
                    wm.scanResults.find { it.BSSID == wm.connectionInfo.bssid }
                } else null
            } catch (e: SecurityException) {
                null
            }

            val network = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(network)

            val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            val isMobile = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
            val rssi = if (isWifi) wm.connectionInfo.rssi else -100
            val tx = TrafficStats.getTotalTxBytes() / (1024 * 1024)
            val rx = TrafficStats.getTotalRxBytes() / (1024 * 1024)

            val capabilitiesString = scanResult?.capabilities.orEmpty()
            val isSecure = capabilitiesString.contains("WPA") ||
                    capabilitiesString.contains("WPA2") ||
                    capabilitiesString.contains("WPA3") ||
                    capabilitiesString.contains("RSN") ||
                    capabilitiesString.contains("EAP")

            val connectionType = if (isWifi) {
                if (isSecure) "WiFi" else "Unsecured WiFi"
            } else if (isMobile) {
                "Mobile"
            } else {
                "None"
            }

            val currentLog = NetworkLog(
                timestamp = System.currentTimeMillis(),
                connectionType = connectionType,
                signalStrength = rssi,
                dataSentMB = tx,
                dataReceivedMB = rx
            )

            val result = AnomalyDetector.detect(currentLog, previousLog)
            val finalLog = currentLog.copy(isAnomaly = result.isAnomaly)

            dao.insertLog(finalLog)

            if (result.isAnomaly && AppConfig.uploadAnomaliesToFirebase) {
                uploadAnomalyToFirebase(context, finalLog, result)
                NotificationHelper.showAnomalyNotification(
                    context = context,
                    message = "Unusual network behavior at ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(currentLog.timestamp))}"
                )

            }

            previousLog = finalLog
            delay(AppConfig.logIntervalSec * 1000L)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("\uD83D\uDCE1 Network Analyser") },
                actions = {
                    IconButton(onClick = { showLegend = true }) {
                        Text("ℹ️")
                    }
                }
            )
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

                Spacer(modifier = Modifier.height(16.dp))

                if (isScanning) {
                    Button(onClick = { isScanning = false }) {
                        Text("🛑 Stop Scan")
                    }
                } else {
                    Button(onClick = { isScanning = true }) {
                        Text("▶ Start Scan")
                    }
                }

                Button(onClick = {
                    NearbyAlertManager.initialize(context)
                }) {
                    Text("\uD83D\uDEA8 Alert Nearby Devices")
                }
                Divider()

                Text(text = "\uD83D\uDCCB Logs:", style = MaterialTheme.typography.titleMedium)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs.take(10)) { log ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Time: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))}")
                                Text("Type: ${log.connectionType}")
                                Text("Signal: ${log.signalStrength} dBm")
                                Text("Sent: ${log.dataSentMB} MB, Received: ${log.dataReceivedMB} MB")
                                if (log.isAnomaly) {
                                    Text("⚠️ Anomaly Detected", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLegend) {
        AlertDialog(
            onDismissRequest = { showLegend = false },
            title = { Text("Connection Type Legend") },
            text = {
                Text("\u2022 WiFi: Secure WiFi connection (WPA/WPA2/WPA3/Enterprise)\n\n" +
                        "\u2022 Unsecured WiFi: Open, WEP, or poorly configured WiFi\n\n" +
                        "\u2022 Mobile: Mobile data connection (4G/5G)")
            },
            confirmButton = {
                Button(onClick = { showLegend = false }) {
                    Text("OK")
                }
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphsScreen() {
    val context = LocalContext.current
    val dao = remember { DatabaseProvider.getDatabase(context).networkLogDao() }
    val logs by dao.getAllLogs().collectAsState(initial = emptyList())

    var expanded by remember { mutableStateOf(false) }
    var selectedGraph by remember { mutableStateOf("Signal Strength") }
    val graphOptions = listOf("Signal Strength", "Data Sent & Received", "Anomaly Highlight")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("\uD83D\uDCCA Graphs") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    readOnly = true,
                    value = selectedGraph,
                    onValueChange = {},
                    label = { Text("Select Graph") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    graphOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedGraph = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (selectedGraph) {
                "Signal Strength" -> SignalStrengthGraphMP(logs)
                "Data Sent & Received" -> DataSentReceivedGraphMP(logs)
                "Anomaly Highlight" -> AnomalyHighlightGraphMP(logs)
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
