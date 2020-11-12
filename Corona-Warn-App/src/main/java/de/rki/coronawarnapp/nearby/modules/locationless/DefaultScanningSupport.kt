package de.rki.coronawarnapp.nearby.modules.locationless

import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultScanningSupport @Inject constructor(
    client: ExposureNotificationClient
) : ScanningSupport {

    override val isLocationLessScanningSupported: Flow<Boolean> = MutableStateFlow(
        client.deviceSupportsLocationlessScanning()
    )
        .onStart { Timber.v("isLocationLessScanningSupported FLOW start") }
        .onEach { Timber.v("isLocationLessScanningSupported FLOW emission: %b", it) }
        .onCompletion { Timber.v("isLocationLessScanningSupported FLOW completed.") }

    companion object {
        private const val TAG = "DefaultLocationLess"
    }
}
