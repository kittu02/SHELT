package com.example.shelt.data

import kotlinx.coroutines.flow.Flow

interface HelmetRepository {
    fun observePiStatus(): Flow<Boolean?>
    fun observeCrashStatus(): Flow<String?>
    suspend fun updateCurrentLocation(lat: Double, lng: Double)
    suspend fun setSearchedLocation(lat: Double, lng: Double)
}


