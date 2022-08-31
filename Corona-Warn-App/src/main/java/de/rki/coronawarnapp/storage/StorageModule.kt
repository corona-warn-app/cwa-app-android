package de.rki.coronawarnapp.storage

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
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import javax.inject.Qualifier
import javax.inject.Singleton

@Module(includes = [StorageModule.ResetModule::class])
object StorageModule {

    @Singleton
    @OnboardingSettingsDataStore
    @Provides
    fun provideOnboardingSettingsDataStore(
        @AppContext context: Context,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = appScope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_ONBOARDING_SETTINGS_NAME) },
        migrations = listOf(
            SharedPreferencesMigration(
                context,
                LEGACY_SHARED_PREFS_ONBOARDING_SETTINGS_NAME
            )
        )
    )

    @Singleton
    @TestSettingsDataStore
    @Provides
    fun provideTestSettingsDataStore(
        @AppContext context: Context,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = appScope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_TEST_SETTINGS_NAME) },
        migrations = listOf(
            SharedPreferencesMigration(
                context,
                LEGACY_SHARED_PREFS_TEST_SETTINGS_NAME
            )
        )
    )

    @Singleton
    @TracingSettingsDataStore
    @Provides
    fun provideTracingSettingsDataStore(
        @AppContext context: Context,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = appScope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_TRACING_SETTINGS_NAME) },
        migrations = listOf(
            SharedPreferencesMigration(
                context,
                LEGACY_SHARED_PREFS_TRACING_SETTINGS_NAME
            )
        )
    )

    @Module
    internal interface ResetModule {
        @Binds
        @IntoSet
        fun bindResettableOnboardingSettings(resettable: OnboardingSettings): Resettable

        @Binds
        @IntoSet
        fun bindResettableTestSettings(resettable: TestSettings): Resettable

        @Binds
        @IntoSet
        fun bindResettableTracingSettings(resettable: TracingSettings): Resettable
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class OnboardingSettingsDataStore

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class TestSettingsDataStore

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class TracingSettingsDataStore

private const val LEGACY_SHARED_PREFS_ONBOARDING_SETTINGS_NAME = "onboarding_localdata"
private const val LEGACY_SHARED_PREFS_TEST_SETTINGS_NAME = "test_settings"
private const val LEGACY_SHARED_PREFS_TRACING_SETTINGS_NAME = "tracing_settings"

private const val STORAGE_DATASTORE_ONBOARDING_SETTINGS_NAME = "onboarding_settings_storage"
private const val STORAGE_DATASTORE_TEST_SETTINGS_NAME = "test_settings_storage"
private const val STORAGE_DATASTORE_TRACING_SETTINGS_NAME = "tracing_settings_storage"
