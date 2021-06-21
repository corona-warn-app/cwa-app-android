package de.rki.coronawarnapp.covidcertificate.ui.onboarding

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class CovidCertificateOnboardingFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(CovidCertificateOnboardingViewModel::class)
    abstract fun covidCertificateOnboardingFragment(
        factory: CovidCertificateOnboardingViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
