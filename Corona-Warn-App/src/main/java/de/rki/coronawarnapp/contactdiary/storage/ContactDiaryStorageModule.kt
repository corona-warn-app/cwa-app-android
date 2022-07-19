package de.rki.coronawarnapp.contactdiary.storage

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.datastore.migrations.SharedPreferencesMigration
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.storage.repo.DefaultContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.storage.settings.ContactDiarySettings
import de.rki.coronawarnapp.contactdiary.storage.settings.ContactDiarySettingsSerializer
import de.rki.coronawarnapp.contactdiary.storage.settings.ContactDiarySettingsStorage
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Singleton

@Module(includes = [ContactDiaryStorageModule.BindsModule::class, ContactDiaryStorageModule.ResetModule::class])
object ContactDiaryStorageModule {

    @Singleton
    @Provides
    fun provideDataStore(
        serializer: ContactDiarySettingsSerializer,
        @AppScope appScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider,
        @AppContext context: Context,
        migration: SharedPreferencesMigration<ContactDiarySettings>
    ): DataStore<ContactDiarySettings> = DataStoreFactory.create(
        serializer = serializer,
        scope = appScope + dispatcherProvider.IO,
        migrations = listOf(migration)
    ) {
        context.dataStoreFile(CONTACT_DIARY_SETTINGS_DATA_STORE)
    }

    @Provides
    fun provideMigration(
        @AppContext context: Context
    ) = SharedPreferencesMigration<ContactDiarySettings>(
        context = context,
        sharedPreferencesName = LEGACY_SHARED_PREFS
    ) { sharedPreferencesView, contactDiarySettings ->

        val migratedOnboardingStatus = runCatching {
            val migratedOrder = sharedPreferencesView.getInt(LEGACY_ONBOARDING_STATUS_KEY, (-1))
            ContactDiarySettings.OnboardingStatus.values().find { it.order == migratedOrder }
        }
            .onFailure { Timber.e(it, "Migration failed") }
            .getOrNull()

        migratedOnboardingStatus?.let { contactDiarySettings.copy(onboardingStatus = it) } ?: contactDiarySettings
    }

    @Module
    interface BindsModule {
        @Binds
        fun contactDiaryRepo(defaultContactDiaryRepository: DefaultContactDiaryRepository): ContactDiaryRepository
    }

    @Module
    interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableContactDiarySettingsStorage(resettable: ContactDiarySettingsStorage): Resettable

        @Binds
        @IntoSet
        fun bindResettableContactDiaryRepository(resettable: DefaultContactDiaryRepository): Resettable
    }
}

private const val CONTACT_DIARY_SETTINGS_DATA_STORE: String = "contact_diary_settings_data_store"
private const val LEGACY_SHARED_PREFS = "contact_diary_localdata"
@VisibleForTesting const val LEGACY_ONBOARDING_STATUS_KEY = "contact_diary_onboardingstatus"
