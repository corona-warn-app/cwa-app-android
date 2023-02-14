package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsPCRKeySubmissionStorage as PCR
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsRAKeySubmissionStorage as RAT
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AnalyticsKeySubmissionModule {
    @Singleton
    @AnalyticsKeySubmissionDataStore
    @Provides
    fun provideAnalyticsKeySubmissionDataStore(
        @ApplicationContext context: Context,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = appScope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_ANALYTICS_KEY_SUBMISSION_SETTINGS_NAME) },
        migrations = listOf(
            SharedPreferencesMigration(
                context,
                LEGACY_SHARED_PREFS_ANALYTICS_KEY_SUBMISSION_SETTINGS_NAME
            ),
            SharedPreferencesMigration(
                context,
                LEGACY_SHARED_PREFS_ANALYTICS_KEY_SUBMISSION_SETTINGS_NAME_RAT
            )
        )
    )

    @Singleton
    @AnalyticsSrsKeySubmissionDataStore
    @Provides
    fun provideAnalyticsSrsKeySubmissionDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile("analytics_srs_key_submission_datastore")
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class AnalyticsKeySubmissionDataStore

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class AnalyticsSrsKeySubmissionDataStore

private const val LEGACY_SHARED_PREFS_ANALYTICS_KEY_SUBMISSION_SETTINGS_NAME =
    "analytics_key_submission_localdata${PCR.sharedPrefKeySuffix}"
private const val LEGACY_SHARED_PREFS_ANALYTICS_KEY_SUBMISSION_SETTINGS_NAME_RAT =
    "analytics_key_submission_localdata${RAT.sharedPrefKeySuffix}"
private const val STORAGE_DATASTORE_ANALYTICS_KEY_SUBMISSION_SETTINGS_NAME = "analytics_key_submission_storage"
