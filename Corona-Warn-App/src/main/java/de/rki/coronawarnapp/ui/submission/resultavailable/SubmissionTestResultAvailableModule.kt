package de.rki.coronawarnapp.ui.submission.resultavailable

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionTestResultAvailableModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionTestResultAvailableViewModel::class)
    abstract fun submissionTestResultAvailableFragment(
        factory: SubmissionTestResultAvailableViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
