package com.example.shelt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.shelt.data.FirebaseHelmetRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CrashMonitorService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val repo by lazy { FirebaseHelmetRepository(getSharedPreferences("user", MODE_PRIVATE)) }

    companion object {
        private const val CHANNEL_ID = "crash_monitor"
        private const val NOTIFICATION_ID = 1002
        private const val MONITOR_INTERVAL = 3000L // Check every 3 seconds

        fun startService(context: android.content.Context) {
            val intent = Intent(context, CrashMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: android.content.Context) {
            context.stopService(Intent(context, CrashMonitorService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startCrashMonitoring()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Crash Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitoring crash status for SHELT"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SHELT Crash Monitor")
            .setContentText("Monitoring helmet crash status")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startCrashMonitoring() {
        // Initialize the location client here
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        scope.launch {
            repo.observeCrashStatus().collectLatest { crashStatus ->
                if (crashStatus == "crash") {
                    // Pass the newly created client to the helper function
                    EmergencyHelper.triggerEmergencyFromPrefs(this@CrashMonitorService, fusedLocationClient)
                    // Update notification to show crash detected
                    updateNotification("CRASH DETECTED! Emergency contacts notified")
                }
                delay(MONITOR_INTERVAL)
            }
        }
    }

    private fun updateNotification(content: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SHELT Crash Monitor")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}