package com.example.shelt

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.shelt.data.FirebaseHelmetRepository
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Locale

class NavigationActivity : AppCompatActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_navigation)

        tvCurrent = findViewById(R.id.tvCurrent)
        val etDestination = findViewById<EditText>(R.id.etDestination)

        // Start crash monitoring service
        CrashMonitorService.startService(this)

        findViewById<View>(R.id.btnGo).setOnClickListener {
            val dest = etDestination.text.toString()
            val geo = Geocoder(this, Locale.getDefault())
            val addrs = geo.getFromLocationName(dest, 1)
            if (!addrs.isNullOrEmpty()) {
                scope.launch { repo.setSearchedLocation(addrs[0].latitude, addrs[0].longitude) }
            }
            startBackgroundLocationService()
        }

        // Initial one-time read if permission already granted
        if (hasLocation()) {
            locationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    tvCurrent.text = "${loc.latitude}, ${loc.longitude}"
                    scope.launch { repo.updateCurrentLocation(loc.latitude, loc.longitude) }
                }
            }
        }
    }

    private fun hasLocation(): Boolean {
        val f = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val c = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return f || c
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), requestCode)
    }

    private fun startBackgroundLocationService() {
        if (!hasLocation()) {
            requestLocationPermission()
            return
        }
        
        // Start background service for continuous location updates
        BackgroundLocationService.startService(this)
        
        // Also start local updates for immediate UI feedback
        if (!started) {
            started = true
            val request = LocationRequest.Builder(5000)
                .setMinUpdateIntervalMillis(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .build()
            locationClient.requestLocationUpdates(request, callback, mainLooper)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.requestCode && grantResults.isNotEmpty() && grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
            startBackgroundLocationService()
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


