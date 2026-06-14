package fr.miage.geotrouvetou.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventParticipant(
    @SerialName("event_id") val eventId: String,
    @SerialName("profile_id") val profileId: String
)
