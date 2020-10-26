package de.rki.coronawarnapp.ui.submission.viewmodel

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionResultPositiveOtherWarningModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionResultPositiveOtherWarningViewModel::class)
    abstract fun submissionResultPositveOtherWarningFragment(
        factory: SubmissionResultPositiveOtherWarningViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
