package fr.miage.geoevent.ui.map.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import fr.miage.geoevent.domain.models.GeoEvent

@Composable
fun MapPage(
    events: List<GeoEvent>,
    onCreateEventClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateEventClick,
                containerColor = Color(0xFF4FA088),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Créer un événement")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Ici viendra la carte OpenStreetMap via AndroidView
            Text(text = "La carte s'affichera ici (${events.size} événements)")
        }
    }
}
