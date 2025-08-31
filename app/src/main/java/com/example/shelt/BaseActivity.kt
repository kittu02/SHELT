package com.example.shelt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Base activity that all activities should extend from.
 * Handles common functionality like theme application.
 */
open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before calling super to ensure proper theming
        ThemeUtils.applyThemeFromPrefs(this)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        // Re-apply theme in case it was changed in settings
        ThemeUtils.applyThemeFromPrefs(this)
    }
}
