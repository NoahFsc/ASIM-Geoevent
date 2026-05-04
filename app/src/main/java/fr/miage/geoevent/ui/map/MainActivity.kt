package fr.miage.geoevent.ui.map

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import fr.miage.geoevent.BuildConfig
import fr.miage.geoevent.data.backend.SupabaseDatabaseService
import fr.miage.geoevent.databinding.ActivityMainBinding
import fr.miage.geoevent.domain.interfaces.IDatabaseService
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import kotlinx.serialization.Serializable
import kotlinx.coroutines.launch

val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_KEY
) {
    install(Postgrest)
    install(Realtime)
}

@Serializable
data class TestItem(
    val id: Long,
    val created_at: String,
    val name: String
)

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Noah : Utilisation de l'interface pour la modularité [cite: 27, 29]
    private lateinit var databaseService: IDatabaseService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        // Il suffit de changer SupabaseDatabaseService par une autre implémentation
        databaseService = SupabaseDatabaseService(supabase)

        // Initialisation de la carte (OpenStreetMap) (cf. Lucas)
        setupMap()

        // Gestion des permissions runtime (cf. Lucas)
        checkLocationPermissions()

        // Listener en temps réel (cf. Noah)
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
        // C'est ici que Lucas configurera OpenStreetMap
    }

    private fun checkLocationPermissions() {
        // Appel aux méthodes de Lucas pour la géolocalisation
    }

    /**
     * Noah : Cette méthode écoute les changements en base de données
     * et demande à Lucas de mettre à jour les marqueurs sur la carte.
     */
    private fun observeEvents() {
        lifecycleScope.launch {
            try {
                databaseService.listenToEventsRealtime().collect { events ->
                    // Ici, on envoie la liste d'événements à la carte de Lucas
                    // Exemple : binding.mapView.updateMarkers(events)

                    if (events.isNotEmpty()) {
                        Toast.makeText(this@MainActivity, "${events.size} événements chargés", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // Gestion des erreurs pour la robustesse [cite: 60, 62]
                Toast.makeText(this@MainActivity, "Erreur de connexion temps réel", Toast.LENGTH_LONG).show()
            }
        }
    }
}