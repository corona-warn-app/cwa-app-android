package de.rki.coronawarnapp.ui.submission.viewmodel

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionContactModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionContactViewModel::class)
    abstract fun submissionContactFragment(
        factory: SubmissionContactViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
