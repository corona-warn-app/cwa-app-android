package de.rki.coronawarnapp.util

import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report

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
     * Checks if background jobs are enabled
     *
     * @param context the context
     *
     * @return Boolean
     *
     * @see isDataSaverEnabled
     * @see isBackgroundRestricted
     */
    fun isBackgroundJobEnabled(context: Context): Boolean {
        return !(isDataSaverEnabled(context) || isBackgroundRestricted(context))
    }

    /**
     * For API level 24+ check if data saver is enabled
     * Else always return false
     *
     * @param context the context
     *
     * @return Boolean
     *
     * @see ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED
     */
    private fun isDataSaverEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.restrictBackgroundStatus != ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED
        } else false
    }

    /**
     * For API level 28+ check if background is restricted
     * Else always return false
     *
     * @param context the context
     *
     * @return Boolean
     *
     * @see isBackgroundRestricted
     */
    private fun isBackgroundRestricted(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return activityManager.isBackgroundRestricted
        } else return false
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
