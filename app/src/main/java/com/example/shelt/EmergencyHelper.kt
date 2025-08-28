package com.example.shelt

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager

object EmergencyHelper {
    fun triggerEmergencyFromPrefs(context: Context) {
        val prefs = context.getSharedPreferences("emergency", Context.MODE_PRIVATE)
        val numbers = prefs.getStringSet("numbers", emptySet()) ?: emptySet()
        val primary = prefs.getString("primary", null)
        val message = "SHELT: Crash detected at your contact's helmet."
        numbers.forEach { sendSms(context, it, message) }
        primary?.let { startCall(context, it) }
    }

    private fun sendSms(context: Context, number: String, message: String) {
        val manager = SmsManager.getDefault()
        manager.sendTextMessage(number, null, message, null, null)
    }

    private fun startCall(context: Context, number: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}


