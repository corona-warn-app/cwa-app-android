package de.rki.coronawarnapp.ui.main

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsFragmentModule
import de.rki.coronawarnapp.ui.information.InformationFragmentModule
import de.rki.coronawarnapp.ui.interoperability.InteroperabilityConfigurationFragment
import de.rki.coronawarnapp.ui.interoperability.InteroperabilityConfigurationFragmentModule
import de.rki.coronawarnapp.ui.main.home.HomeFragmentModule
import de.rki.coronawarnapp.ui.onboarding.OnboardingDeltaInteroperabilityModule
import de.rki.coronawarnapp.ui.settings.SettingFragmentsModule
import de.rki.coronawarnapp.ui.settings.SettingsResetFragment
import de.rki.coronawarnapp.ui.settings.SettingsResetModule
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionFragmentModule
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module(
    includes = [
        OnboardingDeltaInteroperabilityModule::class,
        HomeFragmentModule::class,
        TracingDetailsFragmentModule::class,
        SettingFragmentsModule::class,
        SubmissionFragmentModule::class,
        InformationFragmentModule::class
    ]
)
abstract class MainActivityModule {

    // activity specific injection module for future dependencies

    // example:
    // @ContributesAndroidInjector
    // abstract fun mainFragment(): MainFragment

    @ContributesAndroidInjector(modules = [InteroperabilityConfigurationFragmentModule::class])
    abstract fun intertopConfigScreen(): InteroperabilityConfigurationFragment

    @ContributesAndroidInjector(modules = [SettingsResetModule::class])
    abstract fun settingsResetScreen(): SettingsResetFragment

    @Binds
    @IntoMap
    @CWAViewModelKey(MainActivityViewModel::class)
    abstract fun mainActivityViewModel(
        factory: MainActivityViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
