package com.example.shelt

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.shelt.data.FirebaseHelmetRepository
import com.example.shelt.data.HelmetRepository
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var repository: HelmetRepository
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        repository = FirebaseHelmetRepository(getSharedPreferences("user", MODE_PRIVATE))

        val profileBtn = findViewById<ImageButton>(R.id.btnProfile)
        val statusCircle = findViewById<MaterialCardView>(R.id.cardStatus)
        val statusText = findViewById<TextView>(R.id.txtStatus)

        profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btnEmergency).setOnClickListener {
            startActivity(Intent(this, EmergencyActivity::class.java))
        }
        findViewById<android.view.View>(R.id.btnNavigation).setOnClickListener {
            startActivity(Intent(this, NavigationActivity::class.java))
        }
        findViewById<android.view.View>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<android.view.View>(R.id.btnAbout).setOnClickListener {
            AboutDialogFragment().show(supportFragmentManager, "about")
        }

        scope.launch {
            repository.observePiStatus().collectLatest { active ->
                val connected = active == true
                statusText.text = if (connected) getString(R.string.connected) else getString(R.string.disconnected)
                statusCircle.strokeColor = ContextCompat.getColor(this@DashboardActivity, if (connected) R.color.green else R.color.red)
                findViewById<android.view.View>(R.id.btnNavigation).isEnabled = connected
            }
        }

        scope.launch {
            repository.observeCrashStatus().collectLatest { status ->
                if (status == "crash") {
                    EmergencyHelper.triggerEmergencyFromPrefs(this@DashboardActivity)
                    Snackbar.make(statusCircle, getString(R.string.crash_detected), Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}


