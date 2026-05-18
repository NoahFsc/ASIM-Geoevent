package fr.miage.geoevent.ui.events.pages

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.miage.geoevent.ui.events.components.AppButton
import fr.miage.geoevent.ui.events.components.AppImagePicker
import fr.miage.geoevent.ui.events.components.AppTextField
import fr.miage.geoevent.ui.events.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventPage(onBackClick: () -> Unit) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val isFormValid = title.isNotBlank()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Créer un événement",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppTextField(
                value = title,
                onValueChange = { title = it },
                label = "Titre",
                placeholder = "Ex: Concert au parc",
                required = true
            )

            AppTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                placeholder = "Décrivez l'événement"
            )

            // Affichage des coordonnées (non éditables)
            Text(
                text = "Position : 20 avenue de la sagesse, Amiens",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Utilisation du nouveau composant ImagePicker
            AppImagePicker(
                selectedUri = imageUri,
                onUriSelected = { imageUri = it }
            )

            AppButton(
                text = "Créer l'événement",
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(
                            context,
                            "Veuillez remplir le titre",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@AppButton
                    }

                    // TODO: Brancher ici l'appel API / Supabase
                    Toast.makeText(context, "Événement prêt à être créé", Toast.LENGTH_SHORT).show()
                    onBackClick() // Retourner à la carte après création
                },
                enabled = isFormValid,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateEventPagePreview() {
    MaterialTheme {
        CreateEventPage(onBackClick = {})
    }
}
