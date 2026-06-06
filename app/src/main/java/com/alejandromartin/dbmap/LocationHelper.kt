package com.alejandromartin.dbmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationHelper(private val context: Context) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        onSuccess: (Location?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            onSuccess(null)
            return
        }

        val handler = Handler(Looper.getMainLooper())
        var finished = false

        lateinit var locationCallback: LocationCallback

        fun finish(location: Location?) {
            if (finished) return
            finished = true

            handler.removeCallbacksAndMessages(null)
            fusedLocationClient.removeLocationUpdates(locationCallback)

            onSuccess(location)
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                finish(result.lastLocation)
            }
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            1000L
        )
            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            .setWaitForAccurateLocation(false)
            .setMaxUpdates(1)
            .setDurationMillis(15000L)
            .build()

        handler.postDelayed({
            finish(null)
        }, 15000L)

        fusedLocationClient
            .requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            .addOnFailureListener { exception ->
                if (!finished) {
                    finished = true
                    handler.removeCallbacksAndMessages(null)
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                    onError(exception)
                }
            }
    }
}