package de.rki.coronawarnapp.ui.main

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.ui.interoperability.InteroperabilityConfigurationFragment
import de.rki.coronawarnapp.ui.interoperability.InteroperabilityConfigurationFragmentModule
import de.rki.coronawarnapp.ui.main.home.HomeFragmentModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingDeltaInteroperabilityModule
import de.rki.coronawarnapp.ui.settings.SettingFragmentsModule
import de.rki.coronawarnapp.ui.submission.SubmissionFragmentModule
import de.rki.coronawarnapp.ui.tracing.details.RiskDetailsFragmentModule

@Module(
    includes = [
        OnboardingDeltaInteroperabilityModule::class,
        HomeFragmentModule::class,
        RiskDetailsFragmentModule::class,
        SettingFragmentsModule::class,
        SubmissionFragmentModule::class
    ]
)
abstract class MainActivityModule {

    // activity specific injection module for future dependencies

    // example:
    // @ContributesAndroidInjector
    // abstract fun mainFragment(): MainFragment

    @ContributesAndroidInjector(modules = [InteroperabilityConfigurationFragmentModule::class])
    abstract fun intertopConfigScreen(): InteroperabilityConfigurationFragment
}
