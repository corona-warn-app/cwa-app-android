package de.rki.coronawarnapp.ccl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.ccl.configuration.CclConfigurationModule
import de.rki.coronawarnapp.ccl.configuration.storage.CclConfigurationRepository
import de.rki.coronawarnapp.ccl.configuration.storage.DownloadedCclConfigurationStorage
import de.rki.coronawarnapp.ccl.configuration.update.CclSettings
import de.rki.coronawarnapp.ccl.configuration.update.CclSettingsDataStore
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database.DccWalletInfoDao
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database.DccWalletInfoDatabase
import de.rki.coronawarnapp.util.reset.Resettable
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module(includes = [CclConfigurationModule::class, CclModule.ResetModule::class])
object CclModule {

    @Singleton
    @Provides
    fun dccWalletInfoDao(
        dccWalletInfoDatabaseFactory: DccWalletInfoDatabase.Factory
    ): DccWalletInfoDao = dccWalletInfoDatabaseFactory.create().dccWalletInfoDao()

    @Singleton
    @Provides
    @CclSettingsDataStore
    fun provideCLLSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile(CCL_SETTINGS_DATASTORE_NAME)
        }

    private const val CCL_SETTINGS_DATASTORE_NAME = "ccl_settings_localdata"

    @InstallIn(SingletonComponent::class)
    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableCclSettings(resettable: CclSettings): Resettable

        @Binds
        @IntoSet
        fun bindResettableDownloadedCclConfigurationStorage(resettable: DownloadedCclConfigurationStorage): Resettable

        @Binds
        @IntoSet
        fun bindResettableCclConfigurationRepository(resettable: CclConfigurationRepository): Resettable

        @Binds
        @IntoSet
        fun bindResettableDccWalletInfoRepository(resettable: DccWalletInfoRepository): Resettable
    }
}
