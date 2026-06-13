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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.miage.geotrouvetou.App
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.domain.models.Evenement
import fr.miage.geotrouvetou.ui.utils.appViewModelFactory
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
    onEventDeleted: () -> Unit = onBackClick,
) {
    val context = LocalContext.current
    val viewModel: EventDetailViewModel = viewModel(factory = appViewModelFactory(context))

    var isEditing by remember { mutableStateOf(false) }
    var updateToastKey by remember { mutableIntStateOf(0) }
    var joinToastKey by remember { mutableIntStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        eventId?.let { viewModel.loadEvent(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.eventDeleted.collect { onEventDeleted() }
    }

    LaunchedEffect(Unit) {
        viewModel.joined.collect { joinToastKey++ }
    }

    if (showDeleteDialog) {
        DeleteEventDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteEvent()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = colorResource(R.color.primary_500))
        }
    } else {
        viewModel.event?.let { event ->
            if (isEditing) {
                EventFormScreen(
                    event = event,
                    onBack = { isEditing = false },
                    onSaved = {
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
                    onLeaveClick = { viewModel.leaveEvent() },
                    onEditClick = { isEditing = true },
                    onDeleteClick = { showDeleteDialog = true },
                )

                if (joinToastKey > 0) {
                    Toast(
                        title = stringResource(R.string.event_join_toast_title),
                        description = stringResource(R.string.event_join_toast_desc),
                        key = joinToastKey
                    )
                }

                if (updateToastKey > 0) {
                    Toast(
                        title = stringResource(R.string.event_update_toast_title),
                        description = stringResource(R.string.event_update_toast_desc),
                        key = updateToastKey
                    )
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.event_detail_not_found))
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
    onLeaveClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageUrl = event.imageUrl
        ?: "https://picsum.photos/seed/${event.title.hashCode()}/800/400"
    val date = event.formattedDateLong()
    val time = event.formattedTime()
    val locationParts = event.location?.split(", ") ?: emptyList()
    val locationLabel = locationParts.firstOrNull()
        ?: stringResource(R.string.event_detail_location_fallback)
    val locationDetail: String? = when {
        locationParts.size > 1 -> locationParts.drop(1).joinToString(", ")
        event.location == null -> stringResource(R.string.event_detail_coordinates, event.latitude.toString(), event.longitude.toString())
        else -> null
    }

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
                text = stringResource(R.string.action_back),
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

            EventDetailBody(
                imageUrl = imageUrl,
                date = date,
                time = time,
                locationName = locationLabel,
                locationDetail = locationDetail,
                description = event.description,
                modifier = Modifier.padding(bottom = 8.dp),
                actions = {
                    if (isOwner) {
                        OwnerEventActions(onEdit = onEditClick, onDelete = onDeleteClick)
                    } else {
                        Button(
                            text = if (isJoined) stringResource(R.string.event_detail_leave)
                                else stringResource(R.string.event_detail_join),
                            onClick = { if (isJoined) onLeaveClick() else onJoinClick() },
                            variant = if (isJoined) ButtonVariant.GhostDanger else ButtonVariant.Fill,
                            leftIcon = if (isJoined) Icons.Default.Close else Icons.Default.Add,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
            )
        }
    }
}
