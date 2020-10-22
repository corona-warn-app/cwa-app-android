package de.rki.coronawarnapp.ui.submission.tan

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionTanModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionTanViewModel::class)
    abstract fun submissionTanFragment(
        factory: SubmissionTanViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
