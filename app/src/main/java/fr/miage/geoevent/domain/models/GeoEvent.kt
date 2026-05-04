package fr.miage.geoevent.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class GeoEvent(
    val id: String = "",
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val image_url: String? = null,
    val user_id: String? = null
)