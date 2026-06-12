package com.alejandromartin.dbmap

import android.content.Context

/**
 * Gestiona las preferencias locales de la aplicación mediante SharedPreferences.
 * Las preferencias se almacenan únicamente en el dispositivo y no se envían a Firebase.
 *
 * @param context Contexto de la aplicación.
 */
class ConfigManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Guarda la preferencia de centrado automático del mapa.
     *
     * @param valor true para activar el centrado automático, false para desactivarlo.
     */
    fun guardarCentrarMapaAutomaticamente(valor: Boolean) {
        preferences.edit()
            .putBoolean(KEY_CENTRAR_MAPA, valor)
            .apply()
    }

    /**
     * Devuelve si el centrado automático del mapa está activado.
     * Por defecto es true.
     */
    fun centrarMapaAutomaticamente(): Boolean {
        return preferences.getBoolean(KEY_CENTRAR_MAPA, true)
    }

    /**
     * Guarda la preferencia de mostrar el aviso de privacidad en la pantalla de medición.
     *
     * @param valor true para mostrar el aviso, false para ocultarlo.
     */
    fun guardarMostrarAvisoPrivacidad(valor: Boolean) {
        preferences.edit()
            .putBoolean(KEY_AVISO_PRIVACIDAD, valor)
            .apply()
    }

    /**
     * Devuelve si el aviso de privacidad debe mostrarse en la pantalla de medición.
     * Por defecto es true.
     */
    fun mostrarAvisoPrivacidad(): Boolean {
        return preferences.getBoolean(KEY_AVISO_PRIVACIDAD, true)
    }

    companion object {
        private const val PREFS_NAME = "dbmap_config"       // Nombre del archivo de preferencias
        private const val KEY_CENTRAR_MAPA = "centrar_mapa_automaticamente"  // Clave para el centrado del mapa
        private const val KEY_AVISO_PRIVACIDAD = "mostrar_aviso_privacidad"  // Clave para el aviso de privacidad
    }
}