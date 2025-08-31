package com.example.shelt

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        val prefs = getSharedPreferences("user", MODE_PRIVATE)
        findViewById<TextView>(R.id.txtProfileName).text = prefs.getString("name", "")
        findViewById<TextView>(R.id.txtProfileEmail).text = prefs.getString("email", "")
        findViewById<TextView>(R.id.txtProfilePhone).text = prefs.getString("phone", "")
        findViewById<TextView>(R.id.txtProfileHelmet).text = prefs.getString("helmetId", "")

        findViewById<android.view.View>(R.id.btnEditProfile).setOnClickListener {
            EditProfileDialogFragment().show(supportFragmentManager, "edit_profile")
        }
    }
}
