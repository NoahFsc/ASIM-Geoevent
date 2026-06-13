package fr.miage.geotrouvetou.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Evenement(
    val id: String? = null,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val location: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("event_date") val eventDate: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val visibility: Boolean = true,
)
