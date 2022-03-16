package de.rki.coronawarnapp.ccl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.ccl.configuration.CclConfigurationModule
import de.rki.coronawarnapp.ccl.configuration.update.CclSettingsDataStore
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database.DccWalletInfoDao
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database.DccWalletInfoDatabase
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Singleton

@Module(includes = [CclConfigurationModule::class])
object CclModule {

    @Singleton
    @Provides
    fun dccWalletInfoDao(
        dccWalletInfoDatabaseFactory: DccWalletInfoDatabase.Factory
    ): DccWalletInfoDao = dccWalletInfoDatabaseFactory.create().dccWalletInfoDao()

    @Singleton
    @Provides
    @CclSettingsDataStore
    fun provideCLLSettingsDataStore(@AppContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile(CCL_SETTINGS_DATASTORE_NAME)
        }

    private const val CCL_SETTINGS_DATASTORE_NAME = "ccl_settings_localdata"
}
