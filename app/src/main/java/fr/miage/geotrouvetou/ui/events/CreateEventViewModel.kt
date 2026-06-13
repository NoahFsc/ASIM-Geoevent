package fr.miage.geotrouvetou.ui.events

import android.net.Uri
import androidx.compose.runtime.getValue
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
import java.util.UUID

class CreateEventViewModel(
    private val databaseService: IDatabaseService,
    private val authService: IAuthService,
) : ViewModel() {

    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var date by mutableStateOf("")
    var time by mutableStateOf("")
    var location by mutableStateOf("")
    var latitude by mutableStateOf<Double?>(null)
    var longitude by mutableStateOf<Double?>(null)
    var isPrivate by mutableStateOf(false)
    var imageUri by mutableStateOf<Uri?>(null)
    var isLoading by mutableStateOf(false)
        private set

    private val _eventCreated = MutableSharedFlow<Boolean>()
    val eventCreated = _eventCreated.asSharedFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    val isFormValid: Boolean
        get() = title.isNotBlank() && description.isNotBlank() && date.isNotBlank() && time.isNotBlank() && latitude != null && longitude != null && imageUri != null && !isLoading

    fun createEvent(imageBytes: ByteArray?) {
        viewModelScope.launch {
            isLoading = true
            try {
                val userId = authService.currentUserId()
                if (userId == null) {
                    _error.emit("Vous devez être connecté pour créer un événement")
                    return@launch
                }

                val eventUniqueId = UUID.randomUUID().toString()

                var imageUrl: String? = null
                if (imageBytes != null) {
                    val fileName = "event_${eventUniqueId}_${System.currentTimeMillis()}"
                    imageUrl = databaseService.uploadImage(fileName, imageBytes)
                }

                // Formatage de la date pour la BDD (ISO 8601 recommandé)
                // On suppose que date est au format dd/MM/yyyy et time au format HH:mm
                val formattedDate = try {
                    val dateParts = date.split("/")
                    val timeParts = time.split(":")
                    "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}T${timeParts[0]}:${timeParts[1]}:00Z"
                } catch (e: Exception) {
                    null
                }

                val newEvent = Evenement(
                    id = eventUniqueId,
                    title = title,
                    description = description,
                    latitude = latitude ?: 0.0,
                    longitude = longitude ?: 0.0,
                    location = location,
                    image_url = imageUrl,
                    user_id = userId,
                    visibility = !isPrivate,
                    event_date = formattedDate
                )

                databaseService.addEvent(newEvent)
                _eventCreated.emit(true)
            } catch (e: Exception) {
                _error.emit("Erreur : ${e.message ?: "Une erreur est survenue"}")
            } finally {
                isLoading = false
            }
        }
    }
}
