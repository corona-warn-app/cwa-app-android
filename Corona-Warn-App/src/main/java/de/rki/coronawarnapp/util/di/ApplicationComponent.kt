package de.rki.coronawarnapp.util.di

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.diagnosiskeys.DiagnosisKeysModule
import de.rki.coronawarnapp.diagnosiskeys.download.KeyFileDownloader
import de.rki.coronawarnapp.diagnosiskeys.server.AppConfigServer
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.http.ServiceFactory
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.ENFModule
import de.rki.coronawarnapp.receiver.ReceiverBinder
import de.rki.coronawarnapp.risk.RiskModule
import de.rki.coronawarnapp.service.ServiceBinder
import de.rki.coronawarnapp.storage.SettingsRepository
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisInjectionHelper
import de.rki.coronawarnapp.transaction.RiskLevelInjectionHelper
import de.rki.coronawarnapp.transaction.SubmitDiagnosisInjectionHelper
import de.rki.coronawarnapp.ui.ActivityBinder
import de.rki.coronawarnapp.util.ConnectivityHelperInjection
import de.rki.coronawarnapp.util.UtilModule
import de.rki.coronawarnapp.util.device.DeviceModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AssistedInjectModule::class,
        AndroidModule::class,
        ReceiverBinder::class,
        ServiceBinder::class,
        ActivityBinder::class,
        RiskModule::class,
        UtilModule::class,
        DeviceModule::class,
        ENFModule::class,
        HttpModule::class,
        DiagnosisKeysModule::class
    ]
)
interface ApplicationComponent : AndroidInjector<CoronaWarnApplication> {

    // TODO Remove once Singletons are gone
    val transRetrieveKeysInjection: RetrieveDiagnosisInjectionHelper
    val transRiskLevelInjection: RiskLevelInjectionHelper
    val transSubmitDiagnosisInjection: SubmitDiagnosisInjectionHelper

    val connectivityHelperInjection: ConnectivityHelperInjection

    val settingsRepository: SettingsRepository

    val keyCacheRepository: KeyCacheRepository
    val keyFileDownloader: KeyFileDownloader
    val serviceFactory: ServiceFactory

    val appConfigServer: AppConfigServer

    val enfClient: ENFClient

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: CoronaWarnApplication): ApplicationComponent
    }
}
