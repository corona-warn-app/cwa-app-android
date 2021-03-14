package de.rki.coronawarnapp.ui.submission.testresult.positive

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionTestResultConsentGivenModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionTestResultConsentGivenViewModel::class)
    abstract fun submissionTestResultConsentGivenFragment(
        factory: SubmissionTestResultConsentGivenViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
