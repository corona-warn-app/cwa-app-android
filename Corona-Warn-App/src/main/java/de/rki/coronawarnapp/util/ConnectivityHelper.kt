package de.rki.coronawarnapp.util

import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.core.location.LocationManagerCompat
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import timber.log.Timber

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
        context.registerReceiver(
            callback.recevier,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
        // bluetooth state doesn't change when you register
        if (isBluetoothEnabled())
            callback.onBluetoothAvailable()
        else
            callback.onBluetoothUnavailable()
    }

    /**
     * Unregister bluetooth state change listener.
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
     * Register location state change listener.
     *
     * @param context the context
     * @param callback the location state callback
     *
     */
    fun registerLocationStatusCallback(context: Context, callback: LocationCallback) {
            val receiver = object : BroadcastReceiver() {
                var isGpsEnabled: Boolean = false
                var isNetworkEnabled: Boolean = false

                override fun onReceive(context: Context, intent: Intent) {
                    intent.action?.let { act ->
                        if (act.matches("android.location.PROVIDERS_CHANGED".toRegex())) {
                            val locationManager =
                                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                            isGpsEnabled =
                                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                            isNetworkEnabled =
                                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                            if (isGpsEnabled || isNetworkEnabled) {
                                callback.onLocationAvailable()
                                Timber.d("Location enabled")
                            } else {
                                callback.onLocationUnavailable()
                                Timber.d("Location disabled")
                            }
                        }
                    }
                }
        }
        callback.recevier = receiver
        context.registerReceiver(
            callback.recevier,
            IntentFilter("android.location.PROVIDERS_CHANGED")
        )
        // location state doesn't change when you register
        if (isLocationEnabled(context))
            callback.onLocationAvailable()
        else
            callback.onLocationUnavailable()
    }

    /**
     * Unregister location state change listener.
     *
     * @param context the context
     * @param callback the location state callback
     *
     */
    fun unregisterLocationStatusCallback(context: Context, callback: LocationCallback) {
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
     * For API level 28+ check if background is restricted
     * Else always return false
     *
     * @param context the context
     *
     * @return Boolean
     */
    fun isBackgroundRestricted(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activityManager.isBackgroundRestricted
        } else false
    }

    /**
     * Background jobs are enabled only if the battery optimization is enabled and
     * the background activity is not restricted
     *
     * @param context the context
     *
     * @return Boolean
     *
     * @see isBackgroundRestricted
     */
    fun autoModeEnabled(context: Context): Boolean {
        return !isBackgroundRestricted(context) || PowerManagementHelper.isIgnoringBatteryOptimizations(context)
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
            Timber.d("Device does not have bluetooth hardware")
            return false
        }
        return bAdapter.isEnabled
    }

    /**
     * Get location enabled status.
     *
     * @return current location status
     *
     */
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
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
     * Abstract location state change callback.
     *
     * @see BroadcastReceiver
     */
    abstract class LocationCallback {
        var recevier: BroadcastReceiver? = null

        /**
         * Called when location is turned on.
         */
        abstract fun onLocationAvailable()

        /**
         * Called when location is turned off.
         */
        abstract fun onLocationUnavailable()
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
