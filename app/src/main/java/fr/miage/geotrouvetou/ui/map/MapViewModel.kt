package fr.miage.geotrouvetou.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.miage.geotrouvetou.App
import fr.miage.geotrouvetou.domain.interfaces.IDatabaseService
import fr.miage.geotrouvetou.domain.interfaces.MapBounds
import fr.miage.geotrouvetou.domain.models.Evenement
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview
import kotlin.math.abs
import kotlin.math.cos

data class MapUiState(
    val events: List<Evenement> = emptyList(),
    val currentLocation: Pair<Double, Double>? = null,
    val visibleBounds: MapBounds? = null,
    val hasLocationPermission: Boolean = false,
    val hasLocationFix: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val joinToastKey: Int = 0,
    val updateToastKey: Int = 0,
    val deleteToastKey: Int = 0,
)

@OptIn(FlowPreview::class)
class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val databaseService = runCatching {
        (application as App).databaseService
    }.getOrNull()

    private val startupErrorMessage = "Initialisation Supabase impossible"

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var realtimeJob: Job? = null

    init {
        if (databaseService == null) {
            _uiState.update { it.copy(errorMessage = startupErrorMessage, isLoading = false) }
        } else {
            viewModelScope.launch {
                refreshRequests
                    .debounce(250)
                    .collectLatest {
                        refreshCurrentViewport()
                    }
            }
        }
    }

    fun startRealtime() {
        if (databaseService == null) {
            _uiState.update { it.copy(errorMessage = startupErrorMessage) }
            return
        }

        if (realtimeJob?.isActive == true) return

        realtimeJob = viewModelScope.launch {
            try {
                databaseService.listenToEventsRealtime().collect {
                    scheduleRefresh()
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(errorMessage = "Impossible d'initialiser le temps réel : ${t.message ?: "erreur inconnue"}") }
            }
        }
    }

    fun onLocationPermissionChanged(granted: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = granted) }
    }

    fun onFirstLocationFound(latitude: Double, longitude: Double) {
        _uiState.update {
            it.copy(
                currentLocation = latitude to longitude,
                hasLocationFix = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            val service = databaseService ?: return@launch
            val bounds = _uiState.value.visibleBounds
            if (bounds != null) {
                loadEventsByBounds(service, bounds)
            } else {
                loadEventsAroundLocation(service, latitude, longitude)
            }
        }
    }

    fun onViewBoundsChanged(bounds: MapBounds) {
        _uiState.update { it.copy(visibleBounds = bounds) }
        scheduleRefresh()
    }

    fun onEventJoined() {
        _uiState.update { it.copy(joinToastKey = it.joinToastKey + 1) }
    }

    fun onEventUpdated() {
        _uiState.update { it.copy(updateToastKey = it.updateToastKey + 1) }
    }

    fun onEventDeleted() {
        _uiState.update { it.copy(deleteToastKey = it.deleteToastKey + 1) }
    }

    fun scheduleRefresh() {
        refreshRequests.tryEmit(Unit)
    }

    private suspend fun refreshCurrentViewport() {
        val service = databaseService ?: return
        val state = _uiState.value
        if (!state.hasLocationFix) return

        val bounds = state.visibleBounds
        val location = state.currentLocation

        if (bounds != null) {
            loadEventsByBounds(service, bounds)
        } else if (location != null) {
            loadEventsAroundLocation(service, location.first, location.second)
        }
    }

    private suspend fun loadEventsAroundLocation(service: IDatabaseService, latitude: Double, longitude: Double) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            val bounds = buildBoundsAround(latitude, longitude)
            val events = service.getEventsByVisibleBounds(
                minLat = bounds.first,
                maxLat = bounds.second,
                minLon = bounds.third,
                maxLon = bounds.fourth,
            )
            _uiState.update { it.copy(events = events, isLoading = false) }
        } catch (t: Throwable) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = t.message ?: "Impossible de charger les événements autour de la position",
                )
            }
        }
    }

    private suspend fun loadEventsByBounds(service: IDatabaseService, bounds: MapBounds) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            val events = service.getEventsByVisibleBounds(
                minLat = bounds.minLat,
                maxLat = bounds.maxLat,
                minLon = bounds.minLon,
                maxLon = bounds.maxLon,
            )
            _uiState.update { it.copy(events = events, isLoading = false) }
        } catch (t: Throwable) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = t.message ?: "Impossible de charger les événements visibles",
                )
            }
        }
    }

    private fun buildBoundsAround(latitude: Double, longitude: Double): Quadruple<Double, Double, Double, Double> {
        val rangeKm = 20.0
        val degreesPerLatitudeKm = 111.32
        val deltaLat = rangeKm / degreesPerLatitudeKm

        val cosLat = abs(cos(Math.toRadians(latitude)))
        val deltaLon = if (cosLat < 1e-6) {
            180.0
        } else {
            rangeKm / (degreesPerLatitudeKm * cosLat)
        }

        return Quadruple(
            latitude - deltaLat,
            latitude + deltaLat,
            longitude - deltaLon,
            longitude + deltaLon
        )
    }
}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)