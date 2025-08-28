package com.example.shelt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import android.widget.Switch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        val sw = findViewById<Switch>(R.id.switchTheme)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        sw.isChecked = prefs.getBoolean("dark_theme", false)
        sw.setOnCheckedChangeListener { _, isChecked ->
            ThemeUtils.setDark(this, isChecked)
        }
    }
}


