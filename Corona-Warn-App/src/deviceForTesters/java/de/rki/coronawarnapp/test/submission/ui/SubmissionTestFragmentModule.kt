package de.rki.coronawarnapp.test.submission.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionTestFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionTestFragmentViewModel::class)
    abstract fun testKeyDownloadFragment(
        factory: SubmissionTestFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
