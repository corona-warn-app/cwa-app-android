package de.rki.coronawarnapp.ui.submission.symptoms.introduction

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionSymptomIntroductionModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionSymptomIntroductionViewModel::class)
    abstract fun submissionSymptomIntroductionFragment(
        factory: SubmissionSymptomIntroductionViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
