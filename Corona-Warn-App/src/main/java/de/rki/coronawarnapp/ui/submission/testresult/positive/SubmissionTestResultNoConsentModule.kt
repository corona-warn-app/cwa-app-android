package de.rki.coronawarnapp.ui.submission.testresult.positive

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionTestResultNoConsentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionTestResultNoConsentViewModel::class)
    abstract fun submissionTestResultNoConsentFragment(
        factory: SubmissionTestResultNoConsentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
