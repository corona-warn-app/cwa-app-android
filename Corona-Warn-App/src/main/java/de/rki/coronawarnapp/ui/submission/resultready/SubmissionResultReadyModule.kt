package de.rki.coronawarnapp.ui.submission.resultready

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionResultReadyModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionResultReadyViewModel::class)
    abstract fun submissionDoneNoConsentFragment(
        factory: SubmissionResultReadyViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
