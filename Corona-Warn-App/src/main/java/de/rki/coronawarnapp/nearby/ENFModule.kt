package de.rki.coronawarnapp.nearby

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.nearby.modules.detectiontracker.DefaultExposureDetectionTracker
import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider.DefaultDiagnosisKeyProvider
import de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider.DiagnosisKeyProvider
import de.rki.coronawarnapp.nearby.modules.locationless.DefaultScanningSupport
import de.rki.coronawarnapp.nearby.modules.locationless.ScanningSupport
import de.rki.coronawarnapp.nearby.modules.tracing.DefaultTracingStatus
import de.rki.coronawarnapp.nearby.modules.tracing.TracingStatus
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Singleton

@Module
class ENFModule {

    @Singleton
    @Provides
    fun exposureNotificationClient(@AppContext context: Context): ExposureNotificationClient =
        Nearby.getExposureNotificationClient(context)

    @Singleton
    @Provides
    fun diagnosisKeySubmitter(submitter: DefaultDiagnosisKeyProvider): DiagnosisKeyProvider =
        submitter

    @Singleton
    @Provides
    fun tracingStatus(tracingStatus: DefaultTracingStatus): TracingStatus =
        tracingStatus

    @Singleton
    @Provides
    fun scanningSupport(scanningSupport: DefaultScanningSupport): ScanningSupport =
        scanningSupport

    @Singleton
    @Provides
    fun calculationTracker(exposureDetectionTracker: DefaultExposureDetectionTracker): ExposureDetectionTracker =
        exposureDetectionTracker
}
