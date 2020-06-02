package de.rki.coronawarnapp.util

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.provider.Settings
import android.util.Log
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.report

// TODO integrate the ConnectivityHelper into the UI logic (e.g. RiskLevelTransaction)
/**
 * Helper for connectivity statuses.
 */
object ConnectivityHelper {
    private val TAG: String? = ConnectivityHelper::class.simpleName

    /**
     * Register bluetooth state change listener.
     *
     * @param context the context
     * @param callback the bluetooth state callback
     *
     * @see [BluetoothAdapter.ACTION_STATE_CHANGED]
     * @see [BluetoothCallback]
     */
    fun registerBluetoothStatusCallback(context: Context, callback: BluetoothCallback) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action
                if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {
                    when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                        BluetoothAdapter.STATE_OFF -> {
                            callback.onBluetoothUnavailable()
                        }
                        BluetoothAdapter.STATE_ON -> {
                            callback.onBluetoothAvailable()
                        }
                    }
                }
            }
        }
        callback.recevier = receiver
        context.registerReceiver(callback.recevier, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        // bluetooth state doesn't change when you register
        if (isBluetoothEnabled())
            callback.onBluetoothAvailable()
        else
            callback.onBluetoothUnavailable()
    }

    /**
     * Register bluetooth state change listener.
     *
     * @param context the context
     * @param callback the bluetooth state callback
     *
     * @see [BluetoothCallback]
     */
    fun unregisterBluetoothStatusCallback(context: Context, callback: BluetoothCallback) {
        context.unregisterReceiver(callback.recevier)
        callback.recevier = null
    }

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
     * Get bluetooth enabled status.
     *
     * @return current bluetooth status
     *
     * @see [BluetoothAdapter]
     */
    fun isBluetoothEnabled(): Boolean {
        val bAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bAdapter == null) {
            Log.d(TAG, "Device does not have bluetooth hardware")
            return false
        }
        return bAdapter.isEnabled
    }

    /**
     * Abstract bluetooth state change callback.
     *
     * @see BroadcastReceiver
     */
    abstract class BluetoothCallback {
        var recevier: BroadcastReceiver? = null

        /**
         * Called when bluetooth is turned on.
         */
        abstract fun onBluetoothAvailable()

        /**
         * Called when bluetooth is turned off.
         */
        abstract fun onBluetoothUnavailable()
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
