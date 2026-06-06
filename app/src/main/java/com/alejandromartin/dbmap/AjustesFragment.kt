package com.alejandromartin.dbmap

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial

class AjustesFragment : Fragment(R.layout.fragment_ajustes) {

    private lateinit var configManager: ConfigManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configManager = ConfigManager(requireContext().applicationContext)

        val switchCentrarMapa = view.findViewById<SwitchMaterial>(R.id.switchCentrarMapa)
        val switchAvisoPrivacidad = view.findViewById<SwitchMaterial>(R.id.switchAvisoPrivacidad)

        switchCentrarMapa.isChecked = configManager.centrarMapaAutomaticamente()
        switchAvisoPrivacidad.isChecked = configManager.mostrarAvisoPrivacidad()

        switchCentrarMapa.setOnCheckedChangeListener { _, isChecked ->
            configManager.guardarCentrarMapaAutomaticamente(isChecked)
        }

        switchAvisoPrivacidad.setOnCheckedChangeListener { _, isChecked ->
            configManager.guardarMostrarAvisoPrivacidad(isChecked)
        }
    }
}