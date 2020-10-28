package de.rki.coronawarnapp.util.di

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.appconfig.AppConfigModule
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.diagnosiskeys.DiagnosisKeysModule
import de.rki.coronawarnapp.diagnosiskeys.download.KeyFileDownloader
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.environment.EnvironmentModule
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.ENFModule
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.playbook.PlaybookModule
import de.rki.coronawarnapp.receiver.ReceiverBinder
import de.rki.coronawarnapp.risk.RiskModule
import de.rki.coronawarnapp.service.ServiceBinder
import de.rki.coronawarnapp.storage.SettingsRepository
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.SubmissionModule
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.internal.TaskModule
import de.rki.coronawarnapp.test.DeviceForTestersModule
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisInjectionHelper
import de.rki.coronawarnapp.transaction.SubmitDiagnosisInjectionHelper
import de.rki.coronawarnapp.ui.ActivityBinder
import de.rki.coronawarnapp.util.ConnectivityHelperInjection
import de.rki.coronawarnapp.util.UtilModule
import de.rki.coronawarnapp.util.coroutine.AppCoroutineScope
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.CoroutineModule
import de.rki.coronawarnapp.util.device.DeviceModule
import de.rki.coronawarnapp.util.security.EncryptedPreferencesFactory
import de.rki.coronawarnapp.util.security.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.verification.VerificationModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AssistedInjectModule::class,
        CoroutineModule::class,
        AndroidModule::class,
        ReceiverBinder::class,
        ServiceBinder::class,
        ActivityBinder::class,
        RiskModule::class,
        UtilModule::class,
        DeviceModule::class,
        ENFModule::class,
        HttpModule::class,
        EnvironmentModule::class,
        DiagnosisKeysModule::class,
        AppConfigModule::class,
        SubmissionModule::class,
        VerificationModule::class,
        PlaybookModule::class,
        TaskModule::class,
        DeviceForTestersModule::class,
        SerializationModule::class
    ]
)
interface ApplicationComponent : AndroidInjector<CoronaWarnApplication> {

    // TODO Remove once Singletons are gone
    val transRetrieveKeysInjection: RetrieveDiagnosisInjectionHelper
    val transSubmitDiagnosisInjection: SubmitDiagnosisInjectionHelper

    val connectivityHelperInjection: ConnectivityHelperInjection

    val settingsRepository: SettingsRepository

    val keyCacheRepository: KeyCacheRepository
    val keyFileDownloader: KeyFileDownloader

    val appConfigProvider: AppConfigProvider

    val enfClient: ENFClient

    val encryptedPreferencesFactory: EncryptedPreferencesFactory
    val errorResetTool: EncryptionErrorResetTool

    val playbook: Playbook

    val interoperabilityRepository: InteroperabilityRepository

    val taskController: TaskController

    @AppScope val appScope: AppCoroutineScope

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: CoronaWarnApplication): ApplicationComponent
    }
}
