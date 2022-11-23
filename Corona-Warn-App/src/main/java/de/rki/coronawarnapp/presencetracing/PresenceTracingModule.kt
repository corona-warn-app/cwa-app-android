package de.rki.coronawarnapp.presencetracing

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.environment.presencetracing.qrcodeposter.QrCodePosterTemplateModule
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.organizer.submission.OrganizerSubmissionModule
import de.rki.coronawarnapp.presencetracing.storage.repo.DefaultTraceLocationRepository
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.presencetracing.warning.PresenceTracingWarningModule
import de.rki.coronawarnapp.ui.presencetracing.TraceLocationPreferences
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import javax.inject.Qualifier
import javax.inject.Singleton

@Module(
    includes = [
        QrCodePosterTemplateModule::class,
        PresenceTracingWarningModule::class,
        OrganizerSubmissionModule::class,
        PresenceTracingModule.PresenceTracingDataStoreModule::class,
        PresenceTracingModule.ResetModule::class,
    ]
)
interface PresenceTracingModule {

    @Binds
    fun traceLocationRepository(
        defaultTraceLocationRepo: DefaultTraceLocationRepository
    ): TraceLocationRepository

    @Module
    object PresenceTracingDataStoreModule {
        @Singleton
        @LocationSettingsDataStore
        @Provides
        fun provideLocationSettingsDataStore(
            @AppContext context: Context,
            @AppScope appScope: CoroutineScope,
            dispatcherProvider: DispatcherProvider
        ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = appScope + dispatcherProvider.IO,
            produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_LOCATION_SETTINGS_NAME) },
            migrations = listOf(
                SharedPreferencesMigration(
                    context,
                    LEGACY_SHARED_PREFS_LOCATION_SETTINGS_NAME
                )
            )
        )

        @Singleton
        @LocationPreferencesDataStore
        @Provides
        fun provideLocationPreferencesDataStore(
            @AppContext context: Context,
            @AppScope appScope: CoroutineScope,
            dispatcherProvider: DispatcherProvider
        ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = appScope + dispatcherProvider.IO,
            produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_LOCATION_PREFERENCES_SETTINGS_NAME) },
            migrations = listOf(
                SharedPreferencesMigration(
                    context,
                    LEGACY_SHARED_PREFS_LOCATION_PREFERENCES_SETTINGS_NAME
                )
            )
        )
    }

    @Module
    interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableTraceLocationPreferences(resettable: TraceLocationPreferences): Resettable

        @Binds
        @IntoSet
        fun bindResettableTraceLocationSettings(resettable: TraceLocationSettings): Resettable

        @Binds
        @IntoSet
        fun bindResettableCheckInRepository(resettable: CheckInRepository): Resettable

        @Binds
        @IntoSet
        fun bindResettableTraceLocationRepository(resettable: DefaultTraceLocationRepository): Resettable
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class LocationSettingsDataStore

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class LocationPreferencesDataStore

private const val LEGACY_SHARED_PREFS_LOCATION_PREFERENCES_SETTINGS_NAME = "trace_location_localdata"
private const val STORAGE_DATASTORE_LOCATION_PREFERENCES_SETTINGS_NAME = "location_preferences_storage"

private const val LEGACY_SHARED_PREFS_LOCATION_SETTINGS_NAME = "trace_location_localdata"
private const val STORAGE_DATASTORE_LOCATION_SETTINGS_NAME = "location_settings_storage"
