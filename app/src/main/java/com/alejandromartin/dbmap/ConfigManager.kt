package com.alejandromartin.dbmap

import android.content.Context

class ConfigManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun guardarCentrarMapaAutomaticamente(valor: Boolean) {
        preferences.edit()
            .putBoolean(KEY_CENTRAR_MAPA, valor)
            .apply()
    }

    fun centrarMapaAutomaticamente(): Boolean {
        return preferences.getBoolean(KEY_CENTRAR_MAPA, true)
    }

    fun guardarMostrarAvisoPrivacidad(valor: Boolean) {
        preferences.edit()
            .putBoolean(KEY_AVISO_PRIVACIDAD, valor)
            .apply()
    }

    fun mostrarAvisoPrivacidad(): Boolean {
        return preferences.getBoolean(KEY_AVISO_PRIVACIDAD, true)
    }

    companion object {
        private const val PREFS_NAME = "dbmap_config"
        private const val KEY_CENTRAR_MAPA = "centrar_mapa_automaticamente"
        private const val KEY_AVISO_PRIVACIDAD = "mostrar_aviso_privacidad"
    }
}