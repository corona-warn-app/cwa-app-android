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
import de.rki.coronawarnapp.nearby.modules.diagnosiskeysdatamapper.DefaultDiagnosisKeysDataMapper
import de.rki.coronawarnapp.nearby.modules.diagnosiskeysdatamapper.DiagnosisKeysDataMapper
import de.rki.coronawarnapp.nearby.modules.exposurewindow.DefaultExposureWindowProvider
import de.rki.coronawarnapp.nearby.modules.exposurewindow.ExposureWindowProvider
import de.rki.coronawarnapp.nearby.modules.locationless.DefaultScanningSupport
import de.rki.coronawarnapp.nearby.modules.locationless.ScanningSupport
import de.rki.coronawarnapp.nearby.modules.tekhistory.DefaultTEKHistoryProvider
import de.rki.coronawarnapp.nearby.modules.tekhistory.TEKHistoryProvider
import de.rki.coronawarnapp.nearby.modules.tracing.DefaultTracingStatus
import de.rki.coronawarnapp.nearby.modules.tracing.TracingStatus
import de.rki.coronawarnapp.nearby.modules.version.DefaultENFVersion
import de.rki.coronawarnapp.nearby.modules.version.ENFVersion
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
    fun exposureWindowProvider(exposureWindowProvider: DefaultExposureWindowProvider): ExposureWindowProvider =
        exposureWindowProvider

    @Singleton
    @Provides
    fun diagnosisKeysDataMapper(diagnosisKeysDataMapper: DefaultDiagnosisKeysDataMapper):
        DiagnosisKeysDataMapper = diagnosisKeysDataMapper

    @Singleton
    @Provides
    fun calculationTracker(exposureDetectionTracker: DefaultExposureDetectionTracker): ExposureDetectionTracker =
        exposureDetectionTracker

    @Singleton
    @Provides
    fun enfClientVersion(enfVersion: DefaultENFVersion): ENFVersion = enfVersion

    @Singleton
    @Provides
    fun tekHistory(tekHistory: DefaultTEKHistoryProvider): TEKHistoryProvider = tekHistory
}
