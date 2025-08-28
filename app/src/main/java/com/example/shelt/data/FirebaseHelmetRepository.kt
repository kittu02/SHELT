package com.example.shelt.data

import android.content.SharedPreferences
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseHelmetRepository(private val prefs: SharedPreferences) : HelmetRepository {
    private fun helmetDoc(): DocumentReference {
        val helmetId = prefs.getString("helmetId", "A1M2C4N88") ?: "A1M2C4N88"
        return Firebase.firestore.collection("helmet_locations").document(helmetId)
    }

    override fun observePiStatus(): Flow<Boolean?> = callbackFlow {
        val reg = helmetDoc().addSnapshotListener { snap, _ ->
            val active = snap?.get("DeviceStatus.PiStatus") as? String
            trySend(active?.equals("active", ignoreCase = true))
        }
        awaitClose { reg.remove() }
    }

    override fun observeCrashStatus(): Flow<String?> = callbackFlow {
        val reg = helmetDoc().addSnapshotListener { snap, _ ->
            val status = snap?.get("DeviceStatus.CrashStatus") as? String
            trySend(status)
        }
        awaitClose { reg.remove() }
    }

    override suspend fun updateCurrentLocation(lat: Double, lng: Double) {
        val data = mapOf(
            "CurrentLocation" to mapOf(
                "latitude" to lat,
                "longitude" to lng,
                "timestamp" to System.currentTimeMillis()
            )
        )
        helmetDoc().set(data, com.google.firebase.firestore.SetOptions.merge())
    }

    override suspend fun setSearchedLocation(lat: Double, lng: Double) {
        val data = mapOf(
            "SearchedLocation" to mapOf(
                "latitude" to lat,
                "longitude" to lng
            )
        )
        helmetDoc().set(data, com.google.firebase.firestore.SetOptions.merge())
    }
}


