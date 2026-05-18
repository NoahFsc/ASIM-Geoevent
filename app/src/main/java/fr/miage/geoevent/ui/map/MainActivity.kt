package fr.miage.geoevent.ui.map

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import fr.miage.geoevent.GeoEventApplication
import fr.miage.geoevent.data.backend.SupabaseDatabaseService
import fr.miage.geoevent.databinding.ActivityMainBinding
import fr.miage.geoevent.domain.interfaces.IDatabaseService
import fr.miage.geoevent.ui.auth.LoginActivity
import fr.miage.geoevent.ui.profile.ProfileActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var databaseService: IDatabaseService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()

        val supabase = (applicationContext as GeoEventApplication).supabase
        databaseService = SupabaseDatabaseService(supabase)

        binding.btnProfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnConnexion.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        setupMap()
        checkLocationPermissions()
        observeEvents()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupMap() {
        // Lucas configurera OpenStreetMap ici
    }

    private fun checkLocationPermissions() {
        // Appel aux méthodes de Lucas pour la géolocalisation
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            try {
                databaseService.listenToEventsRealtime().collect { events ->
                    if (events.isNotEmpty()) {
                        Toast.makeText(this@MainActivity, "${events.size} événements chargés", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (_: Exception) {
                // Connexion interrompue silencieusement (déconnexion ou perte réseau)
            }
        }
    }

}
