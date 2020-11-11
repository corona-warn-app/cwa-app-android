@file:Suppress("DEPRECATION")

package de.rki.coronawarnapp.nearby

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.nearby.modules.detectiontracker.TrackedExposureDetection
import de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider.DiagnosisKeyProvider
import de.rki.coronawarnapp.nearby.modules.locationless.ScanningSupport
import de.rki.coronawarnapp.nearby.modules.tracing.TracingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.joda.time.Instant
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ENFClient @Inject constructor(
    private val googleENFClient: ExposureNotificationClient,
    private val diagnosisKeyProvider: DiagnosisKeyProvider,
    private val tracingStatus: TracingStatus,
    private val scanningSupport: ScanningSupport,
    private val exposureDetectionTracker: ExposureDetectionTracker
) : DiagnosisKeyProvider, TracingStatus, ScanningSupport {

    // TODO Remove this once we no longer need direct access to the ENF Client,
    // i.e. in **[InternalExposureNotificationClient]**
    internal val internalClient: ExposureNotificationClient
        get() = googleENFClient

    override suspend fun provideDiagnosisKeys(
        keyFiles: Collection<File>,
        configuration: ExposureConfiguration?,
        token: String
    ): Boolean {
        Timber.d(
            "asyncProvideDiagnosisKeys(keyFiles=%s, configuration=%s, token=%s)",
            keyFiles, configuration, token
        )

        return if (keyFiles.isEmpty()) {
            Timber.d("No key files submitted, returning early.")
            true
        } else {
            Timber.d("Forwarding %d key files to our DiagnosisKeyProvider.", keyFiles.size)
            exposureDetectionTracker.trackNewExposureDetection(token)
            diagnosisKeyProvider.provideDiagnosisKeys(keyFiles, configuration, token)
        }
    }

    override val isLocationLessScanningSupported: Flow<Boolean>
        get() = scanningSupport.isLocationLessScanningSupported

    override val isTracingEnabled: Flow<Boolean>
        get() = tracingStatus.isTracingEnabled

    fun isPerformingExposureDetection(): Flow<Boolean> = exposureDetectionTracker.calculations
        .map { it.values }
        .map { values ->
            values.maxBy { it.startedAt }?.isCalculating == true
        }

    fun latestTrackedExposureDetection(): Flow<Collection<TrackedExposureDetection>> =
        exposureDetectionTracker.calculations.map { it.values }

    fun lastSuccessfulTrackedExposureDetection(): Flow<TrackedExposureDetection?> =
        exposureDetectionTracker.calculations.map { snapshot ->
            snapshot.values
                .filter { !it.isCalculating && it.isSuccessful }
                .maxByOrNull { it.finishedAt ?: Instant.EPOCH }
        }
}
