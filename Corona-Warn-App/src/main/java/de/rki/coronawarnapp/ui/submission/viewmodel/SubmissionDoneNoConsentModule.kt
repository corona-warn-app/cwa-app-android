package de.rki.coronawarnapp.ui.submission.viewmodel

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionDoneNoConsentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionDoneNoConsentViewModel::class)
    abstract fun submissionDoneNoConsentFragment(
        factory: SubmissionDoneNoConsentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
