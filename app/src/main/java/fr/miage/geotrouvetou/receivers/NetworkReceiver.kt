package fr.miage.geotrouvetou.receivers

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

class NetworkReceiver(
    private val onConnectivityChanged: (isConnected: Boolean) -> Unit,
) {
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun register(context: Context) {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = onConnectivityChanged(true)
            override fun onLost(network: Network) = onConnectivityChanged(false)
        }
        networkCallback = callback
        cm.registerNetworkCallback(request, callback)
    }

    fun unregister(context: Context) {
        networkCallback?.let {
            context.getSystemService(ConnectivityManager::class.java).unregisterNetworkCallback(it)
            networkCallback = null
        }
    }

    companion object {
        fun isConnected(context: Context): Boolean {
            val cm = context.getSystemService(ConnectivityManager::class.java)
            return cm.getNetworkCapabilities(cm.activeNetwork)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        }
    }
}
