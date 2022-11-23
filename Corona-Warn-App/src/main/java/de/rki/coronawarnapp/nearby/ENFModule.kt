package de.rki.coronawarnapp.nearby

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
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
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import javax.inject.Qualifier
import javax.inject.Singleton

@Module(includes = [ENFModule.BindsModule::class])
object ENFModule {

    @Singleton
    @Provides
    fun exposureNotificationClient(@AppContext context: Context): ExposureNotificationClient =
        Nearby.getExposureNotificationClient(context)

    @Singleton
    @ENFClientDataStore
    @Provides
    fun provideENFClientDataStore(
        @AppContext context: Context,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = appScope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_ENFCLIENT_SETTINGS_NAME) },
        migrations = listOf(
            SharedPreferencesMigration(context, LEGACY_SHARED_PREFS_ENFCLIENT_SETTINGS_NAME)
        )
    )

    @Module
    internal interface BindsModule {

        @Binds
        @IntoSet
        fun bindResettableExposureDetectionTracker(resettable: ExposureDetectionTracker): Resettable

        @Binds
        fun diagnosisKeySubmitter(submitter: DefaultDiagnosisKeyProvider): DiagnosisKeyProvider

        @Binds
        fun tracingStatus(tracingStatus: DefaultTracingStatus): TracingStatus

        @Binds
        fun scanningSupport(scanningSupport: DefaultScanningSupport): ScanningSupport

        @Binds
        fun exposureWindowProvider(exposureWindowProvider: DefaultExposureWindowProvider): ExposureWindowProvider

        @Binds
        fun diagnosisKeysDataMapper(diagnosisKeysDataMapper: DefaultDiagnosisKeysDataMapper): DiagnosisKeysDataMapper

        @Binds
        fun calculationTracker(exposureDetectionTracker: DefaultExposureDetectionTracker): ExposureDetectionTracker

        @Binds
        fun enfClientVersion(enfVersion: DefaultENFVersion): ENFVersion

        @Binds
        fun tekHistory(tekHistory: DefaultTEKHistoryProvider): TEKHistoryProvider
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class ENFClientDataStore

private const val LEGACY_SHARED_PREFS_ENFCLIENT_SETTINGS_NAME = "enfclient_localdata"
private const val STORAGE_DATASTORE_ENFCLIENT_SETTINGS_NAME = "enfclient_storage"
