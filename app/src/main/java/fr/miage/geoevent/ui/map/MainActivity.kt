package fr.miage.geoevent.ui.map

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import fr.miage.geoevent.databinding.ActivityMainBinding
import fr.miage.geoevent.ui.auth.LoginActivity
import fr.miage.geoevent.ui.events.CreateEventPage
import fr.miage.geoevent.ui.map.pages.MapPage
import fr.miage.geoevent.ui.profile.ProfileActivity
import fr.miage.geoevent.ui.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()

        binding.btnProfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnConnexion.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.composeView.setContent {
            val events by viewModel.events.collectAsState()
            var showCreateEvent by remember { mutableStateOf(false) }

            if (showCreateEvent) {
                BackHandler {
                    showCreateEvent = false
                    binding.btnProfil.visibility = View.VISIBLE
                    binding.btnConnexion.visibility = View.VISIBLE
                }

                CreateEventPage(
                    viewModel = viewModel,
                    onBackClick = {
                        showCreateEvent = false
                        binding.btnProfil.visibility = View.VISIBLE
                        binding.btnConnexion.visibility = View.VISIBLE
                    }
                )
            } else {
                MapPage(
                    events = events,
                    onCreateEventClick = {
                        showCreateEvent = true
                        binding.btnProfil.visibility = View.GONE
                        binding.btnConnexion.visibility = View.GONE
                    }
                )
            }
        }

        setupMap()
        checkLocationPermissions()
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
}
