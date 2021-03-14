package de.rki.coronawarnapp.ui.submission.qrcode.consent

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionConsentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionConsentViewModel::class)
    abstract fun submissionConsentFragment(
        factory: SubmissionConsentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
