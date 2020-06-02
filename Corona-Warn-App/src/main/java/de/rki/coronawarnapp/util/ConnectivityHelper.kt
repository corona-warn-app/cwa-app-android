package de.rki.coronawarnapp.util

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.report

object ConnectivityHelper {
    private val TAG: String? = ConnectivityHelper::class.simpleName

    fun unregisterNetworkStatusCallback(context: Context, callback: NetworkCallback) {
        try {
            val manager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            manager.unregisterNetworkCallback(callback)
        } catch (e: Exception) {
            e.report(
                ExceptionCategory.CONNECTIVITY,
                TAG,
                null
            )
        }
    }

    fun registerNetworkStatusCallback(context: Context, callback: NetworkCallback) {
        try {
            // If there are no Wi-Fi or mobile data presented when callback is registered
            // none of NetworkCallback methods are called
            callback.onNetworkUnavailable()

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                .build()
            val manager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            manager.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            e.report(
                ExceptionCategory.CONNECTIVITY,
                TAG,
                null
            )
        }
    }

    fun isBluetoothEnabled(): Boolean {
        val bAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bAdapter == null) {
            Log.d(TAG, "Device does not have bluetooth hardware")
            return false
        }
        return bAdapter.isEnabled
    }

    abstract class NetworkCallback : ConnectivityManager.NetworkCallback() {
        abstract fun onNetworkAvailable()

        abstract fun onNetworkUnavailable()

        override fun onAvailable(network: Network?) {
            onNetworkAvailable()
        }

        override fun onUnavailable() {
            onNetworkUnavailable()
        }

        override fun onLost(network: Network?) {
            onNetworkUnavailable()
        }
    }
}
