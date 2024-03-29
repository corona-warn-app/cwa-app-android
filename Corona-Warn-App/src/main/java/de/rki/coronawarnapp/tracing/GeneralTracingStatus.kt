package de.rki.coronawarnapp.tracing

import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.util.bluetooth.BluetoothProvider
import de.rki.coronawarnapp.util.location.LocationProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralTracingStatus @Inject constructor(
    bluetoothProvider: BluetoothProvider,
    locationProvider: LocationProvider,
    enfClient: ENFClient
) {

    val generalStatus: Flow<Status> = combine(
        bluetoothProvider.isBluetoothEnabled,
        enfClient.isTracingEnabled,
        locationProvider.isLocationEnabled,
        enfClient.isLocationLessScanningSupported
    ) { bluetoothEnabled, tracingEnabled, locationServices, locationLessScanning ->

        val locationEnabled = locationServices || locationLessScanning

        when {
            !tracingEnabled -> Status.TRACING_INACTIVE
            !locationEnabled -> Status.LOCATION_DISABLED
            !bluetoothEnabled -> Status.BLUETOOTH_DISABLED
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
