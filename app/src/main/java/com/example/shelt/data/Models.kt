package com.example.shelt.data

data class LocationPoint(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class DeviceStatus(
    val appStatus: String? = null,
    val crashStatus: String? = null,
    val piStatus: String? = null
)

data class HelmetSnapshot(
    val crashStatus: String? = null,
    val deviceStatus: DeviceStatus? = null,
    val currentLocation: LocationPoint? = null,
    val searchedLocation: LocationPoint? = null
)


