package com.example.shelt

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.shelt.data.FirebaseHelmetRepository
import com.example.shelt.data.HelmetRepository
import com.google.android.gms.location.LocationServices
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var repository: HelmetRepository
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var helmetId: String? = null

    private lateinit var tvCrashStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtils.applyThemeFromPrefs(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        helmetId = prefs.getString("helmetId", null)
        repository = FirebaseHelmetRepository(prefs)

        val profileBtn = findViewById<View>(R.id.btnProfile)
        val statusCircle = findViewById<MaterialCardView>(R.id.cardStatus)
        val statusText = findViewById<TextView>(R.id.txtStatus)
        tvCrashStatus = findViewById(R.id.tvCrashStatus)

        profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<View>(R.id.btnEmergency).setOnClickListener {
            startActivity(Intent(this, EmergencyActivity::class.java))
        }

        findViewById<View>(R.id.btnNavigation).setOnClickListener {
            startActivity(Intent(this, NavigationActivity::class.java))
        }

        findViewById<View>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<View>(R.id.btnAbout).setOnClickListener {
            AboutDialogFragment().show(supportFragmentManager, "about")
        }

        scope.launch {
            repository.observePiStatus().collectLatest { active ->
                val connected = active == true
                statusText.text = if (connected) getString(R.string.connected) else getString(R.string.disconnected)
                statusCircle.strokeColor = ContextCompat.getColor(
                    this@DashboardActivity,
                    if (connected) R.color.green else R.color.red
                )
                findViewById<View>(R.id.btnNavigation).isEnabled = connected
            }
        }

        // Observe CrashStatus and update the TextView
        scope.launch {
            repository.observeCrashStatus().collectLatest { status ->
                when (status) {
                    "crash" -> {
                        tvCrashStatus.text = "Crash Status: Crash Detected!"
                        tvCrashStatus.setTextColor(ContextCompat.getColor(this@DashboardActivity, R.color.notification_error))
                        Snackbar.make(
                            statusCircle,
                            "Crash Detected!",
                            Snackbar.LENGTH_LONG
                        ).show()

                        // FIX: Initialize the FusedLocationProviderClient and pass it to the helper function.
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@DashboardActivity)
                        EmergencyHelper.triggerEmergencyFromPrefs(this@DashboardActivity, fusedLocationClient)
                    }
                    "safe" -> {
                        tvCrashStatus.text = "Crash Status: Safe"
                        tvCrashStatus.setTextColor(ContextCompat.getColor(this@DashboardActivity, R.color.green))
                    }
                    else -> {
                        tvCrashStatus.text = "Crash Status: Unknown"
                        tvCrashStatus.setTextColor(ContextCompat.getColor(this@DashboardActivity, R.color.text_secondary))
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}