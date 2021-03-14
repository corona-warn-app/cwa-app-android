package de.rki.coronawarnapp.ui.submission.testresult.invalid

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionTestResultInvalidModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionTestResultInvalidViewModel::class)
    abstract fun submissionTestResultInvalid(
        factory: SubmissionTestResultInvalidViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
