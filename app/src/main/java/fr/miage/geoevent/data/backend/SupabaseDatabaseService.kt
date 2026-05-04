package fr.miage.geoevent.data.backend

import fr.miage.geoevent.domain.interfaces.IDatabaseService
import fr.miage.geoevent.domain.models.GeoEvent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class SupabaseDatabaseService(private val client: SupabaseClient) : IDatabaseService {

    private val tableName = "events"

    // Ajout d'un événement
    override suspend fun addEvent(event: GeoEvent) {
        client.postgrest[tableName].insert(event)
    }

    // Récupération classique
    override suspend fun getAllEvents(): List<GeoEvent> {
        return client.postgrest[tableName].select().decodeList<GeoEvent>()
    }

    // Listener Temps Réel
    override fun listenToEventsRealtime(): Flow<List<GeoEvent>> {
        val channel = client.realtime.channel("public-events")

        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = tableName
        }.map {
            // À chaque changement, on recharge la liste
            getAllEvents()
        }.onStart {
            // On s'abonne au channel au démarrage
            channel.subscribe()
            emit(getAllEvents())
        }
    }
}