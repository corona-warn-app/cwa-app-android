package de.rki.coronawarnapp.tracing

import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.util.bluetooth.CWABluetooth
import de.rki.coronawarnapp.util.location.CWALocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TracingStatus @Inject constructor(
    cwaBluetooth: CWABluetooth,
    cwaLocation: CWALocation,
    enfClient: ENFClient,
) {

    val tracingStatus: Flow<Status> = combine(
        cwaBluetooth.isBluetoothEnabled,
        cwaLocation.isLocationEnabled,
        enfClient.isTracingEnabled
    ) { values ->
        val bluetooth = values[0]
        val location = values[1]
        val tracing = values[2]

        when {
            !tracing -> Status.TRACING_INACTIVE
            !location -> Status.LOCATION_DISABLED
            !bluetooth -> Status.BLUETOOTH_DISABLED
            else -> Status.TRACING_ACTIVE
        }
    }
        .onStart { Timber.v("tracingStatus FLOW start") }
        .onEach { Timber.v("tracingStatus FLOW emission: %s", it) }
        .onCompletion { Timber.v("tracingStatus FLOW completed.") }

    enum class Status(val value: Int) {
        TRACING_ACTIVE(0),
        TRACING_INACTIVE(1),
        BLUETOOTH_DISABLED(2),
        LOCATION_DISABLED(3)
    }
}
