package fr.miage.geoevent.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class GeoEvent(
    // L'ID doit être null par défaut pour que Supabase utilise sa fonction uuid_generate_v4() (cf. ton screenshot)
    val id: String? = null,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val image_url: String? = null,
    val user_id: String? = null,
    // created_at est géré automatiquement par la BDD
    val created_at: String? = null
)
