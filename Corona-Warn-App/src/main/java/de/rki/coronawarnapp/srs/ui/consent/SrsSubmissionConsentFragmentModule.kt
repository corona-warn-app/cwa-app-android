package de.rki.coronawarnapp.srs.ui.consent

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SrsSubmissionConsentFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SrsSubmissionConsentFragmentViewModel::class)
    abstract fun srsSubmissionConsentFragmentViewModel(
        factory: SrsSubmissionConsentFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
