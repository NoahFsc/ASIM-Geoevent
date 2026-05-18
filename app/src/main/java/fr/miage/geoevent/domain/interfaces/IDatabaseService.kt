package fr.miage.geoevent.domain.interfaces

import fr.miage.geoevent.domain.models.GeoEvent
import kotlinx.coroutines.flow.Flow

interface IDatabaseService {
    suspend fun addEvent(event: GeoEvent)
    suspend fun getAllEvents(): List<GeoEvent>
    fun listenToEventsRealtime(): Flow<List<GeoEvent>>
    // Télécharge une image dans le bucket et retourne son URL publique
    suspend fun uploadImage(fileName: String, bytes: ByteArray): String
}
