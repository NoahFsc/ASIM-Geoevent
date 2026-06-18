package fr.miage.geotrouvetou.ui.events

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.domain.interfaces.IAuthService
import fr.miage.geotrouvetou.domain.interfaces.IDatabaseService
import fr.miage.geotrouvetou.domain.models.Evenement
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class EventFormViewModel(
    application: Application,
    private val databaseService: IDatabaseService,
    private val authService: IAuthService,
) : AndroidViewModel(application) {

    private var originalEvent: Evenement? = null
    val isEditMode: Boolean get() = originalEvent != null

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
        private set
    var isLoading by mutableStateOf(false)
        private set

    private val _saved = MutableSharedFlow<Boolean>()
    val saved = _saved.asSharedFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    val isFormValid: Boolean
        get() = title.isNotBlank() && description.isNotBlank() && date.isNotBlank() &&
                time.isNotBlank() && latitude != null && longitude != null &&
                (imageUri != null || currentImageUrl != null) && !isLoading

    fun load(event: Evenement?) {
        if (event == null || originalEvent != null) return
        originalEvent = event
        title = event.title
        description = event.description
        currentImageUrl = event.imageUrl
        isPrivate = !event.visibility
        latitude = event.latitude
        longitude = event.longitude
        location = event.location ?: ""

        event.eventDate?.let { isoDate ->
            try {
                val cleanDate = isoDate.replace("Z", "").substring(0, 16)
                val parsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).parse(cleanDate)
                if (parsed != null) {
                    date = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(parsed)
                    time = SimpleDateFormat("HH:mm", Locale.FRANCE).format(parsed)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun save(imageBytes: ByteArray?) {
        viewModelScope.launch {
            isLoading = true
            try {
                val userId = authService.currentUserId()
                if (userId == null) {
                    _error.emit(getApplication<Application>().getString(R.string.event_error_must_login_save))
                    return@launch
                }

                val original = originalEvent
                val eventId = original?.id ?: UUID.randomUUID().toString()

                var imageUrl = currentImageUrl
                if (imageBytes != null) {
                    val fileName = "event_${eventId}_${System.currentTimeMillis()}"
                    imageUrl = databaseService.uploadImage(fileName, imageBytes)
                }

                val formattedDate = formatEventDate() ?: original?.eventDate

                val base = original ?: Evenement(
                    id = eventId,
                    title = title,
                    description = description,
                    latitude = latitude ?: 0.0,
                    longitude = longitude ?: 0.0,
                    userId = userId,
                )
                val event = base.copy(
                    title = title,
                    description = description,
                    latitude = latitude ?: base.latitude,
                    longitude = longitude ?: base.longitude,
                    location = location,
                    imageUrl = imageUrl,
                    visibility = !isPrivate,
                    eventDate = formattedDate,
                )

                if (original == null) databaseService.addEvent(event)
                else databaseService.updateEvent(event)
                _saved.emit(true)
            } catch (e: Exception) {
                val app = getApplication<Application>()
                _error.emit(app.getString(R.string.event_error_generic, e.message ?: app.getString(R.string.event_error_fallback)))
            } finally {
                isLoading = false
            }
        }
    }

    private fun formatEventDate(): String? = try {
        val dateParts = date.split("/")
        val timeParts = time.split(":")
        "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}T${timeParts[0]}:${timeParts[1]}:00Z"
    } catch (_: Exception) {
        null
    }
}
