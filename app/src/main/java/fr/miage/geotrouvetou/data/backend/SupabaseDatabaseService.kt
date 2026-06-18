package fr.miage.geotrouvetou.data.backend

import fr.miage.geotrouvetou.domain.interfaces.IDatabaseService
import fr.miage.geotrouvetou.domain.models.AdminStats
import fr.miage.geotrouvetou.domain.models.AuditLogEntry
import fr.miage.geotrouvetou.domain.models.Evenement
import fr.miage.geotrouvetou.domain.models.EventParticipant
import fr.miage.geotrouvetou.domain.models.User
import fr.miage.geotrouvetou.domain.interfaces.IImageService
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class SupabaseDatabaseService(
    private val client: SupabaseClient,
    private val imageService: IImageService,
) : IDatabaseService {
    private var eventsChannel: RealtimeChannel? = null

    override suspend fun addEvent(event: Evenement) {
        client.postgrest["events"].insert(event)
    }

    override suspend fun updateEvent(event: Evenement) {
        val eventId = event.id ?: return
        client.postgrest["events"].update(buildJsonObject {
            put("title", event.title)
            put("description", event.description)
            put("image_url", event.imageUrl)
            put("visibility", event.visibility)
            put("event_date", event.eventDate)
            put("latitude", event.latitude)
            put("longitude", event.longitude)
            put("location", event.location)
        }) {
            filter { eq("id", eventId) }
        }
    }

    override suspend fun getEvent(eventId: String): Evenement? {
        return client.postgrest["events"].select {
            filter { eq("id", eventId) }
        }.decodeSingleOrNull<Evenement>()
    }

    override suspend fun uploadImage(fileName: String, bytes: ByteArray): String {
        return imageService.uploadEventImage(fileName, bytes)
    }

    private fun PostgrestFilterBuilder.visibleToCurrentUser() {
        val userId = client.auth.currentUserOrNull()?.id
        if (userId != null) {
            or {
                eq("visibility", true)
                eq("user_id", userId)
            }
        } else {
            eq("visibility", true)
        }
    }

    override suspend fun getAllEvents(): List<Evenement> {
        return client.postgrest["events"]
            .select {
                filter { visibleToCurrentUser() }
            }
            .decodeList<Evenement>()
    }

    override suspend fun getEventsByVisibleBounds(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): List<Evenement> {
        return client.postgrest["events"]
            .select {
                filter {
                    and {
                        gte("latitude", minLat)
                        lte("latitude", maxLat)
                        gte("longitude", minLon)
                        lte("longitude", maxLon)
                    }
                    visibleToCurrentUser()
                }
            }
            .decodeList<Evenement>()
    }

    override fun listenToEventsRealtime(): Flow<Unit> = flow {
        eventsChannel?.unsubscribe()

        val channel = client.realtime.channel("public-events")
        eventsChannel = channel

        try {
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "events"
            }.map { Unit }

            channel.subscribe()
            emit(Unit)

            changeFlow.collect { emit(it) }
        } finally {
            channel.unsubscribe()
            eventsChannel = null
        }
    }

    override suspend fun getProfile(userId: String): User? {
        val user = client.postgrest["profiles"].select {
            filter { eq("id", userId) }
        }.decodeSingleOrNull<User>() ?: return null

        val resolvedAvatarUrl = user.avatarUrl?.let { path ->
            if (path.startsWith("http")) path
            else try { imageService.getAvatarSignedUrl(path) } catch (_: Exception) { null }
        }
        return user.copy(avatarUrl = resolvedAvatarUrl)
    }

    override suspend fun createProfile(user: User) {
        client.postgrest["profiles"].insert(user)
    }

    override suspend fun updateProfile(userId: String, fullName: String) {
        client.postgrest["profiles"].update({
            set("full_name", fullName)
        }) {
            filter { eq("id", userId) }
        }
        client.auth.updateUser {
            data = buildJsonObject { put("full_name", fullName) }
        }
    }

    override suspend fun updateAvatar(userId: String, bytes: ByteArray) {
        val avatarUrl = imageService.uploadAvatarImage(userId, bytes)
        client.postgrest["profiles"].update({
            set("avatar_url", avatarUrl)
        }) {
            filter { eq("id", userId) }
        }
    }

    override suspend fun deleteProfile(userId: String) {
        client.postgrest["profiles"].delete {
            filter {
                eq("id", userId)
            }
        }
        client.postgrest.rpc("delete_own_account")
    }

    override suspend fun joinEvent(eventId: String, userId: String) {
        client.postgrest["event_participants"].insert(EventParticipant(eventId, userId))
    }

    override suspend fun leaveEvent(eventId: String, userId: String) {
        client.postgrest["event_participants"].delete {
            filter {
                eq("event_id", eventId)
                eq("profile_id", userId)
            }
        }
    }

    override suspend fun isUserParticipating(eventId: String, userId: String): Boolean {
        return client.postgrest["event_participants"].select {
            filter {
                eq("event_id", eventId)
                eq("profile_id", userId)
            }
        }.decodeList<EventParticipant>().isNotEmpty()
    }

    override suspend fun getParticipantsCount(eventId: String): Int {
        val response = client.postgrest["event_participants"].select {
            filter {
                eq("event_id", eventId)
            }
        }.decodeList<EventParticipant>()
        return response.size
    }

    override suspend fun getAdminStats(): AdminStats {
        val userCount = client.postgrest["profiles"].select().decodeList<User>().size
        val eventCount = client.postgrest["events"].select().decodeList<Evenement>().size
        return AdminStats(userCount = userCount, eventCount = eventCount)
    }

    override suspend fun getAdminUsers(page: Int, pageSize: Int): List<User> {
        val from = (page * pageSize).toLong()
        val to = ((page + 1) * pageSize - 1).toLong()
        return client.postgrest["profiles"]
            .select { range(from = from, to = to) }
            .decodeList<User>()
    }

    override suspend fun getAdminEvents(page: Int, pageSize: Int): List<Evenement> {
        val from = (page * pageSize).toLong()
        val to = ((page + 1) * pageSize - 1).toLong()
        return client.postgrest["events"]
            .select {
                range(from = from, to = to)
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<Evenement>()
    }

    override suspend fun getRecentActivity(limit: Int): List<AuditLogEntry> {
        return client.postgrest["audit_log"]
            .select {
                order(column = "created_at", order = Order.DESCENDING)
                this.limit(count = limit.toLong())
            }
            .decodeList<AuditLogEntry>()
    }

    override suspend fun updateUserRole(userId: String, role: String) {
        client.postgrest["profiles"].update(
            update = { set("role", role) },
            request = { filter { eq("id", userId) } },
        )
    }

    override suspend fun adminDeleteUser(userId: String) {
        client.postgrest.rpc("admin_delete_user", mapOf("target_user_id" to userId))
    }

    override suspend fun deleteEvent(eventId: String) {
        client.postgrest["events"].delete {
            filter { eq("id", eventId) }
        }
    }
}
