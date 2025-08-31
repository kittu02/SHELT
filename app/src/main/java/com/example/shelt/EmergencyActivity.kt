package com.example.shelt

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity // Add this import

// Change parent class to AppCompatActivity
class EmergencyActivity : AppCompatActivity() {
    private lateinit var adapter: ArrayAdapter<String>
    private val numbers = mutableListOf<String>()
    private lateinit var primaryNumber: String
    private val SMS_PERMISSION_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtils.applyThemeFromPrefs(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_emergency)

        // Initialize UI components
        val listView = findViewById<android.widget.ListView>(R.id.listNumbers)
        val btnAdd = findViewById<View>(R.id.btnAdd)
        val btnRemove = findViewById<View>(R.id.btnRemove)
        val btnSetPrimary = findViewById<View>(R.id.btnSetPrimary)
        val btnTestSMS = findViewById<View>(R.id.btnTestSMS)

        // Load saved numbers
        val prefs = getSharedPreferences("emergency", MODE_PRIVATE)
        numbers.addAll(prefs.getStringSet("numbers", mutableSetOf()) ?: mutableSetOf())
        primaryNumber = prefs.getString("primary", "") ?: ""

        // Setup adapter
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, numbers)
        listView.adapter = adapter
        listView.choiceMode = android.widget.ListView.CHOICE_MODE_SINGLE

        // Set click listeners
        btnAdd.setOnClickListener { showAddNumberDialog() }
        btnRemove.setOnClickListener { removeSelectedNumber(listView) }
        btnSetPrimary.setOnClickListener { setPrimaryNumber(listView) }
        btnTestSMS.setOnClickListener { testEmergencySMS() }
    }

    private fun showAddNumberDialog() {
        val input = EditText(this).apply {
            hint = "Enter phone number with country code"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Emergency Contact")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val number = input.text.toString().trim()
                if (number.isNotEmpty()) {
                    if (!numbers.contains(number)) {
                        numbers.add(number)
                        saveNumbers()
                        adapter.notifyDataSetChanged()
                        showSnackbar("Contact added")
                    } else {
                        showSnackbar("Number already exists")
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun removeSelectedNumber(listView: android.widget.ListView) {
        val position = listView.checkedItemPosition
        if (position != android.widget.AdapterView.INVALID_POSITION) {
            val removedNumber = numbers[position]
            numbers.removeAt(position)
            if (removedNumber == primaryNumber) {
                primaryNumber = ""
                getSharedPreferences("emergency", MODE_PRIVATE).edit()
                    .remove("primary")
                    .apply()
            }
            saveNumbers()
            adapter.notifyDataSetChanged()
            showSnackbar("Contact removed")
        } else {
            showSnackbar("Please select a contact to remove")
        }
    }

    private fun setPrimaryNumber(listView: android.widget.ListView) {
        val position = listView.checkedItemPosition
        if (position != android.widget.AdapterView.INVALID_POSITION) {
            primaryNumber = numbers[position]
            getSharedPreferences("emergency", MODE_PRIVATE).edit()
                .putString("primary", primaryNumber)
                .apply()
            showSnackbar("Primary contact set")
        } else {
            showSnackbar("Please select a contact to set as primary")
        }
    }

    private fun testEmergencySMS() {
        if (checkSmsPermission()) {
            sendTestEmergencySMS()
        } else {
            requestSmsPermission()
        }
    }

    private fun checkSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.SEND_SMS),
            SMS_PERMISSION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendTestEmergencySMS()
            } else {
                showSnackbar("SMS permission is required to test emergency alerts")
            }
        }
    }

    private fun sendTestEmergencySMS() {
        if (numbers.isEmpty()) {
            showSnackbar("Please add at least one emergency contact")
            return
        }

        val message = "TEST: SHELT - This is a test emergency alert. If you receive this, the emergency alert system is working correctly."

        try {
            val smsManager = SmsManager.getDefault()
            numbers.forEach { number ->
                smsManager.sendTextMessage(number, null, message, null, null)
            }
            showSnackbar("Test alert sent to ${numbers.size} contact(s)")
        } catch (e: Exception) {
            showSnackbar("Failed to send test alert: ${e.message}")
        }
    }

    private fun saveNumbers() {
        getSharedPreferences("emergency", MODE_PRIVATE).edit()
            .putStringSet("numbers", numbers.toSet())
            .apply()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        ThemeUtils.applyThemeFromPrefs(this)
    }
}