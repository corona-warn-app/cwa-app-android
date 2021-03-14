package de.rki.coronawarnapp.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report

/**
 * Helper for connectivity statuses.
 */
object ConnectivityHelper {
    private val TAG: String? = ConnectivityHelper::class.simpleName

    /**
     * Unregister network state change callback.
     *
     * @param context the context
     * @param callback the network state callback
     *
     * @see [ConnectivityManager]
     */
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

    /**
     * Register network state change callback.
     *
     * @param context the context
     * @param callback the network state callback
     *
     * @see [ConnectivityManager]
     * @see [NetworkCapabilities]
     * @see [NetworkRequest]
     */
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

    /**
     * Get network enabled status.
     *
     * @return current network status
     *
     */
    fun isNetworkEnabled(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: Network? = manager.activeNetwork
        val caps: NetworkCapabilities? = manager.getNetworkCapabilities(activeNetwork)
        return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false
    }

    /**
     * Abstract network state change callback.
     *
     * @see [ConnectivityManager.NetworkCallback]
     */
    abstract class NetworkCallback : ConnectivityManager.NetworkCallback() {

        /**
         * Called when network is available.
         */
        abstract fun onNetworkAvailable()

        /**
         * Called when network is unavailable or lost.
         */
        abstract fun onNetworkUnavailable()

        override fun onAvailable(network: Network) {
            onNetworkAvailable()
        }

        override fun onUnavailable() {
            onNetworkUnavailable()
        }

        override fun onLost(network: Network) {
            onNetworkUnavailable()
        }
    }
}
