package com.example.shelt

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.shelt.data.FirebaseHelmetRepository
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

class NavigationActivity : BaseActivity() {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val locationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val repo by lazy { FirebaseHelmetRepository(getSharedPreferences("user", MODE_PRIVATE)) }
    private lateinit var tvCurrent: TextView
    private var started = false
    private val requestCode = 1001

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            tvCurrent.text = "${loc.latitude}, ${loc.longitude}"
            scope.launch { repo.updateCurrentLocation(loc.latitude, loc.longitude) }
        }
    }

    // Request codes for permission and location settings dialog
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private val REQUEST_CHECK_SETTINGS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        tvCurrent = findViewById(R.id.tvCurrent)
        val etDestination = findViewById<EditText>(R.id.etDestination)

        // Check for location permissions and GPS status on startup
        checkLocationPermissionsAndSettings()

        // Set up back button
        findViewById<View>(R.id.btnBack).setOnClickListener {
            onBackPressed()
        }

        findViewById<View>(R.id.btnGo).setOnClickListener {
            val dest = etDestination.text.toString()
            val geo = Geocoder(this, Locale.getDefault())
            val addrs = geo.getFromLocationName(dest, 1)
            if (!addrs.isNullOrEmpty()) {
                scope.launch { repo.setSearchedLocation(addrs[0].latitude, addrs[0].longitude) }
            }
            startBackgroundLocationService()
        }
    }

    private fun checkLocationPermissionsAndSettings() {
        if (hasLocationPermissions()) {
            checkLocationSettings()
        } else {
            requestLocationPermissions()
        }
    }

    private fun hasLocationPermissions(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fineLocationGranted && coarseLocationGranted
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun checkLocationSettings() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // Location settings are satisfied. Start location updates.
            onLocationReady()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun onLocationReady() {
        Snackbar.make(findViewById(android.R.id.content), "Location is ready!", Snackbar.LENGTH_SHORT).show()
        // Initial one-time read if permission already granted
        locationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                tvCurrent.text = "${loc.latitude}, ${loc.longitude}"
                scope.launch { repo.updateCurrentLocation(loc.latitude, loc.longitude) }
            }
        }
    }

    private fun startBackgroundLocationService() {
        if (!hasLocationPermissions()) {
            requestLocationPermissions()
            return
        }

        BackgroundLocationService.startService(this)

        if (!started) {
            started = true
            val request = LocationRequest.Builder(5000)
                .setMinUpdateIntervalMillis(5000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()
            locationClient.requestLocationUpdates(request, callback, mainLooper)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions were granted, now check location settings
                checkLocationSettings()
            } else {
                // Permissions were denied, go back to Dashboard
                Snackbar.make(findViewById(android.R.id.content), "Location permissions are required for navigation.", Snackbar.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                // User enabled location, proceed
                onLocationReady()
            } else {
                // User did not enable location, go back to Dashboard
                Snackbar.make(findViewById(android.R.id.content), "GPS must be enabled for navigation.", Snackbar.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (started) {
            locationClient.removeLocationUpdates(callback)
            started = false
        }
    }
}