package fr.miage.geotrouvetou.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager

/**
 * Notifie l'activation et la désactivation de la localisation de l'appareil.
 */
class LocationReceiver(
    private val onLocationStateChanged: (isEnabled: Boolean) -> Unit,
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != LocationManager.PROVIDERS_CHANGED_ACTION) return
        onLocationStateChanged(isLocationEnabled(context))
    }

    companion object {
        fun intentFilter() = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)

        fun isLocationEnabled(context: Context): Boolean {
            val locationManager = context.getSystemService(LocationManager::class.java)
            return locationManager.isLocationEnabled
        }
    }
}
