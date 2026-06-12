package com.alejandromartin.dbmap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.alejandromartin.dbmap.databinding.ActivityMainBinding

/**
 * Actividad principal de dBMap.
 * Actúa como contenedor de la navegación inferior entre las cuatro pantallas principales:
 * Medición, Mapa, Histórico y Ajustes.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /**
     * Inicializa el layout, obtiene el NavController del NavHostFragment
     * y lo conecta con la barra de navegación inferior.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

        val navController = navHostFragment.navController

        // Vincula la navegación inferior con el NavController para gestionar los fragments
        binding.bottomNavigation.setupWithNavController(navController)
    }
}