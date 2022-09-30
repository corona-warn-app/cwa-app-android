package de.rki.coronawarnapp.risk

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.risk.storage.DefaultRiskLevelStorage
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskTypeKey
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import javax.inject.Qualifier
import javax.inject.Singleton

@Module(includes = [RiskModule.ResetModule::class, RiskModule.RiskLevelSettingsDataStoreModule::class])
interface RiskModule {

    @Binds
    @IntoMap
    @TaskTypeKey(EwRiskLevelTask::class)
    fun riskLevelTaskFactory(
        factory: EwRiskLevelTask.Factory
    ): TaskFactory<out Task.Progress, out Task.Result>

    @Binds
    fun bindRiskLevelCalculation(
        riskLevelCalculation: DefaultRiskLevels
    ): RiskLevels

    @Binds
    fun riskLevelStorage(
        storage: DefaultRiskLevelStorage
    ): RiskLevelStorage

    @Module
    interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableRiskLevelStorage(resettable: DefaultRiskLevelStorage): Resettable
    }

    @Module
    object RiskLevelSettingsDataStoreModule {
        @Singleton
        @RiskLevelSettingsDataStore
        @Provides
        fun provideRiskLevelSettingsDataStore(
            @AppContext context: Context,
            @AppScope appScope: CoroutineScope,
            dispatcherProvider: DispatcherProvider
        ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = appScope + dispatcherProvider.IO,
            produceFile = { context.preferencesDataStoreFile(STORAGE_DATASTORE_RISK_LEVEL_SETTINGS_NAME) },
            migrations = listOf(
                SharedPreferencesMigration(
                    context,
                    LEGACY_SHARED_PREFS_RISK_LEVEL_SETTINGS_NAME
                )
            )
        )
    }
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class RiskLevelSettingsDataStore

private const val LEGACY_SHARED_PREFS_RISK_LEVEL_SETTINGS_NAME = "risklevel_localdata"
private const val STORAGE_DATASTORE_RISK_LEVEL_SETTINGS_NAME = "risklevel_storage"
