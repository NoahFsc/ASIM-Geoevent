package fr.miage.geotrouvetou.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.miage.geotrouvetou.App
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.domain.interfaces.MapBounds
import fr.miage.geotrouvetou.domain.models.Evenement
import fr.miage.geotrouvetou.ui.components.atoms.RoundIconButton
import fr.miage.geotrouvetou.ui.components.atoms.Toast
import fr.miage.geotrouvetou.ui.components.organisms.SearchBar
import fr.miage.geotrouvetou.ui.map.modals.EventListModal
import fr.miage.geotrouvetou.ui.map.modals.EventDetailModal
import fr.miage.geotrouvetou.ui.events.EventUpdateScreen
import org.osmdroid.views.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
fun MapScreen(
    onLoginClick: () -> Unit = {},
    viewModel: MapViewModel = viewModel(),
) {
    @Suppress("DEPRECATION")
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val mapService = remember(context) { (context.applicationContext as App).createMapService(context) }
    val mapView = remember(context) { MapView(context) }

    var mapBound by rememberSaveable { mutableStateOf(false) }
    var locationPermissionGranted by rememberSaveable { mutableStateOf(hasLocationPermission(context)) }
    var locationFlowStarted by rememberSaveable { mutableStateOf(false) }
    var showEventList by remember { mutableStateOf(false) }
    var clusterEvents by remember { mutableStateOf<List<Evenement>?>(null) }
    var selectedEvent by remember { mutableStateOf<Evenement?>(null) }
    var editingEvent by remember { mutableStateOf<Evenement?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationPermissionGranted = granted
        viewModel.onLocationPermissionChanged(granted)
        if (!granted) {
            Toast.makeText(context, "Permission localisation refusée", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(mapView) {
        mapService.bind(mapView)
        mapService.setOnViewBoundsChangedListener { bounds: MapBounds ->
            viewModel.onViewBoundsChanged(bounds)
        }
        mapService.setOnEventClickListener { event ->
            clusterEvents = null
            showEventList = false
            selectedEvent = event
        }
        mapService.setOnClusterClickListener { events ->
            selectedEvent = null
            showEventList = false
            clusterEvents = events
        }
        mapBound = true
        onDispose {
            mapService.setOnEventClickListener(null)
            mapService.setOnClusterClickListener(null)
            mapService.onPause()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapService.onResume()
                Lifecycle.Event.ON_PAUSE -> mapService.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapService.onPause()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startRealtime()
        if (!locationPermissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        } else {
            viewModel.onLocationPermissionChanged(true)
        }
    }

    LaunchedEffect(mapBound, locationPermissionGranted) {
        if (mapBound && locationPermissionGranted && !locationFlowStarted) {
            locationFlowStarted = true
            mapService.enableMyLocation { point ->
                val defaultZoom = mapService.getZoomForWidth(20.0, point.latitude)
                mapService.centerOn(point.latitude, point.longitude, defaultZoom)
                mapService.setMinimumZoomForWidth(20.0)
                viewModel.onFirstLocationFound(point.latitude, point.longitude)
            }
        }
    }

    LaunchedEffect(uiState.events) {
        mapService.displayEvents(uiState.events)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.background))
    ) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp),
                color = colorResource(R.color.primary_400),
                strokeWidth = 3.dp
            )
        }

        // Top-left: center on self
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp),
        ) {
            RoundIconButton(
                icon = Icons.Filled.MyLocation,
                contentDescription = "Centrer sur ma position",
                onClick = {
                    val loc = uiState.currentLocation ?: return@RoundIconButton
                    val zoom = mapService.getZoomForWidth(20.0, loc.first)
                    mapService.centerOn(loc.first, loc.second, zoom)
                },
            )
        }

        // Top-right: zoom in / zoom out
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RoundIconButton(
                icon = Icons.Filled.Add,
                contentDescription = "Zoom avant",
                onClick = { mapView.controller.zoomIn() },
            )
            RoundIconButton(
                icon = Icons.Filled.Remove,
                contentDescription = "Zoom arrière",
                onClick = { mapView.controller.zoomOut() },
            )
        }

        // Bottom-center: search bar opening the event list modal
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
        ) {
            SearchBar(
                value = "",
                onValueChange = {},
                placeholder = "Rechercher par lieu",
                onClick = { showEventList = true },
            )
        }

        if (uiState.joinToastKey > 0) {
            Toast(
                title = "Inscription réussie !",
                description = "Vous participez désormais à cet événement",
                key = uiState.joinToastKey
            )
        }

        if (uiState.updateToastKey > 0) {
            Toast(
                title = "Modification réussie !",
                description = "L'événement a été mis à jour",
                key = uiState.updateToastKey
            )
        }
    }

    when {
        editingEvent != null -> EventUpdateScreen(
            event = editingEvent!!,
            onBackClick = { editingEvent = null },
            onEventUpdated = { 
                viewModel.onEventUpdated()
                viewModel.scheduleRefresh()
                editingEvent = null
            }
        )
        selectedEvent != null -> EventDetailModal(
            event = selectedEvent!!,
            onDismissRequest = { selectedEvent = null },
            onEventJoined = { viewModel.onEventJoined() },
            onEditClick = {
                editingEvent = selectedEvent
                selectedEvent = null
            },
            onLoginClick = onLoginClick
        )
        clusterEvents != null -> EventListModal(
            events = clusterEvents!!,
            title = "Groupement (${clusterEvents!!.size})",
            onDismissRequest = { clusterEvents = null },
            onEditClick = { event ->
                editingEvent = event
                clusterEvents = null
            }
        )
        showEventList -> EventListModal(
            events = uiState.events,
            title = "Propositions (${uiState.events.size})",
            onDismissRequest = { showEventList = false },
            onPlaceSelected = { place ->
                showEventList = false
                val zoom = mapService.getZoomForWidth(20.0, place.latitude)
                mapService.centerOn(place.latitude, place.longitude, zoom)
            },
            onEditClick = { event ->
                editingEvent = event
                showEventList = false
            }
        )
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
    return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
}
