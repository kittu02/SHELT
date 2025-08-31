package com.example.shelt

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : ComponentActivity() {
    // Static credentials for demo purposes
    private val validEmail = "1@gmail.com"
    private val validPassword = "123456"
    private val staticName = "John Doe"
    private val staticEmail = "user@example.com"
    private val staticPhone = "+911234567890"
    private val staticHelmetId = "A1M2C4N88"

    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var errorText: TextView // Updated to TextView
    private lateinit var loginButton: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apply theme before setting content view
        ThemeUtils.applyThemeFromPrefs(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Initialize views
        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        emailInput = findViewById(R.id.etEmail)
        passwordInput = findViewById(R.id.etPassword)
        errorText = findViewById(R.id.tvError) // No casting needed now
        loginButton = findViewById(R.id.btnLogin)

        setupInputValidation()
        setupLoginButton()
    }

    private fun setupInputValidation() {
        // Email validation
        emailInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { validateEmail() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Password validation
        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { validatePassword() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupLoginButton() {
        loginButton.setOnClickListener {
            if (validateForm()) {
                attemptLogin()
            }
        }
    }

    private fun validateForm(): Boolean {
        val emailValid = validateEmail()
        val passwordValid = validatePassword()
        return emailValid && passwordValid
    }

    private fun validateEmail(): Boolean {
        val email = emailInput.text.toString().trim()
        return when {
            email.isEmpty() -> {
                emailLayout.error = getString(R.string.error_field_required)
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailLayout.error = getString(R.string.error_invalid_email)
                false
            }
            else -> {
                emailLayout.error = null
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        val password = passwordInput.text.toString()
        return when {
            password.isEmpty() -> {
                passwordLayout.error = getString(R.string.error_field_required)
                false
            }
            password.length < 6 -> {
                passwordLayout.error = getString(R.string.error_invalid_password)
                false
            }
            else -> {
                passwordLayout.error = null
                true
            }
        }
    }

    private fun attemptLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

        // Show loading state
        loginButton.isEnabled = false
        errorText.visibility = View.GONE

        // Simulate network request
        loginButton.postDelayed({
            if (email == validEmail && password == validPassword) {
                // Save user data
                val prefs: SharedPreferences = getSharedPreferences("user", MODE_PRIVATE)
                prefs.edit()
                    .putString("name", staticName)
                    .putString("email", staticEmail)
                    .putString("phone", staticPhone)
                    .putString("helmetId", staticHelmetId)
                    .putBoolean("isLoggedIn", true)
                    .apply()

                // Navigate to dashboard
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                // Show error
                errorText.visibility = View.VISIBLE
                errorText.text = getString(R.string.error_invalid_credentials) // Corrected line
                loginButton.isEnabled = true
            }
        }, 1000) // Simulate network delay
    }

    override fun onResume() {
        super.onResume()
        // Re-apply theme in case it was changed in settings
        ThemeUtils.applyThemeFromPrefs(this)
    }
}