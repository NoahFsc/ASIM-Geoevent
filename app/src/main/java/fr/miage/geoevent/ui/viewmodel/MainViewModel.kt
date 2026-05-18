package fr.miage.geoevent.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.miage.geoevent.data.backend.SupabaseDatabaseService
import fr.miage.geoevent.data.supabase
import fr.miage.geoevent.domain.interfaces.IDatabaseService
import fr.miage.geoevent.domain.models.GeoEvent
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel principal gérant la liste des événements et la création de nouveaux points.
 * Utilise StateFlow pour assurer une UI réactive en Compose.
 */
class MainViewModel : ViewModel() {
    private val databaseService: IDatabaseService = SupabaseDatabaseService(supabase)

    // Liste des événements observée par la MapPage
    private val _events = MutableStateFlow<List<GeoEvent>>(emptyList())
    val events: StateFlow<List<GeoEvent>> = _events.asStateFlow()

    // État de chargement pour bloquer l'UI pendant les appels réseau
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observeEvents()
    }

    /**
     * S'abonne aux changements en temps réel via Supabase Realtime.
     * Dès qu'un événement est ajouté en base (même par un autre utilisateur),
     * la liste locale est mise à jour automatiquement.
     */
    private fun observeEvents() {
        viewModelScope.launch {
            try {
                databaseService.listenToEventsRealtime().collect { newEvents ->
                    _events.value = newEvents
                }
            } catch (e: Exception) {
                // Erreur silencieuse pour ne pas bloquer l'app, les logs suffisent
            }
        }
    }

    /**
     * Gère le flux complet de création d'un événement :
     * 1. Génération d'un ID unique client-side pour lier l'image et l'event.
     * 2. Upload de l'image (si fournie) via ImageHelper.
     * 3. Insertion des données finales dans la table Postgrest.
     */
    fun createEvent(
        title: String,
        description: String,
        imageBytes: ByteArray?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = supabase.auth.currentSessionOrNull()?.user
                
                if (user == null) {
                    onError("Vous devez être connecté pour créer un événement")
                    return@launch
                }

                // ID unique pour l'événement et son image associée
                val eventUniqueId = UUID.randomUUID().toString()

                // Upload image si l'utilisateur en a sélectionné une
                var imageUrl: String? = null
                if (imageBytes != null) {
                    // Le nom de base (ImageHelper ajoutera l'extension .webp)
                    val fileName = "event_${eventUniqueId}_${System.currentTimeMillis()}"
                    imageUrl = databaseService.uploadImage(fileName, imageBytes)
                }

                val newEvent = GeoEvent(
                    id = eventUniqueId,
                    title = title,
                    description = description,
                    latitude = 49.8887, // TODO: Remplacer par la position réelle du clic
                    longitude = 2.2858,
                    image_url = imageUrl,
                    user_id = user.id
                )

                databaseService.addEvent(newEvent)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Une erreur est survenue")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
