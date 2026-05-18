package fr.miage.geoevent.ui.events

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.miage.geoevent.ui.components.AppButton
import fr.miage.geoevent.ui.components.AppImagePicker
import fr.miage.geoevent.ui.components.AppTextField
import fr.miage.geoevent.ui.components.AppTopBar
import fr.miage.geoevent.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventPage(
    viewModel: MainViewModel, // On passe le ViewModel pour centraliser la logique métier
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    // Observation de l'état de chargement global du ViewModel
    val isLoading by viewModel.isLoading.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Validation simple : titre non vide et pas de chargement en cours
    val isFormValid = title.isNotBlank() && !isLoading

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
                required = true,
                enabled = !isLoading // Désactive le champ pendant l'envoi
            )

            AppTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                placeholder = "Décrivez l'événement",
                enabled = !isLoading
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
                // Changement de texte dynamique selon l'état (comme sur le Login)
                text = if (isLoading) "Création..." else "Créer l'événement",
                onClick = {
                    // Lecture des bytes de l'image si sélectionnée
                    val imageBytes = imageUri?.let { uri ->
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }

                    // Appel de la méthode du ViewModel avec gestion des retours
                    viewModel.createEvent(
                        title = title,
                        description = description,
                        imageBytes = imageBytes,
                        onSuccess = {
                            Toast.makeText(context, "Événement créé avec succès !", Toast.LENGTH_SHORT).show()
                            onBackClick() // On ferme la page en cas de succès
                        },
                        onError = { error ->
                            // Affichage de l'erreur brute ou traduite
                            Toast.makeText(context, "Erreur : $error", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                enabled = isFormValid, // Le bouton n'est cliquable que si le formulaire est valide
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateEventPagePreview() {
    MaterialTheme {
        // Pour la preview, on ne peut pas facilement instancier le vrai ViewModel
        // On pourrait utiliser une interface pour mocker, mais pour rester simple :
        Text("Preview de CreateEventPage (nécessite un ViewModel)")
    }
}
