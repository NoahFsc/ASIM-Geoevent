package fr.miage.geotrouvetou.domain.models

data class Place(
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val mainLine: String,
    val countryLine: String,
    val shortAddress: String,
)
