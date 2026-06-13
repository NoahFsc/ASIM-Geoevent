package fr.miage.geotrouvetou.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.miage.geotrouvetou.App
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.domain.models.Evenement
import fr.miage.geotrouvetou.ui.appViewModelFactory
import fr.miage.geotrouvetou.ui.components.atoms.Button
import fr.miage.geotrouvetou.ui.components.atoms.ButtonVariant
import fr.miage.geotrouvetou.ui.components.atoms.Toast
import fr.miage.geotrouvetou.ui.components.organisms.EventDetailBody
import fr.miage.geotrouvetou.ui.utils.formattedDateLong
import fr.miage.geotrouvetou.ui.utils.formattedTime

@Composable
fun EventDetailScreen(
    eventId: String?,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: EventDetailViewModel = viewModel(factory = appViewModelFactory(context))

    var isEditing by remember { mutableStateOf(false) }
    var updateToastKey by remember { mutableStateOf(0) }

    LaunchedEffect(eventId) {
        eventId?.let { viewModel.loadEvent(it) }
    }

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colorResource(R.color.primary_500))
        }
    } else {
        viewModel.event?.let { event ->
            if (isEditing) {
                EventUpdateScreen(
                    event = event,
                    onBackClick = { isEditing = false },
                    onEventUpdated = { 
                        isEditing = false
                        updateToastKey++
                        eventId?.let { viewModel.loadEvent(it) }
                    }
                )
            } else {
                EventDetailContent(
                    event = event,
                    onBackClick = onBackClick,
                    isJoined = viewModel.isJoined,
                    isOwner = viewModel.isOwner,
                    onJoinClick = {
                        if ((context.applicationContext as App).authService.isLoggedIn()) {
                            viewModel.joinEvent()
                        } else {
                            onLoginClick()
                        }
                    },
                    onEditClick = { isEditing = true }
                )

                if (viewModel.joinToastKey > 0) {
                    Toast(
                        title = "Inscription réussie !",
                        description = "Vous participez désormais à cet événement",
                        key = viewModel.joinToastKey
                    )
                }

                if (updateToastKey > 0) {
                    Toast(
                        title = "Modification réussie !",
                        description = "L'événement a été mis à jour",
                        key = updateToastKey
                    )
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Événement introuvable")
            }
        }
    }
}

@Composable
fun EventDetailContent(
    event: Evenement,
    onBackClick: () -> Unit,
    isJoined: Boolean,
    isOwner: Boolean,
    onJoinClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageUrl = event.image_url
        ?: "https://picsum.photos/seed/${event.title.hashCode()}/800/400"
    val date = event.formattedDateLong()
    val time = event.formattedTime()
    val locationLabel = event.location ?: "Coordonnées"
    val locationDetail = if (event.location != null) "" else "Lat: ${event.latitude}, Lon: ${event.longitude}"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .clickable { onBackClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = colorResource(R.color.text_light),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Retour",
                fontSize = 18.sp,
                color = colorResource(R.color.text_light)
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = event.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.primary_600),
                lineHeight = 34.sp
            )

            Button(
                text = if (isOwner) "Modifier" else if (isJoined) "Déjà inscrit" else "Enregistrer",
                onClick = {
                    if (isOwner) onEditClick()
                    else if (!isJoined) onJoinClick()
                },
                enabled = isOwner || !isJoined,
                variant = ButtonVariant.Fill,
                leftIcon = if (isOwner) Icons.Default.Edit else Icons.Default.Add,
                modifier = Modifier.fillMaxWidth()
            )

            EventDetailBody(
                imageUrl = imageUrl,
                date = date,
                time = time,
                locationName = locationLabel,
                locationDetail = locationDetail,
                description = event.description,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventDetailScreenPreview() {
    EventDetailContent(
        event = Evenement(
            title = "Grande Forêt de Chailluz",
            description = "Ce parcours accessible aux chiens vous fait découvrir la grande forêt de Chailluz...",
            latitude = 0.0,
            longitude = 0.0,
            event_date = "2024-04-28T09:00:00"
        ),
        onBackClick = {},
        isJoined = false,
        isOwner = false,
        onJoinClick = {},
        onEditClick = {}
    )
}
