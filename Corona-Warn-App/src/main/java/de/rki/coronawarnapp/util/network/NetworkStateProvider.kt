package de.rki.coronawarnapp.util.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_NOT_METERED
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
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
        send(currentState)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.tag(TAG).v("onAvailable(network=%s)", network)
                appScope.launch { send(currentState) }
            }

            override fun onUnavailable() {
                Timber.tag(TAG).v("onUnavailable()")
                appScope.launch { send(currentState) }
            }
        }

        val request = networkRequestBuilderProvider.get()
            .addCapability(NET_CAPABILITY_INTERNET)
            .build()

        try {
            /**
             * This may throw java.lang.SecurityException on Samsung devices
             * java.lang.SecurityException:
             * at android.os.Parcel.createExceptionOrNull (Parcel.java:2385)
             * at android.net.ConnectivityManager.registerNetworkCallback (ConnectivityManager.java:4564)
             */
            manager.registerNetworkCallback(request, callback)
        } catch (e: SecurityException) {
            Timber.e(e, "registerNetworkCallback() threw an undocumented SecurityException, Just Samsung Things™️")
            State(
                activeNetwork = null,
                capabilities = null,
                linkProperties = null,
            ).run { send(this) }
        }

        val fakeConnectionSubscriber = launch {
            testSettings.fakeMeteredConnection.flow.drop(1)
                .collect {
                    Timber.v("fakeMeteredConnection=%b", it)
                    send(currentState)
                }
        }

        awaitClose {
            Timber.tag(TAG).v("unregisterNetworkCallback()")
            manager.unregisterNetworkCallback(callback)
            fakeConnectionSubscriber.cancel()
        }
    }
        .shareLatest(
            tag = TAG,
            scope = appScope
        )

    private val currentState: State
        get() = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> api23NetworkState()
            else -> {
                // Most state information is not available
                State(
                    activeNetwork = null,
                    capabilities = null,
                    linkProperties = null,
                    assumeMeteredConnection = testSettings.fakeMeteredConnection.value ||
                        ConnectivityManagerCompat.isActiveNetworkMetered(manager)
                )
            }
        }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun api23NetworkState() = manager.activeNetwork.let { network ->
        State(
            activeNetwork = network,
            capabilities = network?.let {
                try {
                    manager.getNetworkCapabilities(it)
                } catch (e: SecurityException) {
                    Timber.tag(TAG).e(e, "Failed to determine network capabilities.")
                    null
                }
            },
            linkProperties = network?.let {
                try {
                    manager.getLinkProperties(it)
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Failed to determine link properties.")
                    null
                }
            },
            assumeMeteredConnection = testSettings.fakeMeteredConnection.value
        )
    }

    data class State(
        val activeNetwork: Network?,
        val capabilities: NetworkCapabilities?,
        val linkProperties: LinkProperties?,
        private val assumeMeteredConnection: Boolean = false
    ) {
        val isMeteredConnection: Boolean
            get() {
                val unMetered = if (BuildVersionWrap.hasAPILevel(Build.VERSION_CODES.N)) {
                    capabilities?.hasCapability(NET_CAPABILITY_NOT_METERED) ?: false
                } else {
                    capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
                }
                return assumeMeteredConnection || !unMetered
            }
    }

    companion object {
        private const val TAG = "NetworkStateProvider"
    }
}
