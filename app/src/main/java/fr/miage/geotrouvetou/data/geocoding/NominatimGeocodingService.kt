package fr.miage.geotrouvetou.data.geocoding

import fr.miage.geotrouvetou.domain.interfaces.IGeocodingService
import fr.miage.geotrouvetou.domain.models.Place
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class NominatimAddress(
    @SerialName("house_number") val houseNumber: String? = null,
    val road: String? = null,
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val municipality: String? = null,
    val county: String? = null,
    val state: String? = null,
    val postcode: String? = null,
    val country: String? = null,
)

@Serializable
private data class NominatimPlace(
    @SerialName("display_name") val displayName: String,
    val lat: String,
    val lon: String,
    val address: NominatimAddress? = null,
) {
    private val street: String? get() {
        val a = address ?: return null
        return when {
            a.houseNumber != null && a.road != null -> "${a.houseNumber} ${a.road}"
            a.road != null -> a.road
            else -> null
        }
    }

    private val locality: String? get() =
        address?.let { it.city ?: it.town ?: it.village ?: it.municipality }

    private val mainLine: String get() =
        listOfNotNull(street, locality).joinToString(", ").ifBlank { displayName }

    private val countryLine: String get() {
        val a = address ?: return ""
        return listOfNotNull(a.postcode, a.county, a.country).joinToString(", ")
    }

    private val shortAddress: String get() =
        listOfNotNull(street, locality, address?.postcode).joinToString(", ").ifBlank { displayName }

    fun toPlace() = Place(
        displayName = displayName,
        latitude = lat.toDoubleOrNull() ?: 0.0,
        longitude = lon.toDoubleOrNull() ?: 0.0,
        mainLine = mainLine,
        countryLine = countryLine,
        shortAddress = shortAddress,
    )
}

class NominatimGeocodingService : IGeocodingService {

    private val client = HttpClient(OkHttp) {
        install(HttpTimeout) {
            requestTimeoutMillis = 8_000
            connectTimeoutMillis = 5_000
        }
    }
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun search(query: String, limit: Int): List<Place> {
        if (query.isBlank()) return emptyList()
        val text = client.get("https://nominatim.openstreetmap.org/search") {
            parameter("q", query)
            parameter("format", "json")
            parameter("limit", limit)
            parameter("addressdetails", 1)
            header("User-Agent", "ASIM-Geoevent/1.0")
        }.bodyAsText()

        val results: List<NominatimPlace> = json.decodeFromString(text)
        return results.distinctBy { it.toPlace().mainLine }.take(5).map { it.toPlace() }
    }
}
