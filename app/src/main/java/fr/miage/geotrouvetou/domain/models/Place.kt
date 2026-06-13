package fr.miage.geotrouvetou.domain.models

/** Résultat de géocodage neutre, découplé du fournisseur. */
data class Place(
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    /** Rue + ville, ex. "10 Rue de la Paix, Paris". */
    val mainLine: String,
    /** Code postal, département, pays, ex. "75001, Paris, France". */
    val countryLine: String,
)
