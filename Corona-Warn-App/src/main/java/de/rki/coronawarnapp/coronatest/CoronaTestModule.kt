package de.rki.coronawarnapp.coronatest

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
import de.rki.coronawarnapp.coronatest.server.VerificationModule
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTestProcessor
import de.rki.coronawarnapp.coronatest.type.pcr.PCRTestProcessor
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RATestProcessor
import de.rki.coronawarnapp.profile.ProfileModule
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
        CoronaTestModule.ResetModule::class,
        CoronaTestModule.CoronaTestStorageModule::class,
        VerificationModule::class,
        ProfileModule::class
    ]
)
interface CoronaTestModule {

    @Binds
    @IntoSet
    fun pcrProcessor(
        processor: PCRTestProcessor
    ): PersonalCoronaTestProcessor

    @Binds
    @IntoSet
    fun ratProcessor(
        processor: RATestProcessor
    ): PersonalCoronaTestProcessor

    @Module
    object CoronaTestStorageModule {
        @Singleton
        @CoronaTestStorageDataStore
        @Provides
        fun provideCoronaTestStorageDataStore(
            @AppContext context: Context,
            @AppScope appScope: CoroutineScope,
            dispatcherProvider: DispatcherProvider
        ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = appScope + dispatcherProvider.IO,
            produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_CORONA_TEST_STORAGE_SETTINGS_NAME) },
            migrations = listOf(
                SharedPreferencesMigration(
                    context,
                    LEGACY_SHARED_PREFS_CORONA_TEST_STORAGE_SETTINGS_NAME
                )
            )
        )
    }

    @Module
    interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableCoronaTestRepository(resettable: CoronaTestRepository): Resettable
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class CoronaTestStorageDataStore

private const val LEGACY_SHARED_PREFS_CORONA_TEST_STORAGE_SETTINGS_NAME = "coronatest_localdata"
private const val STORAGE_DATASTORE_CORONA_TEST_STORAGE_SETTINGS_NAME = "corona_test_storage"
