package de.rki.coronawarnapp.util.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_NOT_METERED
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.net.ConnectivityManagerCompat
import de.rki.coronawarnapp.storage.TestSettings
import de.rki.coronawarnapp.util.BuildVersionWrap
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.flow.shareLatest
import de.rki.coronawarnapp.util.hasAPILevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStateProvider @Inject constructor(
    @AppContext private val context: Context,
    @AppScope private val appScope: CoroutineScope,
    private val testSettings: TestSettings,
    private val networkRequestBuilderProvider: NetworkRequestBuilderProvider
) {
    private val manager: ConnectivityManager
        get() = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkState: Flow<State> = callbackFlow {
        send(currentState())

        val request = networkRequestBuilderProvider.get()
            .addCapability(NET_CAPABILITY_INTERNET)
            .build()

        var registeredCallback: ConnectivityManager.NetworkCallback? = null

        try {

            fun refreshState(delayValue: Long = 0) {
                appScope.launch {
                    delay(delayValue)
                    send(currentState())
                }
            }

            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    Timber.tag(TAG).v("onAvailable(network=%s)", network)
                    refreshState()
                }

                /**
                 * We provide a little delay because some devices (Pixel2, Samsung *) don't update the
                 * active network fast enought. 200ms gave good results.
                 */

                override fun onLost(network: Network) {
                    Timber.tag(TAG).v("onLost(network=%s)", network)
                    refreshState(200)
                }

                /**
                 * Not consistently called in all Android versions
                 * https://issuetracker.google.com/issues/144891976
                 */
                override fun onUnavailable() {
                    Timber.tag(TAG).v("onUnavailable()")
                    refreshState()
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    Timber.tag(TAG).v("onLosing(network=%s, maxMsToLive=%s)", network, maxMsToLive)
                    refreshState()
                }

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    Timber.tag(TAG).v(
                        "onCapabilitiesChanged(network=%s, capabilities=%s)", network, networkCapabilities
                    )
                    refreshState()
                }

                override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                    Timber.tag(TAG).v("onLinkPropertiesChanged(network=%s, properties=...)", network)
                    refreshState()
                }

                override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
                    Timber.tag(TAG).v("onBlockedStatusChanged(network=%s, blocked=%s)", network, blocked)
                    refreshState()
                }
            }

            /**
             * This may throw java.lang.SecurityException on Samsung devices
             * java.lang.SecurityException:
             * at android.os.Parcel.createExceptionOrNull (Parcel.java:2385)
             * at android.net.ConnectivityManager.registerNetworkCallback (ConnectivityManager.java:4564)
             */
            manager.registerNetworkCallback(request, callback)
            registeredCallback = callback
        } catch (e: SecurityException) {
            Timber.e(e, "registerNetworkCallback() threw an undocumented SecurityException, Just Samsung Things™️")
            send(FallbackState)
        }

        val fakeConnectionSubscriber = launch {
            testSettings.fakeMeteredConnection.drop(1)
                .collect {
                    Timber.v("fakeMeteredConnection=%b", it)
                    send(currentState())
                }
        }

        awaitClose {
            Timber.tag(TAG).v("unregisterNetworkCallback(%s)", registeredCallback)
            registeredCallback?.let { manager.unregisterNetworkCallback(it) }
            fakeConnectionSubscriber.cancel()
        }
    }
        .distinctUntilChanged()
        .shareLatest(
            tag = TAG,
            scope = appScope
        )

    private suspend fun currentState(): State = when {
        BuildVersionWrap.hasAPILevel(Build.VERSION_CODES.M) -> modernNetworkState()
        else -> legacyNetworkState()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private suspend fun modernNetworkState(): State = manager.activeNetwork.let { network ->
        ModernState(
            activeNetwork = network,
            capabilities = network?.let {
                try {
                    manager.getNetworkCapabilities(it)
                } catch (e: SecurityException) {
                    Timber.tag(TAG).e(e, "Failed to determine network capabilities.")
                    null
                }
            },
            assumeMeteredConnection = testSettings.fakeMeteredConnection.first()
        )
    }

    @Suppress("DEPRECATION")
    private suspend fun legacyNetworkState(): State = StateLegacyAPI21(
        isInternetAvailable = manager.activeNetworkInfo?.isConnected ?: false,
        isMeteredConnection = testSettings.fakeMeteredConnection.first() ||
            ConnectivityManagerCompat.isActiveNetworkMetered(manager)
    )

    interface State {
        val isMeteredConnection: Boolean
        val isInternetAvailable: Boolean
    }

    data class StateLegacyAPI21(
        override val isMeteredConnection: Boolean,
        override val isInternetAvailable: Boolean
    ) : State

    data class ModernState(
        val activeNetwork: Network?,
        val capabilities: NetworkCapabilities?,
        private val assumeMeteredConnection: Boolean = false
    ) : State {
        override val isInternetAvailable: Boolean
            get() = capabilities?.hasCapability(NET_CAPABILITY_VALIDATED) ?: false

        override val isMeteredConnection: Boolean
            get() {
                val unMetered = if (BuildVersionWrap.hasAPILevel(Build.VERSION_CODES.N)) {
                    capabilities?.hasCapability(NET_CAPABILITY_NOT_METERED) ?: false
                } else {
                    capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
                }
                return assumeMeteredConnection || !unMetered
            }
    }

    object FallbackState : State {
        override val isMeteredConnection: Boolean = true
        override val isInternetAvailable: Boolean = true
    }

    companion object {
        private const val TAG = "NetworkStateProvider"
    }
}
