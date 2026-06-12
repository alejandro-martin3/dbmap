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

/**
 * Gestiona la obtención de la ubicación aproximada del dispositivo.
 * Utiliza FusedLocationProviderClient para solicitar una ubicación puntual
 * con precisión balanceada, sin guardar ni exponer coordenadas exactas.
 *
 * @param context Contexto de la aplicación.
 */
class LocationHelper(private val context: Context) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Solicita la ubicación actual del dispositivo de forma puntual.
     * Si el permiso de ubicación no está concedido, devuelve null sin lanzar error.
     * Si no se obtiene ubicación en 15 segundos, devuelve null por timeout.
     *
     * @param onSuccess Callback con la ubicación obtenida, o null si no está disponible.
     * @param onError Callback ejecutado si se produce un error al solicitar la ubicación.
     */
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        onSuccess: (Location?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Sin permiso de ubicación no se puede continuar; se devuelve null sin error
        if (!hasPermission) {
            onSuccess(null)
            return
        }

        val handler = Handler(Looper.getMainLooper())
        var finished = false

        lateinit var locationCallback: LocationCallback

        // Función interna para garantizar que el callback solo se ejecuta una vez
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

        // Solicita una única actualización de ubicación con precisión balanceada
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            1000L
        )
            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            .setWaitForAccurateLocation(false)
            .setMaxUpdates(1)
            .setDurationMillis(15000L)
            .build()

        // Timeout de 15 segundos: si no llega ubicación, se devuelve null
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