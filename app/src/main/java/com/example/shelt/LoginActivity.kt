package com.example.shelt

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.google.android.material.snackbar.Snackbar

class LoginActivity : ComponentActivity() {
    private val staticName = "John Doe"
    private val staticEmail = "john@example.com"
    private val staticPhone = "+911234567890"
    private val staticHelmetId = "A1M2C4N88"
    private val staticPassword = "password123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        findViewById<android.view.View>(R.id.btnLogin).setOnClickListener {
            // For now accept static credentials
            val prefs: SharedPreferences = getSharedPreferences("user", MODE_PRIVATE)
            prefs.edit()
                .putString("name", staticName)
                .putString("email", staticEmail)
                .putString("phone", staticPhone)
                .putString("helmetId", staticHelmetId)
                .apply()
            Snackbar.make(it, "Logged in as $staticName", Snackbar.LENGTH_SHORT).show()
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }
}


