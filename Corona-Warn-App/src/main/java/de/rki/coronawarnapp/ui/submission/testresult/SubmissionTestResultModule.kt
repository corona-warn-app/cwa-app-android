package de.rki.coronawarnapp.ui.submission.testresult

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionTestResultModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionTestResultViewModel::class)
    abstract fun submissionTestResultFragment(
        factory: SubmissionTestResultViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
