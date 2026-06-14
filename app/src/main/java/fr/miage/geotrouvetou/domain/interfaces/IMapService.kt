package fr.miage.geotrouvetou.domain.interfaces

import fr.miage.geotrouvetou.domain.models.Evenement
import org.osmdroid.views.MapView

data class MapBounds(
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double
)

interface IMapService {
    fun bind(mapView: MapView)
    fun onResume()
    fun onPause()
    fun enableMyLocation(onFirstFix: ((Evenement) -> Unit)? = null)
    fun disableMyLocation()
    fun centerOn(latitude: Double, longitude: Double, zoom: Double = 15.0)
    fun getZoomForWidth(widthKm: Double, latitude: Double): Double
    fun addMarker(event: Evenement)
    fun displayEvents(events: List<Evenement>)

    // Retourne les limites visibles actuelles (lat/lon min/max)
    fun getVisibleBounds(): MapBounds?

    // Écoute les changements de zoom/pan pour recharger les events
    fun setOnViewBoundsChangedListener(listener: ((MapBounds) -> Unit)?)

    // Contraint le zoom minimum pour que la largeur visible soit d'au moins widthKm
    fun setMinimumZoomForWidth(widthKm: Double)

    // Appelé quand l'utilisateur tape sur un marker individuel
    fun setOnEventClickListener(listener: ((Evenement) -> Unit)?)

    // Appelé quand l'utilisateur tape sur un cluster
    fun setOnClusterClickListener(listener: ((List<Evenement>) -> Unit)?)
}