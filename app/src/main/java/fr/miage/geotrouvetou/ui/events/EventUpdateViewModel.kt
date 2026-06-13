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
import java.text.SimpleDateFormat
import java.util.Locale

class EventUpdateViewModel(
    private val databaseService: IDatabaseService,
    private val authService: IAuthService,
) : ViewModel() {

    private var originalEvent: Evenement? = null

    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var date by mutableStateOf("")
    var time by mutableStateOf("")
    var location by mutableStateOf("")
    var latitude by mutableStateOf<Double?>(null)
    var longitude by mutableStateOf<Double?>(null)
    var isPrivate by mutableStateOf(false)
    var imageUri by mutableStateOf<Uri?>(null)
    var currentImageUrl by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
        private set

    private val _eventUpdated = MutableSharedFlow<Boolean>()
    val eventUpdated = _eventUpdated.asSharedFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    val isFormValid: Boolean
        get() = title.isNotBlank() && description.isNotBlank() && date.isNotBlank() && time.isNotBlank() && (latitude != null && longitude != null) && (imageUri != null || currentImageUrl != null) && !isLoading

    fun setEvent(event: Evenement) {
        if (originalEvent != null) return
        originalEvent = event
        title = event.title
        description = event.description
        currentImageUrl = event.image_url
        isPrivate = !(event.visibility)
        latitude = event.latitude
        longitude = event.longitude
        location = event.location ?: ""
        
        // Parsing event_date "2024-04-28T10:00:00Z" ou "2024-04-28T10:00:00"
        event.event_date?.let { isoDate ->
            try {
                // On essaie de parser les 16 premiers caractères yyyy-MM-ddTHH:mm
                val cleanDate = isoDate.replace("Z", "").substring(0, 16)
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
                val dateObj = inputFormat.parse(cleanDate)
                if (dateObj != null) {
                    date = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(dateObj)
                    time = SimpleDateFormat("HH:mm", Locale.FRANCE).format(dateObj)
                }
            } catch (e: Exception) {
                // Fallback: si le format est différent, on peut essayer de mettre des valeurs par défaut ou logguer
            }
        }
    }

    fun updateEvent(imageBytes: ByteArray?) {
        val eventId = originalEvent?.id ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                if (authService.currentUserId() == null) {
                    _error.emit("Vous devez être connecté pour modifier un événement")
                    return@launch
                }

                var imageUrl = currentImageUrl
                if (imageBytes != null) {
                    val fileName = "event_${eventId}_${System.currentTimeMillis()}"
                    imageUrl = databaseService.uploadImage(fileName, imageBytes)
                }

                val formattedDate = try {
                    val dateParts = date.split("/")
                    val timeParts = time.split(":")
                    "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}T${timeParts[0]}:${timeParts[1]}:00Z"
                } catch (e: Exception) {
                    originalEvent?.event_date
                }

                val updatedEvent = originalEvent!!.copy(
                    title = title,
                    description = description,
                    latitude = latitude ?: originalEvent!!.latitude,
                    longitude = longitude ?: originalEvent!!.longitude,
                    location = location,
                    image_url = imageUrl,
                    visibility = !isPrivate,
                    event_date = formattedDate
                )

                databaseService.updateEvent(updatedEvent)
                _eventUpdated.emit(true)
            } catch (e: Exception) {
                _error.emit("Erreur : ${e.message ?: "Une erreur est survenue"}")
            } finally {
                isLoading = false
            }
        }
    }
}
