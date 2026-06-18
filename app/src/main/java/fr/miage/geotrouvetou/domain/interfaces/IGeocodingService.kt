package fr.miage.geotrouvetou.domain.interfaces

import fr.miage.geotrouvetou.domain.models.Place

interface IGeocodingService {
    suspend fun search(query: String, limit: Int = 8): List<Place>
}
