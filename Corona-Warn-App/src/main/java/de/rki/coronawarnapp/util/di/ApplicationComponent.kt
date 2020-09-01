package de.rki.coronawarnapp.util.di

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.receiver.ReceiverBinder
import de.rki.coronawarnapp.risk.RiskModule
import de.rki.coronawarnapp.service.ServiceBinder
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisInjectionHelper
import de.rki.coronawarnapp.transaction.RiskLevelInjectionHelper
import de.rki.coronawarnapp.transaction.SubmitDiagnosisInjectionHelper
import de.rki.coronawarnapp.ui.ActivityBinder
import de.rki.coronawarnapp.util.device.DeviceModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AndroidModule::class,
        ReceiverBinder::class,
        ServiceBinder::class,
        ActivityBinder::class,
        DeviceModule::class,
        RiskModule::class
    ]
)
interface ApplicationComponent : AndroidInjector<CoronaWarnApplication> {

    val transRetrieveKeysInjection: RetrieveDiagnosisInjectionHelper
    val transRiskLevelInjection: RiskLevelInjectionHelper
    val transSubmitDiagnosisInjection: SubmitDiagnosisInjectionHelper

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: CoronaWarnApplication): ApplicationComponent
    }
}
