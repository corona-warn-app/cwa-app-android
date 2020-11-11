package de.rki.coronawarnapp.util.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_NOT_METERED
import de.rki.coronawarnapp.storage.TestSettings
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.flow.shareLatest
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
        manager.registerNetworkCallback(request, callback)

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
        get() = manager.activeNetwork.let { network ->
            State(
                activeNetwork = network,
                capabilities = network?.let { manager.getNetworkCapabilities(it) },
                linkProperties = network?.let { manager.getLinkProperties(it) },
                isFakeMeteredConnection = testSettings.fakeMeteredConnection.value
            )
        }

    data class State(
        val activeNetwork: Network?,
        val capabilities: NetworkCapabilities?,
        val linkProperties: LinkProperties?,
        private val isFakeMeteredConnection: Boolean = false
    ) {
        val isMeteredConnection: Boolean
            get() = isFakeMeteredConnection || !(capabilities?.hasCapability(NET_CAPABILITY_NOT_METERED) ?: false)
    }

    companion object {
        private const val TAG = "NetworkStateProvider"
    }
}
