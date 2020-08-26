package de.rki.coronawarnapp.util.di

import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.receiver.ReceiverBinder
import de.rki.coronawarnapp.risk.RiskModule
import de.rki.coronawarnapp.service.ServiceBinder
import de.rki.coronawarnapp.ui.ActivityBinder
import de.rki.coronawarnapp.util.UtilModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AndroidModule::class,
        ReceiverBinder::class,
        ServiceBinder::class,
        ActivityBinder::class,
        RiskModule::class,
        UtilModule::class
    ]
)
interface ApplicationComponent : AndroidInjector<CoronaWarnApplication> {
    @Component.Factory
    interface Factory : AndroidInjector.Factory<CoronaWarnApplication>
}
