package com.alejandromartin.dbmap

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Objeto de utilidades comunes de la aplicación.
 * Proporciona funciones auxiliares reutilizables en cualquier parte de dBMap.
 */
object AppUtils {

    /**
     * Comprueba si el dispositivo tiene conexión a internet activa.
     * Se utiliza antes de iniciar una medición o cargar datos de Firebase.
     *
     * @param context Contexto de la aplicación.
     * @return true si hay conexión a internet disponible, false en caso contrario.
     */
    fun hayConexion(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}