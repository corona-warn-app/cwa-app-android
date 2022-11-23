package de.rki.coronawarnapp.datadonation.analytics.modules.testresult

import dagger.Module
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Provides
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import javax.inject.Qualifier
import javax.inject.Singleton
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsPCRTestResultSettings as PCR
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsRATestResultSettings as RAT

@Module
class AnalyticsTestResultModule {
    @Singleton
    @AnalyticsTestResultSettingsDataStore
    @Provides
    fun provideAnalyticsTestResultSettingsDataStore(
        @AppContext context: Context,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = appScope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_ANALYTICS_TEST_RESULT_SETTINGS_NAME) },
        migrations = listOf(
            SharedPreferencesMigration(
                context,
                LEGACY_SHARED_PREFS_ANALYTICS_TEST_RESULT_SETTINGS_NAME
            ),
            SharedPreferencesMigration(
                context,
                LEGACY_SHARED_PREFS_ANALYTICS_TEST_RESULT_SETTINGS_NAME_RAT
            )
        )
    )

    @Singleton
    @AnalyticsExposureWindowsDataStore
    @Provides
    fun provideAnalyticsExposureWindowsDataStore(
        @AppContext context: Context,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = appScope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_ANALYTICS_EXPOSURE_WINDOWS_SETTINGS_NAME) },
        migrations = listOf(
            SharedPreferencesMigration(
                context,
                LEGACY_SHARED_PREFS_ANALYTICS_EXPOSURE_WINDOWS_SETTINGS_NAME
            )
        )
    )
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class AnalyticsExposureWindowsDataStore

private const val LEGACY_SHARED_PREFS_ANALYTICS_EXPOSURE_WINDOWS_SETTINGS_NAME = "analytics_exposureWindows"
private const val STORAGE_DATASTORE_ANALYTICS_EXPOSURE_WINDOWS_SETTINGS_NAME = "analytics_exposure_windows_storage"

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class AnalyticsTestResultSettingsDataStore

private const val LEGACY_SHARED_PREFS_ANALYTICS_TEST_RESULT_SETTINGS_NAME =
    "analytics_testResultDonor${PCR.sharedPrefKeySuffix}"
private const val LEGACY_SHARED_PREFS_ANALYTICS_TEST_RESULT_SETTINGS_NAME_RAT =
    "analytics_testResultDonor${RAT.sharedPrefKeySuffix}"
private const val STORAGE_DATASTORE_ANALYTICS_TEST_RESULT_SETTINGS_NAME = "analytics_test_result_settings_storage"
