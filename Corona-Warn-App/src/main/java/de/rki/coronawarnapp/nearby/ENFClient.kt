@file:Suppress("DEPRECATION")

package de.rki.coronawarnapp.nearby

import com.google.android.gms.nearby.exposurenotification.DiagnosisKeysDataMapping
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.nearby.modules.detectiontracker.TrackedExposureDetection
import de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider.DiagnosisKeyProvider
import de.rki.coronawarnapp.nearby.modules.exposurewindow.ExposureWindowProvider
import de.rki.coronawarnapp.nearby.modules.locationless.ScanningSupport
import de.rki.coronawarnapp.nearby.modules.tekhistory.TEKHistoryProvider
import de.rki.coronawarnapp.nearby.modules.tracing.TracingStatus
import de.rki.coronawarnapp.nearby.modules.version.ENFVersion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.joda.time.Instant
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ENFClient @Inject constructor(
    private val googleENFClient: ExposureNotificationClient,
    private val diagnosisKeyProvider: DiagnosisKeyProvider,
    private val tracingStatus: TracingStatus,
    private val scanningSupport: ScanningSupport,
    private val exposureWindowProvider: ExposureWindowProvider,
    private val exposureDetectionTracker: ExposureDetectionTracker,
    private val enfVersion: ENFVersion,
    private val tekHistoryProvider: TEKHistoryProvider
) : DiagnosisKeyProvider,
    TracingStatus by tracingStatus,
    ScanningSupport,
    ExposureWindowProvider,
    ENFVersion by enfVersion,
    TEKHistoryProvider by tekHistoryProvider {

    // TODO Remove this once we no longer need direct access to the ENF Client,
    // i.e. in **[InternalExposureNotificationClient]**
    internal val internalClient: ExposureNotificationClient
        get() = googleENFClient

    override suspend fun provideDiagnosisKeys(
        keyFiles: Collection<File>,
        newDiagnosisKeysDataMapping: DiagnosisKeysDataMapping
    ): Boolean {
        Timber.d("asyncProvideDiagnosisKeys(keyFiles=$keyFiles)")

        return if (keyFiles.isEmpty()) {
            Timber.d("No key files submitted, returning early.")
            true
        } else {
            Timber.d("Forwarding %d key files to our DiagnosisKeyProvider.", keyFiles.size)
            exposureDetectionTracker.trackNewExposureDetection(UUID.randomUUID().toString())
            diagnosisKeyProvider.provideDiagnosisKeys(keyFiles, newDiagnosisKeysDataMapping)
        }
    }

    override val isLocationLessScanningSupported: Flow<Boolean>
        get() = scanningSupport.isLocationLessScanningSupported

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

    override suspend fun exposureWindows(): List<ExposureWindow> = exposureWindowProvider.exposureWindows()
}
