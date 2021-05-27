package de.rki.coronawarnapp.greencertificate.ui.onboarding

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class CertificatesOnboardingFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(CertificatesOnboardingViewModel::class)
    abstract fun certificatesFragment(
        factory: CertificatesOnboardingViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
