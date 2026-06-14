package fr.miage.geotrouvetou.ui.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.receivers.LocationReceiver
import fr.miage.geotrouvetou.receivers.NetworkReceiver
import fr.miage.geotrouvetou.ui.components.atoms.Toast
import fr.miage.geotrouvetou.ui.components.atoms.ToastType

private data class StatusToast(
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    val type: ToastType = ToastType.Success,
)

/**
 * Surveille la connectivité réseau et l'état du GPS via deux receivers liés au cycle de vie
 * de la composition (actifs entre ON_START et ON_STOP), et affiche un toast à chaque transition.
 */
@Composable
fun SystemStatusToasts() {
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf<Boolean?>(null) }
    var isLocationEnabled by remember { mutableStateOf<Boolean?>(null) }

    LifecycleStartEffect(Unit) {
        val networkReceiver = NetworkReceiver { isConnected = it }
        val locationReceiver = LocationReceiver { isLocationEnabled = it }
        networkReceiver.register(context)
        ContextCompat.registerReceiver(
            context,
            locationReceiver,
            LocationReceiver.intentFilter(),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        isConnected = NetworkReceiver.isConnected(context)
        isLocationEnabled = LocationReceiver.isLocationEnabled(context)
        onStopOrDispose {
            networkReceiver.unregister(context)
            context.unregisterReceiver(locationReceiver)
        }
    }

    var toastTick by remember { mutableIntStateOf(0) }
    var pendingToast by remember { mutableStateOf<StatusToast?>(null) }
    var previousConnected by remember { mutableStateOf<Boolean?>(null) }
    var previousLocationEnabled by remember { mutableStateOf<Boolean?>(null) }
    fun showToast(toast: StatusToast) {
        pendingToast = toast
        toastTick++
    }

    LaunchedEffect(isConnected) {
        when (isConnected) {
            false if previousConnected != false ->
                showToast(StatusToast(R.string.toast_network_lost_title, R.string.toast_network_lost_desc, ToastType.Warning))
            true if previousConnected == false ->
                showToast(StatusToast(R.string.toast_network_restored_title, R.string.toast_network_restored_desc))
            else -> {}
        }
        previousConnected = isConnected
    }

    LaunchedEffect(isLocationEnabled) {
        when (isLocationEnabled) {
            false if previousLocationEnabled != false ->
                showToast(StatusToast(R.string.toast_location_lost_title, R.string.toast_location_lost_desc, ToastType.Warning))
            true if previousLocationEnabled == false ->
                showToast(StatusToast(R.string.toast_location_restored_title, R.string.toast_location_restored_desc))
            else -> {}
        }
        previousLocationEnabled = isLocationEnabled
    }

    pendingToast?.let { toast ->
        Toast(
            title = stringResource(toast.title),
            description = stringResource(toast.description),
            type = toast.type,
            key = toastTick,
        )
    }
}
