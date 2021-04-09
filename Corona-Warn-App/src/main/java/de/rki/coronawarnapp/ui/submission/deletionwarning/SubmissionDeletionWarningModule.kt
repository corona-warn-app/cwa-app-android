package de.rki.coronawarnapp.ui.submission.deletionwarning

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionDeletionWarningModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionDeletionWarningFragmentViewModel::class)
    abstract fun submissionDeletionWarningFragmentVM(
        factory: SubmissionDeletionWarningFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
