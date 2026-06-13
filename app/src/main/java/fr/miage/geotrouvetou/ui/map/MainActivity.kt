package fr.miage.geotrouvetou.ui.map

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import fr.miage.geotrouvetou.R
import fr.miage.geotrouvetou.receivers.LocationReceiver
import fr.miage.geotrouvetou.receivers.NetworkReceiver
import fr.miage.geotrouvetou.ui.components.atoms.Toast
import fr.miage.geotrouvetou.ui.components.atoms.ToastType
import fr.miage.geotrouvetou.ui.navigation.NavGraph

class MainActivity : AppCompatActivity() {

    private val isConnected = mutableStateOf<Boolean?>(null)
    private val isLocationEnabled = mutableStateOf<Boolean?>(null)

    private val networkReceiver = NetworkReceiver { connected -> isConnected.value = connected }
    private val locationReceiver = LocationReceiver { enabled -> isLocationEnabled.value = enabled }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            NavGraph(navController = navController)
            SystemStatusToasts(
                isConnected = isConnected.value,
                isLocationEnabled = isLocationEnabled.value,
            )
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(networkReceiver, NetworkReceiver.intentFilter(), RECEIVER_NOT_EXPORTED)
        registerReceiver(locationReceiver, LocationReceiver.intentFilter(), RECEIVER_NOT_EXPORTED)
        isConnected.value = NetworkReceiver.isConnected(this)
        isLocationEnabled.value = LocationReceiver.isLocationEnabled(this)
    }

    override fun onStop() {
        unregisterReceiver(networkReceiver)
        unregisterReceiver(locationReceiver)
        super.onStop()
    }
}

@Composable
private fun SystemStatusToasts(isConnected: Boolean?, isLocationEnabled: Boolean?) {
    var networkLostToastKey by remember { mutableIntStateOf(0) }
    var networkRestoredToastKey by remember { mutableIntStateOf(0) }
    var locationLostToastKey by remember { mutableIntStateOf(0) }
    var locationRestoredToastKey by remember { mutableIntStateOf(0) }
    var previousConnected by remember { mutableStateOf<Boolean?>(null) }
    var previousLocationEnabled by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(isConnected) {
        when {
            isConnected == false && previousConnected != false -> networkLostToastKey++
            isConnected == true && previousConnected == false -> networkRestoredToastKey++
        }
        previousConnected = isConnected
    }

    LaunchedEffect(isLocationEnabled) {
        when {
            isLocationEnabled == false && previousLocationEnabled != false -> locationLostToastKey++
            isLocationEnabled == true && previousLocationEnabled == false -> locationRestoredToastKey++
        }
        previousLocationEnabled = isLocationEnabled
    }

    if (networkLostToastKey > 0) {
        Toast(
            title = stringResource(R.string.toast_network_lost_title),
            description = stringResource(R.string.toast_network_lost_desc),
            type = ToastType.Warning,
            key = networkLostToastKey,
        )
    }
    if (networkRestoredToastKey > 0) {
        Toast(
            title = stringResource(R.string.toast_network_restored_title),
            description = stringResource(R.string.toast_network_restored_desc),
            key = networkRestoredToastKey,
        )
    }
    if (locationLostToastKey > 0) {
        Toast(
            title = stringResource(R.string.toast_location_lost_title),
            description = stringResource(R.string.toast_location_lost_desc),
            type = ToastType.Warning,
            key = locationLostToastKey,
        )
    }
    if (locationRestoredToastKey > 0) {
        Toast(
            title = stringResource(R.string.toast_location_restored_title),
            description = stringResource(R.string.toast_location_restored_desc),
            key = locationRestoredToastKey,
        )
    }
}
