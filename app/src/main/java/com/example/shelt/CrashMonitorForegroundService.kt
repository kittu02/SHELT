package com.example.shelt

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class CrashMonitorForegroundService : Service() {
    private val TAG = "MonitorService"
    private var crashListener: ListenerRegistration? = null
    private val CHANNEL_ID = "CrashMonitorChannel"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val helmetId = intent?.getStringExtra("helmetId")
        if (helmetId.isNullOrEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Create the notification channel
        createNotificationChannel()

        // Build the notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SHELT is Active")
            .setContentText("Monitoring helmet for crashes...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Start the service in the foreground
        startForeground(1, notification)

        // Start the Firestore listener
        startCrashMonitoring(helmetId)
        
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCrashMonitoring()
        Log.d(TAG, "Service destroyed and listener removed.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Crash Monitor Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun startCrashMonitoring(helmetId: String) {
        stopCrashMonitoring() // Ensure no duplicates
        Log.d(TAG, "Starting crash monitoring for helmet: $helmetId")

        // Initialize the fusedLocationClient here
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        crashListener = FirebaseFirestore.getInstance()
            .collection("helmets")
            .document(helmetId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                val status = snapshot?.getString("CrashStatus")
                if (status == "crash") {
                    Log.d(TAG, "Crash detected! Triggering emergency response.")
                    // Pass fusedLocationClient to the function
                    EmergencyHelper.triggerEmergencyFromPrefs(this, fusedLocationClient)
                }
            }
    }


    private fun stopCrashMonitoring() {
        crashListener?.remove()
        crashListener = null
    }
}