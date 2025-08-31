package com.example.shelt

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object EmergencyHelper {
    private const val TAG = "EmergencyHelper"
    private var crashListener: ListenerRegistration? = null
    // FusedLocationProviderClient is now initialized in the functions that need it,
    // so it doesn't need to be lateinit anymore.

    /**
     * Start monitoring for crash events
     */
    fun startCrashMonitoring(context: Context, helmetId: String) {
        stopCrashMonitoring() // Stop any existing listeners

        // The fusedLocationClient is now created here when monitoring starts.
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        crashListener = FirebaseFirestore.getInstance()
            .collection("helmets")
            .document(helmetId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                // Correctly access the field with a capital C
                val status = snapshot?.getString("CrashStatus")
                if (status == "crash") {
                    Log.d(TAG, "Crash detected!")
                    // Pass the client to the function that needs it
                    triggerEmergencyFromPrefs(context, fusedLocationClient)
                }
            }
    }

    /**
     * Stop monitoring for crash events
     */
    fun stopCrashMonitoring() {
        crashListener?.remove()
        crashListener = null
    }

    /**
     * Trigger emergency response with current location
     * Now accepts FusedLocationProviderClient as a parameter
     */
    fun triggerEmergencyFromPrefs(context: Context, fusedLocationClient: FusedLocationProviderClient) {
        // Get emergency contacts
        val prefs = context.getSharedPreferences("emergency", Context.MODE_PRIVATE)
        val numbers = prefs.getStringSet("numbers", emptySet()) ?: emptySet()
        val primaryNumber = prefs.getString("primary", null)

        if (numbers.isEmpty() && primaryNumber == null) {
            Log.w(TAG, "No emergency contacts found")
            return
        }

        // Get current location
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    val locationText = if (location != null) {
                        // Use a correct Google Maps URL format
                        val mapsUrl = "https://www.google.com/maps?q=${location.latitude},${location.longitude}"
                        "Location: $mapsUrl"
                    } else {
                        "Location unavailable"
                    }

                    val message = "EMERGENCY: Crash detected! $locationText"

                    // Send to all emergency contacts
                    numbers.forEach { number ->
                        sendSms(context, number, message)
                    }

                    // Also send to primary contact if different
                    primaryNumber?.takeIf { !numbers.contains(it) }?.let { number ->
                        sendSms(context, number, "[PRIMARY] $message")
                    }

                    // Start emergency call to primary contact if available
                    primaryNumber?.let { startCall(context, it) }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error getting location", e)
                    // Send without location if we can't get it
                    val message = "EMERGENCY: Crash detected! Unable to get location."
                    numbers.forEach { number -> sendSms(context, number, message) }
                    primaryNumber?.let { startCall(context, it) }
                }
        } else {
            // Location permission not granted, send without location
            val message = "EMERGENCY: Crash detected! Location permission not granted."
            numbers.forEach { number -> sendSms(context, number, message) }
            primaryNumber?.let { startCall(context, it) }
        }
    }

    private fun sendSms(context: Context, number: String, message: String) {
        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val smsManager = context.getSystemService(SmsManager::class.java)
                smsManager.sendTextMessage(number, null, message, null, null)
                Log.d(TAG, "SMS sent to $number")
            } else {
                Log.e(TAG, "SMS permission not granted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to $number", e)
        }
    }

    private fun startCall(context: Context, number: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$number")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start call to $number", e)
        }
    }
}