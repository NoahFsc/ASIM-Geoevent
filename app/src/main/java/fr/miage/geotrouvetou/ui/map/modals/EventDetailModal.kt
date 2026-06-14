package fr.miage.geotrouvetou.ui.map.modals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import fr.miage.geotrouvetou.ui.components.organisms.EventDetailBody
import fr.miage.geotrouvetou.ui.components.organisms.Modal
import fr.miage.geotrouvetou.ui.events.DeleteEventDialog
import fr.miage.geotrouvetou.ui.events.EventDetailViewModel
import fr.miage.geotrouvetou.ui.events.OwnerEventActions
import fr.miage.geotrouvetou.ui.utils.formattedDateLong
import fr.miage.geotrouvetou.ui.utils.formattedTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailModal(
    onDismissRequest: () -> Unit,
    event: Evenement,
    onBackClick: (() -> Unit)? = null,
    onEventJoined: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onEventDeleted: (() -> Unit)? = null,
    onLoginClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    Modal(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        EventDetailModalContentWithViewModel(
            event = event,
            onBackClick = onBackClick,
            onEventJoined = onEventJoined,
            onEditClick = onEditClick,
            onEventDeleted = onEventDeleted,
            onLoginClick = onLoginClick
        )
    }
}

@Composable
fun EventDetailModalContentWithViewModel(
    event: Evenement,
    onBackClick: (() -> Unit)? = null,
    onEventJoined: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onEventDeleted: (() -> Unit)? = null,
    onLoginClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: EventDetailViewModel = viewModel(
        key = event.id,
        factory = appViewModelFactory(context),
    )
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(event) {
        viewModel.event = event
        event.id?.let { viewModel.loadEvent(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.joined.collect { onEventJoined?.invoke() }
    }

    LaunchedEffect(Unit) {
        viewModel.eventDeleted.collect { onEventDeleted?.invoke() }
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

    EventDetailModalContent(
        event = event,
        onBackClick = onBackClick,
        isJoined = viewModel.isJoined,
        isOwner = viewModel.isOwner,
        onJoinClick = {
            if ((context.applicationContext as App).authService.isLoggedIn()) {
                viewModel.joinEvent()
            } else {
                onLoginClick?.invoke()
            }
        },
        onLeaveClick = { viewModel.leaveEvent() },
        onEditClick = onEditClick,
        onDeleteClick = { showDeleteDialog = true },
        modifier = modifier
    )
}

@Composable
fun EventDetailModalContent(
    event: Evenement,
    onBackClick: (() -> Unit)? = null,
    isJoined: Boolean = false,
    isOwner: Boolean = false,
    onJoinClick: () -> Unit = {},
    onLeaveClick: () -> Unit = {},
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val date = event.formattedDateLong()
    val time = event.formattedTime()
    val imageUrl = event.imageUrl
        ?: "https://picsum.photos/seed/${event.title.hashCode()}/800/400"
    val locationParts = event.location?.split(", ") ?: emptyList()
    val locationLabel = locationParts.firstOrNull()
        ?: stringResource(R.string.event_detail_location_fallback)
    val locationDetail: String? = when {
        locationParts.size > 1 -> locationParts.drop(1).joinToString(", ")
        event.location == null -> stringResource(R.string.event_detail_coordinates, "%.4f".format(event.latitude), "%.4f".format(event.longitude))
        else -> null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                Row(
                    modifier = Modifier.clickable(onClick = onBackClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = colorResource(R.color.text_darker),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.action_back),
                        fontSize = 16.sp,
                        color = colorResource(R.color.text_darker)
                    )
                }
            } else {
                Spacer(modifier = Modifier)
            }
        }

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
            modifier = Modifier.padding(bottom = 16.dp),
            actions = {
                if (isOwner) {
                    OwnerEventActions(onEdit = { onEditClick?.invoke() }, onDelete = onDeleteClick)
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
