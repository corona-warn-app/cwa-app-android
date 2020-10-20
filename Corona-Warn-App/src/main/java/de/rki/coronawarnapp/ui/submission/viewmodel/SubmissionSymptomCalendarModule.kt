package de.rki.coronawarnapp.ui.submission.viewmodel

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionSymptomCalendarModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionSymptomCalendarViewModel::class)
    abstract fun submissionSymptomCalendarFragment(
        factory: SubmissionSymptomCalendarViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
