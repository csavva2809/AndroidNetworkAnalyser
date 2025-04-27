package com.example.networkanalyser.utils

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.widget.Toast
import androidx.core.app.ActivityCompat

object NearbyAlertManager {

    private var manager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null
    private lateinit var receiver: BroadcastReceiver
    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
    }

    fun initialize(context: Context) {
        // Permissions check
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Missing permissions for Nearby Alerts.", Toast.LENGTH_SHORT).show()
            return
        }

        manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager?.initialize(context, context.mainLooper, null)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                        try {
                            manager?.requestPeers(channel) { peers ->
                                peers.deviceList.forEach { device ->
                                    if (context != null) {
                                        sendAlertToDevice(context, device)
                                    }
                                }
                            }
                        } catch (e: SecurityException) {
                            Toast.makeText(context, "Permission error: Cannot access peers.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        try {
            context.registerReceiver(receiver, intentFilter)
        } catch (e: SecurityException) {
            Toast.makeText(context, "Permission error: Cannot register receiver.", Toast.LENGTH_SHORT).show()
        }

        discoverPeers(context)
    }

    private fun discoverPeers(context: Context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Cannot discover peers without permission.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Toast.makeText(context, "🔍 Searching for nearby devices...", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(reason: Int) {
                    Toast.makeText(context, "❌ Failed to search for devices.", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: SecurityException) {
            Toast.makeText(context, "Permission error: Cannot discover peers.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendAlertToDevice(context: Context, device: WifiP2pDevice) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "No permission to send alert.", Toast.LENGTH_SHORT).show()
            return
        }

        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }

        try {
            manager?.connect(channel, config, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Toast.makeText(context, "✅ Alert sent to: ${device.deviceName}", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(reason: Int) {
                    Toast.makeText(context, "❌ Failed to alert ${device.deviceName}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: SecurityException) {
            Toast.makeText(context, "Permission error: Cannot send alert to device.", Toast.LENGTH_SHORT).show()
        }
    }

    fun cleanup(context: Context) {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            // Receiver already unregistered or never registered
        } catch (e: SecurityException) {
            // Handle potential security exception too
        }
    }
}
