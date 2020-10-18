package de.rki.coronawarnapp.util.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationProvider @Inject constructor(
    @AppContext private val context: Context,
    @AppScope private val appScope: CoroutineScope
) {
    private val locationManager: LocationManager
        get() = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val isLocationEnabled: Flow<Boolean> = callbackFlow {
        send(startingState)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action?.matches(INTENT_ACTION.toRegex()) != true) {
                    Timber.d("Unknown intent action: %s", intent)
                    return
                }

                val isGpsEnabled = locationManager.isProviderEnabled(
                    LocationManager.GPS_PROVIDER
                )
                val isNetworkEnabled = locationManager.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
                )

                this@callbackFlow.launch {
                    (isGpsEnabled || isNetworkEnabled).let {
                        this@callbackFlow.send(it)
                        Timber.d("Location available update: enabled=$it")
                    }
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(INTENT_ACTION))
        awaitClose { context.unregisterReceiver(receiver) }
    }
        .onStart { Timber.v("locationState FLOW start") }
        .onEach { Timber.v("locationState FLOW emission: %b", it) }
        .onCompletion { Timber.v("locationState FLOW completed.") }
        .stateIn(
            scope = appScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )
        .mapNotNull { it }

    private val startingState: Boolean
        get() = LocationManagerCompat.isLocationEnabled(locationManager)

    companion object {
        private const val INTENT_ACTION = "android.location.PROVIDERS_CHANGED"
    }
}
