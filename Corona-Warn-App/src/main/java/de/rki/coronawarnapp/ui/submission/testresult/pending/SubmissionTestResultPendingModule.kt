package de.rki.coronawarnapp.ui.submission.testresult.pending

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionTestResultPendingModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionTestResultPendingViewModel::class)
    abstract fun submissionTestResultFragment(
        factory: SubmissionTestResultPendingViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
