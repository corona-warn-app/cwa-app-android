package de.rki.coronawarnapp.ui.submission.testresultremoval

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionRemovePriorTestFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionRemovePriorTestFragmentViewModel::class)
    abstract fun overwriteInformationFragmentVM(
        factory: SubmissionRemovePriorTestFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
