package de.rki.coronawarnapp.ui.submission.submissiondone

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionDoneModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionDoneViewModel::class)
    abstract fun submissionDoneFragmentVM(
        factory: SubmissionDoneViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
