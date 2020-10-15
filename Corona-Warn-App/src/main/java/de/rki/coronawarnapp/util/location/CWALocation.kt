package de.rki.coronawarnapp.util.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CWALocation @Inject constructor(
    @AppContext private val context: Context
) {
    private val locationManager: LocationManager
        get() = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val isLocationEnabled: Flow<Boolean>
        get() = locationState

    private val locationState by lazy {
        MutableStateFlow(startingState)
            .apply {
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

                        value = isGpsEnabled || isNetworkEnabled
                        Timber.d("Location available update: enabled=$value")
                    }
                }
                context.registerReceiver(receiver, IntentFilter(INTENT_ACTION))
            }
            .onStart { Timber.v("locationState FLOW start") }
            .onEach { Timber.v("locationState FLOW emission: %b", it) }
            .onCompletion { Timber.v("locationState FLOW completed.") }
    }

    private val startingState: Boolean
        get() = LocationManagerCompat.isLocationEnabled(locationManager)

    companion object {
        private const val INTENT_ACTION = "android.location.PROVIDERS_CHANGED"
    }
}
