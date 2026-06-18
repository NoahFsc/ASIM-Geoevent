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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
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
import fr.miage.geotrouvetou.ui.events.EventFormScreen
import org.osmdroid.views.MapView

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun MapScreen(
    onLoginClick: () -> Unit = {},
    viewModel: MapViewModel = viewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val mapService = remember(context) { (context.applicationContext as App).createMapService(context) }
    val mapView = remember(context) { MapView(context) }

    var locationPermissionGranted by rememberSaveable { mutableStateOf(hasLocationPermission(context)) }
    var showEventList by remember { mutableStateOf(false) }
    var clusterEvents by remember { mutableStateOf<List<Evenement>?>(null) }
    var selectedEvent by remember { mutableStateOf<Evenement?>(null) }
    var editingEvent by remember { mutableStateOf<Evenement?>(null) }

    var joinToastKey by remember { mutableIntStateOf(0) }
    var updateToastKey by remember { mutableIntStateOf(0) }
    var deleteToastKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.toasts.collect { toast ->
            when (toast) {
                MapToast.Joined -> joinToastKey++
                MapToast.Updated -> updateToastKey++
                MapToast.Deleted -> deleteToastKey++
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationPermissionGranted = granted
        viewModel.onLocationPermissionChanged(granted)
        if (!granted) {
            Toast.makeText(context, context.getString(R.string.map_location_permission_denied), Toast.LENGTH_SHORT).show()
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

        mapService.onResume()
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

    LaunchedEffect(mapView, locationPermissionGranted) {
        if (!locationPermissionGranted) return@LaunchedEffect
        val knownLocation = viewModel.uiState.value.currentLocation

        mapService.enableMyLocation(
            onFirstFix = if (knownLocation != null) null else { point ->
                val zoom = mapService.getZoomForWidth(20.0, point.latitude)
                mapService.centerOn(point.latitude, point.longitude, zoom)
                mapService.setMinimumZoomForWidth(20.0)
                viewModel.onFirstLocationFound(point.latitude, point.longitude)
            }
        )
        if (knownLocation != null) {
            val zoom = mapService.getZoomForWidth(20.0, knownLocation.first)
            mapService.centerOn(knownLocation.first, knownLocation.second, zoom)
            mapService.setMinimumZoomForWidth(20.0)
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

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp),
        ) {
            RoundIconButton(
                icon = Icons.Filled.MyLocation,
                contentDescription = stringResource(R.string.map_center_on_me_cd),
                onClick = {
                    val loc = uiState.currentLocation ?: return@RoundIconButton
                    val zoom = mapService.getZoomForWidth(20.0, loc.first)
                    mapService.centerOn(loc.first, loc.second, zoom)
                },
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RoundIconButton(
                icon = Icons.Filled.Add,
                contentDescription = stringResource(R.string.map_zoom_in_cd),
                onClick = { mapView.controller.zoomIn() },
            )
            RoundIconButton(
                icon = Icons.Filled.Remove,
                contentDescription = stringResource(R.string.map_zoom_out_cd),
                onClick = { mapView.controller.zoomOut() },
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
        ) {
            SearchBar(
                value = "",
                onValueChange = {},
                placeholder = stringResource(R.string.map_search_placeholder),
                onClick = { showEventList = true },
            )
        }

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

        if (deleteToastKey > 0) {
            Toast(
                title = stringResource(R.string.event_delete_toast_title),
                description = stringResource(R.string.event_delete_toast_desc),
                key = deleteToastKey
            )
        }
    }

    when {
        editingEvent != null -> EventFormScreen(
            event = editingEvent!!,
            onBack = { editingEvent = null },
            onSaved = {
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
            onEventDeleted = {
                selectedEvent = null
                viewModel.onEventDeleted()
                viewModel.scheduleRefresh()
            },
            onLoginClick = onLoginClick
        )
        clusterEvents != null -> EventListModal(
            events = clusterEvents!!,
            title = stringResource(R.string.event_list_cluster, clusterEvents!!.size),
            onDismissRequest = { clusterEvents = null },
            onEditClick = { event ->
                editingEvent = event
                clusterEvents = null
            }
        )
        showEventList -> EventListModal(
            events = uiState.events,
            title = stringResource(R.string.event_list_proposals, uiState.events.size),
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
