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

    fun getVisibleBounds(): MapBounds?
    fun setOnViewBoundsChangedListener(listener: ((MapBounds) -> Unit)?)
    fun setMinimumZoomForWidth(widthKm: Double)

    fun setOnEventClickListener(listener: ((Evenement) -> Unit)?)
    fun setOnClusterClickListener(listener: ((List<Evenement>) -> Unit)?)
}