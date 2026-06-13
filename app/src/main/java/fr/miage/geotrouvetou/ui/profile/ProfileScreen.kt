package fr.miage.geotrouvetou.ui.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.domain.models.Evenement
import fr.miage.geotrouvetou.ui.components.atoms.SegmentedControl
import fr.miage.geotrouvetou.ui.components.atoms.StatCard
import fr.miage.geotrouvetou.ui.components.molecules.ProfileEventItem
import fr.miage.geotrouvetou.ui.map.modals.EventListModal

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onEventClick: (String) -> Unit = {},
    viewModel: ProfileViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.navigateToLogin) {
        if (uiState.navigateToLogin) {
            onLogout()
            viewModel.onNavigationHandled()
        }
    }

    ProfileContent(
        uiState = uiState,
        onSettingsClick = onSettingsClick,
        onTabSelected = { viewModel.onTabSelected(it) },
        onEventClick = onEventClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onSettingsClick: () -> Unit,
    onTabSelected: (ProfileTab) -> Unit,
    onEventClick: (String) -> Unit
) {
    var showEventList by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.profile_title),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_darker),
                )
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.profile_settings_cd),
                    tint = colorResource(R.color.text_darker),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onSettingsClick() },
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(colorResource(R.color.text_disabled)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (uiState.avatarUrl != null) {
                        AsyncImage(
                            model = uiState.avatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = colorResource(R.color.text_lighter),
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }
                Text(
                    text = uiState.fullName.ifEmpty { "—" },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.text_darker),
                    lineHeight = 32.sp,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    value = "${uiState.events.size}",
                    label = stringResource(R.string.profile_stat_events),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    value = "${uiState.joinedEvents.size}",
                    label = stringResource(R.string.profile_stat_participations),
                    modifier = Modifier.weight(1f),
                )
            }

            SegmentedControl(
                tabs = listOf(stringResource(R.string.profile_tab_my_events), stringResource(R.string.profile_tab_my_participations)),
                selectedIndex = uiState.selectedTab.ordinal,
                onTabSelected = { onTabSelected(ProfileTab.entries[it]) },
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(R.color.primary_500))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (uiState.selectedTab) {
                        ProfileTab.MesEvenements -> {
                            if (uiState.events.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.profile_empty_events),
                                    color = colorResource(R.color.text_lighter),
                                    fontSize = 14.sp,
                                )
                            } else {
                                uiState.events.forEach { event ->
                                    ProfileEventItem(
                                        event = event,
                                        onClick = { event.id?.let { onEventClick(it) } }
                                    )
                                }
                            }
                        }
                        ProfileTab.MesParticipations -> {
                            if (uiState.joinedEvents.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.profile_empty_participations),
                                    color = colorResource(R.color.text_lighter),
                                    fontSize = 14.sp,
                                )
                            } else {
                                uiState.joinedEvents.forEach { event ->
                                    ProfileEventItem(
                                        event = event,
                                        onClick = { event.id?.let { onEventClick(it) } }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = colorResource(R.color.danger_500),
                    fontSize = 14.sp,
                )
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileContent(
        uiState = ProfileUiState(
            fullName = "Maxime MIAGE",
            avatarUrl = null,
            isLoading = false,
            events = listOf(
                Evenement(
                    id = "1",
                    title = "Randonnée Forêt",
                    description = "Une petite marche",
                    latitude = 0.0,
                    longitude = 0.0
                )
            )
        ),
        onSettingsClick = {},
        onTabSelected = {},
        onEventClick = {}
    )
}
