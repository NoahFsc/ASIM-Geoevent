package fr.miage.geotrouvetou.ui.events

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.miage.geotrouvetou.domain.interfaces.IAuthService
import fr.miage.geotrouvetou.domain.interfaces.IDatabaseService
import fr.miage.geotrouvetou.domain.models.Evenement
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class EventDetailViewModel(
    private val databaseService: IDatabaseService,
    private val authService: IAuthService,
) : ViewModel() {

    var event by mutableStateOf<Evenement?>(null)
    
    var isLoading by mutableStateOf(false)
        private set

    var isJoined by mutableStateOf(false)
        private set

    var isOwner by mutableStateOf(false)
        private set

    var participantsCount by mutableIntStateOf(0)
        private set

    private val _joined = MutableSharedFlow<Boolean>()
    val joined = _joined.asSharedFlow()

    private val _eventDeleted = MutableSharedFlow<Boolean>()
    val eventDeleted = _eventDeleted.asSharedFlow()

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                event = databaseService.getEvent(eventId)

                val userId = authService.currentUserId()
                if (userId != null) {
                    isJoined = databaseService.isUserParticipating(eventId, userId)
                    isOwner = event?.userId == userId
                }
                participantsCount = databaseService.getParticipantsCount(eventId)
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun joinEvent() {
        val currentEvent = event ?: return
        val eventId = currentEvent.id ?: return

        viewModelScope.launch {
            try {
                val userId = authService.currentUserId()
                if (userId != null) {
                    databaseService.joinEvent(eventId, userId)
                    isJoined = true
                    participantsCount++
                    _joined.emit(true)
                }
            } catch (_: Exception) {
                // Échec silencieux (ex : déjà inscrit)
            }
        }
    }

    fun leaveEvent() {
        val currentEvent = event ?: return
        val eventId = currentEvent.id ?: return

        viewModelScope.launch {
            try {
                val userId = authService.currentUserId()
                if (userId != null) {
                    databaseService.leaveEvent(eventId, userId)
                    isJoined = false
                    if (participantsCount > 0) participantsCount--
                }
            } catch (_: Exception) {
                // Échec silencieux
            }
        }
    }

    fun deleteEvent() {
        val eventId = event?.id ?: return
        viewModelScope.launch {
            try {
                databaseService.deleteEvent(eventId)
                _eventDeleted.emit(true)
            } catch (_: Exception) {
                // Échec silencieux
            }
        }
    }
}
