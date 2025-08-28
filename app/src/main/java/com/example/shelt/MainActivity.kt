package com.example.shelt

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtils.applyThemeFromPrefs(this)
        enableEdgeToEdge()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}