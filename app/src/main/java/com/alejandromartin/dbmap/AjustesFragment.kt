package com.alejandromartin.dbmap

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * Fragment que representa la pantalla de ajustes.
 * Permite al usuario activar o desactivar preferencias básicas de visualización y privacidad.
 * Las preferencias se guardan localmente en el dispositivo mediante ConfigManager.
 */
class AjustesFragment : Fragment(R.layout.fragment_ajustes) {

    private lateinit var configManager: ConfigManager

    /**
     * Inicializa los switches de ajustes con los valores guardados
     * y registra los listeners para persistir los cambios.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configManager = ConfigManager(requireContext().applicationContext)

        val switchCentrarMapa = view.findViewById<SwitchMaterial>(R.id.switchCentrarMapa)
        val switchAvisoPrivacidad = view.findViewById<SwitchMaterial>(R.id.switchAvisoPrivacidad)

        // Carga los valores guardados al abrir la pantalla
        switchCentrarMapa.isChecked = configManager.centrarMapaAutomaticamente()
        switchAvisoPrivacidad.isChecked = configManager.mostrarAvisoPrivacidad()

        // Guarda la preferencia de centrado automático del mapa al cambiar el switch
        switchCentrarMapa.setOnCheckedChangeListener { _, isChecked ->
            configManager.guardarCentrarMapaAutomaticamente(isChecked)
        }

        // Guarda la preferencia del aviso de privacidad al cambiar el switch
        switchAvisoPrivacidad.setOnCheckedChangeListener { _, isChecked ->
            configManager.guardarMostrarAvisoPrivacidad(isChecked)
        }
    }
}