package de.rki.coronawarnapp.ccl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.ccl.configuration.update.CCLSettingsDataStore
import de.rki.coronawarnapp.ccl.configuration.CCLConfigurationModule
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database.DccWalletInfoDao
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database.DccWalletInfoDatabase
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTask
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskTypeKey
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Singleton

@Module(includes = [CCLConfigurationModule::class])
abstract class CCLModule {

    @Binds
    @IntoMap
    @TaskTypeKey(DccWalletInfoUpdateTask::class)
    abstract fun dccWalletInfoUpdateTaskFactory(
        factory: DccWalletInfoUpdateTask.Factory
    ): TaskFactory<out Task.Progress, out Task.Result>

    companion object {
        @Singleton
        @Provides
        fun dccWalletInfoDao(
            dccWalletInfoDatabaseFactory: DccWalletInfoDatabase.Factory
        ): DccWalletInfoDao = dccWalletInfoDatabaseFactory.create().dccWalletInfoDao()

        @Singleton
        @Provides
        @CCLSettingsDataStore
        fun provideCLLSettingsDataStore(@AppContext context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create {
                context.preferencesDataStoreFile(CCL_SETTINGS_DATASTORE_NAME)
            }

        private const val CCL_SETTINGS_DATASTORE_NAME = "ccl_settings_localdata"
    }
}
