package fr.miage.geotrouvetou.domain.interfaces

import fr.miage.geotrouvetou.domain.models.Place

/** Abstraction du géocodage, indépendante du fournisseur (Nominatim/OSM aujourd'hui). */
interface IGeocodingService {
    suspend fun search(query: String, limit: Int = 8): List<Place>
}
